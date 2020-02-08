/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.users.User;
import com.mvv.bots.vk.database.tables.users.UsersTable;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Weather implements Script {

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
        return smile()+"["+ key()+"] - прогноз погоды. (Powered by Dark Sky)";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.USER;
    }

    @Override
    public void update() {
        UsersTable.findAll().forEach(user -> {
            if(user.getParameters().has("weather")){
                var options = new JsonParser().parse(user.getParameters().get("weather")).getAsJsonObject();
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

            switch (step){
                case -1:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 0)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Описание")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 0:
                    User user = UsersTable.find(message.getFromId());
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
                        var temperature = currently.get("temperature").getAsFloat();
                        var apparentTemperature = currently.get("apparentTemperature").getAsFloat();
                        var visibility = currently.get("visibility").getAsFloat();
                        var pressure  = currently.get("pressure").getAsFloat()/1.333;
                        var windSpeed = currently.get("windSpeed").getAsFloat();
                        String windBearing = (int)windSpeed == 0 ? ""
                                : (int)currently.get("windBearing").getAsFloat() <= 30 ? "С"
                                : (int)currently.get("windBearing").getAsFloat() <= 60 ? "СВ"
                                : (int)currently.get("windBearing").getAsFloat() <= 120 ? "В"
                                : (int)currently.get("windBearing").getAsFloat() <= 150 ? "ЮВ"
                                : (int)currently.get("windBearing").getAsFloat() <= 210 ? "Ю"
                                : (int)currently.get("windBearing").getAsFloat() <= 240 ? "ЮЗ"
                                : (int)currently.get("windBearing").getAsFloat() <= 300 ? "З"
                                : (int)currently.get("windBearing").getAsFloat() <= 330 ? "СЗ"
                                : "С";


                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", getClass().getName())
                                                        .put("step", 1)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.LOCATION))
                        ));
                        if(user.getParameters().has("weather")){
                            var options = new JsonParser().parse(user.getParameters().get("weather")).getAsJsonObject();
                            var update = options.get("update").getAsBoolean();
                            var full = options.get("full").getAsBoolean();
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(full
                                                    ? KeyboardButtonColor.NEGATIVE
                                                    : KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    new Payload()
                                                            .put("script", getClass().getName())
                                                            .put("step", 3)
                                                            .toString()
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel(full
                                                            ? "Кратко"
                                                            : "Подробно"))
                            ));
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
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel(update
                                                            ? "Отписаться"
                                                            : "Подписаться"))
                            ));

                            String info;
                            if(full){
                                info = summary
                                        +"\n"
                                        +temperature+"˚C"
                                        +"\n"
                                        +"Ощущается как "+ apparentTemperature+"˚C"
                                        +"\n"
                                        +"Ветер "+(windSpeed==0?"":windBearing+" ")+windSpeed+"м/с"
                                        +"\n"
                                        +"Давление "+ pressure+"мм рт. ст."
                                        +"\n"
                                        +"Видимость "+ visibility+"км"
                                        +"\n"
                                        +"Powered by Dark Sky";
                            }else{
                                info = summary
                                        +"\n"
                                        +temperature+"˚C"
                                        +"\n"
                                        +"Powered by Dark Sky";
                            }
                            new Messages(Config.VK())
                                    .send(Config.GROUP)
                                    .message(info)
                                    .keyboard(keyboard)
                                    .peerId(message.getPeerId())
                                    .randomId(Utils.getRandomInt32())
                                    .execute();
                        }else{
                            JsonObject object = new JsonObject();
                            object.addProperty("update", false);
                            object.addProperty("full", false);
                            user.getParameters().put("weather", object);
                            UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                            send(message, 0);
                            return;
                        }
                    }else {
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", getClass().getName())
                                                        .put("step", 1)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.LOCATION))
                        ));
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Местоположение")
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 1:
                    String geo = message.getGeo().toString();
                    user = UsersTable.find(message.getFromId());
                    user.getParameters().put("geo", geo);
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Местоположение сохранено.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 2:
                    user = UsersTable.find(message.getFromId());
                    var options = new JsonParser().parse(user.getParameters().get("weather")).getAsJsonObject();
                    var update = options.get("update").getAsBoolean();
                    options.addProperty("update", !update);
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
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
                case 3:
                    user = UsersTable.find(message.getFromId());
                    options = new JsonParser().parse(user.getParameters().get("weather")).getAsJsonObject();
                    var full = options.get("full").getAsBoolean();
                    options.addProperty("full", !full);
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message(full
                                    ? "Кратко."
                                    : "Подробно.")
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
