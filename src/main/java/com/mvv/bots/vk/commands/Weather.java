/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.Users;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author I1PABIJJA
 */
public class Weather implements Script{

    @Override
    public String smile(){
        return "⛅";
    }

    @Override
    public String key(){
        return "погода";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - прогноз погоды.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.FORALL;
    }

    @Override
    public void update() {
        Users.findAll().forEach(user -> {
            if(user.getParameters().has("weatherupdate")){
                boolean b = Boolean.parseBoolean(user.getParameters().get("weatherupdate"));
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

            switch (step){
                case -1:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+0+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Погода - описание")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 0:
                    Users.User user = Users.find(message.getFromId());
                    if(user.getParameters().has("geo")){
                        String geo = (String)user.getParameters().get("geo");
                        JsonElement jelement = new JsonParser().parse(geo);
                        JsonObject  jobject = jelement.getAsJsonObject();
                        JsonObject coordinates = jobject.get("coordinates").getAsJsonObject();
                        LOG.debug(geo);
                        float lat = coordinates.get("latitude").getAsFloat();
                        float lon = coordinates.get("longitude").getAsFloat();
                        String url = String
                                .format("https://api.darksky.net/forecast/%s/%f,%f?lang=ru&units=si", Config.DARKSKY_API_KEY, lat, lon);
                        String weather = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                        jelement = new JsonParser().parse(weather);
                        jobject = jelement.getAsJsonObject();
                        JsonObject currently = jobject.get("currently").getAsJsonObject();
                        String summary = currently.get("summary").getAsString();
                        int temperature = (int)currently.get("temperature").getAsFloat();
                        String info = summary+"\n"+temperature+"˚C";
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 1 + "}"
                                        ).setType(KeyboardButtonActionType.LOCATION))
                        ));
                        if(user.getParameters().has("weatherupdate")){
                            if(Boolean.parseBoolean(user.getParameters().get("weatherupdate"))){
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
                            user.getParameters().put("weatherupdate", false);
                            Users.save(user);
                            send(message, 0);
                            return;
                        }
						new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message(info)
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else {
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 1 + "}"
                                        ).setType(KeyboardButtonActionType.LOCATION))
                        ));
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Погода - местоположение")
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 1:
                    String geo = message.getGeo().toString();
                    user = Users.find(message.getFromId());
                    user.getParameters().put("geo", geo);
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Местоположение сохранено.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                case 2:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("weatherupdate", "true");
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Подписка на погоду активирована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                case 3:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("weatherupdate", "false");
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Подписка на погоду деактивирована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException | IOException e){
            LOG.error(e);
        }
    }
    
}
