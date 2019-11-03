package com.mvv.bots.vk.main;

import com.google.gson.*;
import com.mvv.bots.vk.database.models.User;
import com.mvv.bots.vk.database.services.UserService;
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

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class Server {

    private static int restartsCount = 0;

    public Server(){
        try {
            System.out.println("Запуск сервера.");
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(Integer.parseInt(System.getenv("PORT"))), 0);
            server.createContext("/callback", new CallbackHandler());
            server.createContext("/", new MainHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Сервер запущен.");
        } catch (IOException e) {
            System.out.println("Сервер остановлен с ошибкой.");
            e.printStackTrace(System.out);
            System.out.println("Попытка запуска №"+restartsCount+"...");
            restartsCount++;
            if(restartsCount > 3){
                System.out.println("Остановка.");
                System.exit(0);
            }
            new Server();
        }
    }

    public static void main(String[] args) {
        if(args.length > 0){
            System.out.println(List.of(args));
            for(String arg : args){
                if(arg.equals("%update%")){
                    update();
                }
            }
            return;
        }
        new Server();
    }

    private static class CallbackHandler implements HttpHandler {

        private static List<String> spamList = new ArrayList<>();

        @Override
        public void handle(HttpExchange exchange) {
            try {
                String request = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).readLine();

                System.out.println("Запрос: "+request);

                OutputStream response = exchange.getResponseBody();

                if(spamList.contains(request)) {
                    System.out.println("Спам: " + request);
                    String answer = "OK";
                    byte[] bytes = answer.getBytes();
                    try {
                        exchange.sendResponseHeaders(200, bytes.length);
                        response.write(bytes);
                        response.flush();
                        response.close();
                        exchange.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                spamList.add(request);

                CallbackApi callback = new CallbackApi(){

                    @Override
                    public void confirmation(Integer groupId) {
                        if(groupId != Config.GROUP_ID){
                            System.out.println("Не совпадает группа: "+groupId);
                            return;
                        }
                        System.out.println("Подтверждение токена.");
                        answer(Config.CONFIRMATION_TOKEN);
                    }

                    @Override
                    public void messageNew(Integer groupId, Message message) {
                        if(groupId != Config.GROUP_ID){
                            System.out.println("Не совпадает группа: "+groupId);
                            return;
                        }
                        System.out.println("Новое сообщение: "+message);
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
                            e.printStackTrace();
                        }
                    }
                };

                callback.parse(request);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private static class MainHandler implements HttpHandler {

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
                e.printStackTrace();
            }
        }
    }

    private static void update() {
        Config.SCRIPTS.forEach(Script::update);
        /*try {
            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message("Обновление.")
                    .peerId(Config.ADMIN_ID)
                    .randomId(Utils.getRandomInt32())
                    .execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }*/
    }

    private static void parseMessage(Message message) {
        try {
            System.out.println(message);

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
            }else if(Script.containsByKey(message.getText())){
                Script script = Script.getByKey(message.getText());
                script.send(message, 0);
                plusUse(message);
            }else{

            }

        }catch (PatternSyntaxException | ClientException | ApiException e){
            e.printStackTrace();
        }
    }

    private static void plusUse(Message message){
        UserService userService = new UserService();
        System.out.println(1);
        User user = userService.findUser(message.getFromId());
        System.out.println(2);
        if(user == null){
            System.out.println(3);
            user = new User(message.getFromId());
            user.setUse(1);
            userService.saveUser(user);
        }else{
            System.out.println(4);
            int use = user.getUse();
            user.setUse(use + 1);
            userService.saveUser(user);
        }
    }

}
