/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.malyshgit.bots.vk.main.commands;

import com.github.malyshgit.bots.vk.Config;
import com.github.malyshgit.bots.vk.database.dao.UsersTable;
import com.github.malyshgit.bots.vk.database.models.User;
import com.github.malyshgit.bots.vk.main.AccessMode;
import com.github.malyshgit.bots.vk.main.Command;
import com.github.malyshgit.bots.vk.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Advice implements Command {

    @Override
    public String smile(){
        return "\uD83D\uDCAC";
    }

    @Override
    public String key(){
        return "совет";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - даст дельный совет.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.USER;
    }

    @Override
    public void update() {
        if(LocalDateTime.now().getMinute() >= 30) return;
        UsersTable.findAll().forEach(user -> {
            if(user.getFields().containsKey("advice")){
                JsonObject options = user.getFields().get("advice").getAsJsonObject();
                var update = options.get("update").getAsBoolean();
                if(update){
                    Message m = new Message();
                    m.setFromId(user.getId());
                    m.setPeerId(user.getId());
                    send(m, 0);
                }
            }
        });
    }

    @Override
    public void send(Message message, Integer step) {
        try {
            Keyboard keyboard = new Keyboard();

            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);

            switch (step) {
                case 0:
                    User user = UsersTable.findById(message.getFromId());
                    keyboard.setInline(true);
                    if(user.getFields().containsKey("advice")){
                        var options = user.getFields().get("advice").getAsJsonObject();
                        var update = options.get("update").getAsBoolean();
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(update
                                                ? KeyboardButtonColor.NEGATIVE
                                                : KeyboardButtonColor.POSITIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", getClass().getName())
                                                        .put("step", 2)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.CALLBACK)
                                                .setLabel(update
                                                        ? "Отписаться"
                                                        : "Подписаться"))
                        ));
                    }else{
                        var object = new JsonObject();
                        object.addProperty("update", false);
                        user.getFields().put("advice", object);
                        user.update();
                        send(message, 0);
                        return;
                    }
                    /*boolean censored;
                    if(user.getParameters().has("advicecensored")){
                        censored = Boolean.parseBoolean(user.getParameters().get("advicecensored"));
                        if(censored){
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.DEFAULT)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 5 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Цензура: вкл"))
                            ));
                        }else{
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.DEFAULT)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 4 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Цензура: выкл"))
                            ));
                        }
                    }else{
                        user.getParameters().put("advicecensored", true);
                        Users.update(user);
                        send(message, 0);
                        return;
                    }*/
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message(advice(false))
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 2:
                    user = UsersTable.findById(message.getFromId());
                    var options = user.getFields().get("advice").getAsJsonObject();
                    var update = options.get("update").getAsBoolean();
                    options.addProperty("update", !update);
                    user.update();
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message(update
                                    ? "Подписка деактивирована."
                                    : "Подписка активирована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                /*case 4:
                    user = UsersTable.find(message.getFromId());
                    user.getParameters().put("advicecensored", "true");
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Цензура включена.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 5:
                    user = UsersTable.find(message.getFromId());
                    user.getParameters().put("advicecensored", "false");
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Цензура выключена.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;*/
                default:
                    break;
            }
        }catch (ApiException | ClientException | IOException e){
            LOG.error(e);
        }
    }

    public static String advice(boolean censored) throws IOException {
        URL url;
        if(censored){
            url = new URL("http://fucking-great-advice.ru/api/random/censored");
        }else{
            url = new URL("http://fucking-great-advice.ru/api/random");
        }
        String json =  new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("text").getAsString();
    }

}
