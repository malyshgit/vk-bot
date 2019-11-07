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
                        LOG.debug(geo);
                        String[] coord = geo.split(":");
                        float lat = Float.parseFloat(coord[0]);
                        float lon = Float.parseFloat(coord[1]);
                        LOG.debug(lat+":"+lon);
                        String url = String
                                .format("https://api.darksky.net/forecast/%s/%f,%f?lang=ru&units=si", Config.DARKSKY_API_KEY, lat, lon);
                        String weather = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                        JsonElement jelement = new JsonParser().parse(weather);
                        JsonObject  jobject = jelement.getAsJsonObject();
                        JsonObject currently = jobject.get("currently").getAsJsonObject();
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
                                .message(currently.toString())
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else {
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.NEGATIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + ScriptList.class.getName() + "\"," +
                                                        "\"step\":" + 0 + "}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Назад"))
                        ));
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
                    JsonElement jelement = new JsonParser().parse(geo);
                    JsonObject  jobject = jelement.getAsJsonObject();
                    JsonObject coord = jobject.get("coordinates").getAsJsonObject();
                    float lat = coord.get("latitude").getAsFloat();
                    float lon = coord.get("longitude").getAsFloat();
                    user = Users.find(message.getFromId());
                    user.getParameters().put("geo", lat+":"+lon);
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Местоположение сохранено.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException | IOException e){
            LOG.error(e);
        }
    }
    
}
