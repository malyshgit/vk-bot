package com.mvv.bots.vk.main;

import com.google.gson.*;
import com.mvv.bots.vk.commands.AdminPanel;
import com.mvv.bots.vk.database.tables.Settings;
import com.mvv.bots.vk.database.tables.Users;
import com.mvv.bots.vk.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.callback.CallbackApi;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.commands.Script;
import com.mvv.bots.vk.commands.ScriptList;
import org.apache.commons.io.IOUtils;
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
        if(args.length > 0){
            LOG.debug(List.of(args));
            Settings.Option update = Settings.find("update");
            if(update != null){
                if(Boolean.parseBoolean(update.getValue())) {
                    for (String arg : args) {
                        if (arg.equals("%update%")) {
                            update();
                        }
                    }
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
                    public void messageNew(Integer groupId, Message message) {
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

        @Override
        public void handle(HttpExchange exchange) {
            try {
                String path = exchange.getRequestURI().getPath().substring(1).replaceAll("//", "/");

                if (path.length() == 0) path = "index.html";

                String www = "/www/";
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (Server.class.getResource(www + path) == null) {
                    bytes.write("404".getBytes());
                }else{
                    bytes.write(IOUtils.toByteArray(Server.class.getResourceAsStream(www+path)));
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
            Settings.Option update = Settings.find("update");
            if(update != null) {
                if (Boolean.parseBoolean(update.getValue())) {
                    Config.SCRIPTS.forEach(Script::update);
                    Settings.Option debbug = Settings.find("debbug");
                    if (debbug != null) {
                        if (Boolean.parseBoolean(debbug.getValue())) {
                            new Messages(Config.VK)
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
                new Messages(Config.VK)
                        .markAsAnsweredConversation(Config.GROUP, message.getPeerId())
                        .execute();
            }else{
                new Messages(Config.VK)
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
                        new ScriptList().send(message, 0);
                        plusUse(message);
                    }
                }else if(jobject.has("script") && jobject.has("step")){
                    Script script = Script.getByName(jobject.get("script").getAsString());
                    int step = jobject.get("step").getAsInt();
                    if(step == 0){
                        plusUse(message);
                    }
                    script.send(message, step);
                }
            }else if(!message.getText().isEmpty() && Script.containsByKey(message.getText())){
                Script script = Script.getByKey(message.getText());
                script.send(message, 0);
                plusUse(message);
            }else{
                if(message.getFromId() == Config.ADMIN_ID){
                    Users.User user = Users.find(message.getFromId());
                    if(user.getParameters().has("savemode")) {
                        if (Boolean.parseBoolean(user.getParameters().get("savemode"))) {
                            new AdminPanel().send(message, 63);
                            return;
                        }
                    }
                }
            }

        }catch (PatternSyntaxException | ClientException | ApiException e){
            LOG.error(e);
        }
    }

    private static void plusUse(Message message){
        Users.User user = Users.find(message.getFromId());
        if(user != null){
            user.setUse(user.getUse()+1);
            Users.update(user);
        }else{
            user = new Users.User(message.getFromId());
            user.setUse(1);
            Users.add(user);
        }
    }

}
