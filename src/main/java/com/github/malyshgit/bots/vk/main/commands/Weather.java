/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.malyshgit.bots.vk.main.commands;

import com.github.malyshgit.bots.vk.Config;
import com.github.malyshgit.bots.vk.database.dao.UsersTable;
import com.github.malyshgit.bots.vk.database.models.User;
import com.github.malyshgit.bots.vk.main.Command;
import com.github.malyshgit.bots.vk.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.github.malyshgit.bots.vk.main.AccessMode;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Weather implements Command {

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
        if(LocalDateTime.now().getMinute() >= 30) return;
        UsersTable.findAll().forEach(user -> {
            if(user.getFields().containsKey("weather")){
                var options = user.getFields().get("weather").getAsJsonObject();
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
                    User user = UsersTable.findById(message.getFromId());
                    if(user.getFields().containsKey("geo")){
                        JsonObject jelement = user.getFields().get("geo").getAsJsonObject();
                        JsonObject coordinates = jelement.get("coordinates").getAsJsonObject();
                        float lat = coordinates.get("latitude").getAsFloat();
                        float lon = coordinates.get("longitude").getAsFloat();
                        String url = String
                                .format("https://api.darksky.net/forecast/%s/%f,%f?lang=ru&units=si", Config.DARKSKY_API_KEY, lat, lon);
                        String weather = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                        jelement = new JsonParser().parse(weather).getAsJsonObject();
                        JsonObject currently = jelement.get("currently").getAsJsonObject();
                        String summary = currently.get("summary").getAsString();
                        var icon = "";
                        switch (currently.get("icon").getAsString()){
                            case "clear-day":
                                icon = "☀";
                            case "clear-night":
                                icon = "\uD83C\uDF19";
                            case "rain":
                                icon = "\uD83C\uDF27";
                            case "snow":
                                icon = "\uD83C\uDF28";
                            case "sleet":
                                icon = "\uD83C\uDF27\uD83C\uDF28";
                            case "wind":
                                icon = "\uD83C\uDF2C";
                            case "fog":
                                icon = "\uD83C\uDF2B";
                            case "cloudy":
                                icon = "☁";
                            case "partly-cloudy-day":
                                icon = "⛅";
                            case "partly-cloudy-night":
                                icon = "\uD83C\uDF25";
                                break;
                            default:
                                break;
                        }
                        //clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
                        var temperature = currently.get("temperature").getAsFloat();
                        var apparentTemperature = currently.get("apparentTemperature").getAsFloat();
                        var visibility = currently.get("visibility").getAsFloat();
                        var pressure  = String.format("%.2f", currently.get("pressure").getAsFloat()/1.333);
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
                        if(user.getFields().containsKey("weather")){
                            var options = user.getFields().get("weather").getAsJsonObject();
                            var update = options.get("update").getAsBoolean();
                            var full = options.get("full").getAsBoolean();
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.PRIMARY)
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
                                info = icon+summary
                                        +" "
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
                                info = icon+summary
                                        +" "
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
                            user.getFields().put("weather", object);
                            user.update();
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
                    var geo = message.getGeo().toString();
                    user = UsersTable.findById(message.getFromId());
                    user.getFields().put("geo", new JsonParser().parse(geo).getAsJsonObject());
                    user.update();
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Местоположение сохранено.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 2:
                    user = UsersTable.findById(message.getFromId());
                    var options = user.getFields().get("weather").getAsJsonObject();
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
                case 3:
                    user = UsersTable.findById(message.getFromId());
                    options = user.getFields().get("weather").getAsJsonObject();
                    var full = options.get("full").getAsBoolean();
                    options.addProperty("full", !full);
                    user.update();
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
