package com.github.malyshgit.bots.vk.main;

import com.github.malyshgit.bots.vk.Config;
import com.github.malyshgit.bots.vk.database.dao.OptionsTable;
import com.github.malyshgit.bots.vk.database.dao.ResaveTable;
import com.github.malyshgit.bots.vk.database.dao.UsersTable;
import com.github.malyshgit.bots.vk.database.models.Option;
import com.github.malyshgit.bots.vk.database.models.User;
import com.github.malyshgit.bots.vk.utils.Utils;
import com.google.gson.*;
import com.github.malyshgit.bots.vk.main.commands.Authorization;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.callback.CallbackApi;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.messages.MessageWithClientInfo;
import com.vk.api.sdk.objects.messages.Message;
import com.github.malyshgit.bots.vk.main.commands.Commands;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);

    private static int restartsCount = 0;

    public Server(){
        try {
            LOG.debug("Запуск сервера.");
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(Integer.parseInt(System.getenv("PORT"))), 0);
            server.createContext("/callback", new CallbackHandler());
            server.createContext("/", new MainHandler());
            server.setExecutor(null);
            server.start();
            LOG.debug("Сервер запущен.");
        } catch (IOException e) {
            LOG.error("Сервер остановлен с ошибкой.");
            LOG.error(e);
            LOG.debug("Попытка запуска №"+restartsCount+"...");
            restartsCount++;
            if(restartsCount > 3){
                LOG.error("Остановка.");
                System.exit(0);
            }
            new Server();
        }
    }

    public static void main(String[] args) {
            Config.load();
            UsersTable.create();
            ResaveTable.create();
        if(args.length > 0){
            for(var arg : args){
                switch (arg){
                    case "%update%":
                        Config.COMMANDS.forEach(Command::update);
                        break;
                    default:
                        break;
                }
            }
            return;
        }
        new Server();
    }

    private static class CallbackHandler implements HttpHandler {
        private static final Logger LOG = LogManager.getLogger(CallbackHandler.class);

        private static List<String> spamList = new ArrayList<>();

        @Override
        public void handle(HttpExchange exchange) {
            try {
                String request = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).readLine();

                LOG.debug("Запрос: "+request);

                OutputStream response = exchange.getResponseBody();

                if(spamList.contains(request)) {
                    LOG.warn("Спам: " + request);
                    String answer = "OK";
                    byte[] bytes = answer.getBytes();
                    try {
                        exchange.sendResponseHeaders(200, bytes.length);
                        response.write(bytes);
                        response.flush();
                        response.close();
                        exchange.close();
                    }catch (IOException e) {
                        LOG.error(e);
                    }
                    return;
                }
                spamList.add(request);

                CallbackApi callback = new CallbackApi(){

                    @Override
                    public void confirmation(Integer groupId) {
                        if(groupId != Config.GROUP_ID){
                            LOG.warn("Не совпадает группа: "+groupId);
                            return;
                        }
                        LOG.debug("Подтверждение токена.");
                        answer(Config.CONFIRMATION_TOKEN);
                    }

                    @Override
                    public void messageNew(Integer groupId, MessageWithClientInfo messageWithClientInfo) {
                        Message message = messageWithClientInfo.getMessage();
                        if(groupId != Config.GROUP_ID){
                            LOG.warn("Не совпадает группа: "+groupId);
                            return;
                        }
                        LOG.debug("Новое сообщение: "+message);
                        parseMessage(message);
                        answer("OK");
                    }

                    public void answer(String answer){
                        byte[] bytes = (answer == null) ? new byte[0] : answer.getBytes();
                        try {
                            exchange.sendResponseHeaders(200, bytes.length);
                            response.write(bytes);
                            response.flush();
                            response.close();
                            exchange.close();
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                    }
                };

                callback.parse(request);
            }catch (IOException e){
                LOG.error(e);
            }
        }
    }
    private static class MainHandler implements HttpHandler {
        private static final Logger LOG = LogManager.getLogger(MainHandler.class);
        private static String indexHtmlString;
        static{
            try {
                var is = Server.class.getResourceAsStream("/www/auth.html");
                indexHtmlString = IOUtils.toString(is, StandardCharsets.UTF_8);
                indexHtmlString = indexHtmlString.replace("%APP_ID%", String.valueOf(Config.APP_ID));
                indexHtmlString = indexHtmlString.replace("%REDIRECT_URL%", Config.REDIRECT_URL);
            } catch (IOException e) {
                LOG.error(e);
            }
        }


        @Override
        public void handle(HttpExchange exchange) {
            try {
                String path = exchange.getRequestURI().getPath().substring(1).replaceAll("//", "/");

                String query = exchange.getRequestURI().getQuery();

                if(query != null){
                    var params = new URIBuilder(exchange.getRequestURI()).getQueryParams();
                    var code = params.stream()
                            .filter(p -> p.getName().equalsIgnoreCase("code"))
                            .map(NameValuePair::getValue)
                            .findFirst()
                            .orElse(null);
                    if(code != null){
                        if(Authorization.getToken(code)) {
                            path = "authSuccess.html";
                        }else{
                            path = "auth.html";
                        }
                    }
                }

                if (path.length() == 0) path = "auth.html";

                String www = "/www/";
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (Server.class.getResource(www + path) == null) {
                    bytes.write("404".getBytes());
                }else{
                    if(path.equals("auth.html")) {
                        bytes.write(indexHtmlString.getBytes(StandardCharsets.UTF_8));
                    }else bytes.write(IOUtils.toByteArray(Server.class.getResourceAsStream(www + path)));
                }

                if (path.endsWith(".js")) {
                    exchange.getResponseHeaders().set("Content-Type", "text/javascript");
                }else if (path.endsWith(".html"))
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                else if (path.endsWith(".css"))
                    exchange.getResponseHeaders().set("Content-Type", "text/css");
                else if (path.endsWith(".json"))
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                else if (path.endsWith(".svg"))
                    exchange.getResponseHeaders().set("Content-Type", "image/svg+xml");
                if (exchange.getRequestMethod().equals("HEAD")) {
                    exchange.getResponseHeaders().set("Content-Length", "" + bytes.toByteArray().length);
                    exchange.sendResponseHeaders(200, -1);
                    return;
                }

                exchange.sendResponseHeaders(200, bytes.toByteArray().length);
                exchange.getResponseBody().write(bytes.toByteArray());
                exchange.getResponseBody().close();
                bytes.close();
            }catch (IOException e){
                LOG.error(e);
            }
        }
    }

    private static void update() {
        try {
            Option update = OptionsTable.findByKey("update");
            if(update != null) {
                if (Boolean.parseBoolean(update.getValue())) {
                    Config.COMMANDS.forEach(Command::update);
                    Option debbug = OptionsTable.findByKey("debbug");
                    if (debbug != null) {
                        if (Boolean.parseBoolean(debbug.getValue())) {
                            new Messages(Config.VK())
                                    .send(Config.GROUP)
                                    .message("Обновление.")
                                    .peerId(Config.ADMIN_ID)
                                    .randomId(Utils.getRandomInt32())
                                    .execute();
                        }
                    }
                }
            }
        } catch (ApiException | ClientException e) {
            LOG.error(e);
        }
    }

    private static void parseMessage(Message message) {
        try {
            if(message.getPeerId() >= 2000000000){
                new Messages(Config.VK())
                        .markAsAnsweredConversation(Config.GROUP, message.getPeerId())
                        .execute();
            }else{
                new Messages(Config.VK())
                        .markAsRead(Config.GROUP)
                        .peerId(message.getPeerId())
                        .execute();
            }

            if(message.getPayload() != null){
                JsonElement jelement = new JsonParser().parse(message.getPayload());
                JsonObject  jobject = jelement.getAsJsonObject();
                if(jobject.has("command")){
                    String command = jobject.get("command").getAsString();
                    if(command.equals("start")){
                        if(UsersTable.findById(message.getPeerId()) == null) UsersTable.save(new User(message.getPeerId()));
                        new Commands().send(message, 0);
                    }
                }else if(jobject.has("script") && jobject.has("step")){
                    Command command = Command.getByName(jobject.get("script").getAsString());
                    int step = jobject.get("step").getAsInt();
                    command.send(message, step);
                }
            }else if(!message.getText().isEmpty() && Command.containsByKey(message.getText())){
                if(UsersTable.findById(message.getPeerId()) == null) UsersTable.save(new User(message.getPeerId()));
                Command command = Command.getByKey(message.getText());
                command.send(message, 0);
            }else{

            }

        }catch (PatternSyntaxException | ClientException | ApiException e){
            LOG.error(e);
        }
    }

}
