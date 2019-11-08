/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.database.tables.Users;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.mvv.bots.vk.Config;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author I1PABIJJA
 */
public class Advice implements Script{

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
        return AccessMode.FORALL;
    }

    @Override
    public void update() {
        Users.findAll().forEach(user -> {
            if(user.getParameters().has("adviceupdate")){
                boolean b = Boolean.parseBoolean(user.getParameters().get("adviceupdate"));
                if(b){
                    Message m = new Message();
                    m.setFromId(user.getId());
                    m.setPeerId(user.getId());
                    send(m, 0);
                }
            }
        });
        return;
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
                    Users.User user = Users.find(message.getFromId());
                    keyboard.setInline(true);
                    if(user.getParameters().has("adviceupdate")){
                        if(Boolean.parseBoolean(user.getParameters().get("adviceupdate"))){
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 3 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Отписаться"))
                            ));
                        }else{
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 2 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Подписаться"))
                            ));
                        }
                    }else{
                        user.getParameters().put("adviceupdate", false);
                        Users.update(user);
                        send(message, 0);
                        return;
                    }
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message(advice())
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 2:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("adviceupdate", "true");
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Подписка активирована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                case 3:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("adviceupdate", "false");
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Подписка деактивирована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
            }
        }catch (ApiException | ClientException | IOException e){
            LOG.error(e);
        }
    }

    public static String advice() throws IOException {
        URL url = new URL("http://fucking-great-advice.ru/api/random");
        String json =  new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("text").getAsString();
    }

}
