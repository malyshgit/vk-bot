/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.settings.Option;
import com.mvv.bots.vk.database.tables.settings.Settings;
import com.mvv.bots.vk.database.tables.users.User;
import com.mvv.bots.vk.database.tables.users.Users;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.*;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Image;
import com.vk.api.sdk.objects.base.UploadServer;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.responses.OwnerCoverUploadResponse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Administration implements Script {

    @Override
    public String smile(){
        return "⚙";
    }

    @Override
    public String key(){
        return "управление";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - управление сообществом.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.ADMIN;
    }

    @Override
    public void update() {
        Option autoposting = Settings.find("autoposting");
        if(autoposting == null) return;
        if (!Boolean.parseBoolean(autoposting.getValue())) return;
        var autopostingTasks = Settings.find("autopostingtasks");
        if(autopostingTasks == null) return;
        JsonArray tasks = new JsonParser().parse(autopostingTasks.getValue()).getAsJsonArray();
        tasks.forEach(taskElement -> {
            var task = taskElement.getAsJsonObject();
            String name = task.get("name").getAsString();
            int count = task.get("count").getAsInt();
            String url = task.get("albumurl").getAsString();
            if (url.startsWith("http") || url.startsWith("vk.com")) {
                url = url.replace("https://vk.com/", "");
                url = url.replace("http://vk.com/", "");
                url = url.replace("vk.com/", "");
            }
            if(url.startsWith("album")) url = url.replace("album", "");
            String[] urlParts = url.split("_");
            int ownerId = Integer.parseInt(urlParts[0]);
            int albumId = Integer.parseInt(urlParts[1]);
            int offset = 0;
            if(task.has("offset")){
                offset = task.get("offset").getAsInt();
            }
            User user = Users.find(Config.ADMIN_ID);
            UserActor admin = new UserActor(Config.ADMIN_ID, user.getToken());
            try {
                var size = new Photos(Config.VK()).get(admin)
                        .albumId(urlParts[1])
                        .ownerId(ownerId)
                        .count(1)
                        .offset(0)
                        .execute().getCount();
                if(offset+1 >= size) return;
                var res = new Photos(Config.VK()).get(admin)
                        .albumId(urlParts[1])
                        .ownerId(ownerId)
                        .count(count)
                        .offset(offset)
                        .photoSizes(true)
                        .execute();
                var attaches = res.getItems().stream().map(p -> {
                    /*if(p.getOwnerId() != user.getId()){
                        return "photo"+p.getOwnerId()+"_"+p.getId()+"_"+p.getAccessKey();
                    }else{
                        return "photo"+p.getOwnerId()+"_"+p.getId();
                    }*/
                    return "photo"+p.getOwnerId()+"_"+p.getId();
                }).collect(Collectors.joining(","));
                new Wall(Config.VK()).post(admin)
                        .ownerId(Config.GROUP_ID)
                        .attachments(attaches)
                        .fromGroup(true)
                        .execute();
                task.addProperty("offset", offset+count);
                Settings.update(new Option("autopostingtasks", tasks.toString()));
            } catch (ApiException | ClientException e) {
                LOG.error(e);
            }
        });
    }

    @Override
    public void send(Message message, Integer step) {
        try {
            if(message.getPeerId() != Config.ADMIN_ID){
                return;
            }

            Keyboard keyboard = new Keyboard();

            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);

            switch (step){
                case 0:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Стена"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+ScriptList.class.getName()+"\"," +
                                                    "\"step\":"+1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Управление сообществом")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1_1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Автопостинг"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Стена")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1:
                    keyboard.setInline(true);
                    Option autoposting = Settings.find("autoposting");
                    if(autoposting != null){
                        if(Boolean.parseBoolean(autoposting.getValue())){
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 1_1_1 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Включен"))
                            ));
                        }else{
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 1_1_1 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Выключен"))
                            ));
                        }
                    }else{
                        autoposting = new Option("autoposting", "false");
                        Settings.add(autoposting);
                        send(message, 1_1);
                        return;
                    }
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1_1_2+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Задания"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Автопостинг")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1_1:
                    autoposting = Settings.find("autoposting");
                    autoposting = new Option("autoposting", Boolean.parseBoolean(autoposting.getValue())==true?"false":"true");
                    Settings.update(autoposting);
                    send(message, 1_1);
                    break;
                case 1_1_2:
                    var autopostingTasks = Settings.find("autopostingtasks");
                    if(autopostingTasks == null){
                        autopostingTasks = new Option("autopostingtasks", "[]");
                        Settings.add(autopostingTasks);
                    }
                    JsonArray tasks = new JsonParser().parse(autopostingTasks.getValue()).getAsJsonArray();

                    if(tasks.size() > 0) {
                        int offset = 0;
                        var payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                        if(payload.has("offset")){
                            offset = payload.get("offset").getAsInt();
                        }
                        int maxTaskButtons = 8;
                        if(tasks.size() <= 10) maxTaskButtons = 10;
                        int nextOffset = offset;

                        int n = 0;
                        for(int i = offset; i < tasks.size(); i++){
                            var task = tasks.get(i).getAsJsonObject();
                            var name = task.get("name").getAsString();
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.DEFAULT)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\""+getClass().getName()+"\"," +
                                                            "\"step\":"+1_1_2_3+"}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel(name))
                            ));
                            nextOffset++;
                            n++;
                            if(n >= maxTaskButtons){
                                break;
                            }
                        }
                        keyboard.setInline(true);
                        var btns = new ArrayList<KeyboardButton>();
                        if(offset == 0){
                            if(maxTaskButtons == 8){
                                btns.add(new KeyboardButton()
                                        .setColor(KeyboardButtonColor.DEFAULT)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\""+getClass().getName()+"\"," +
                                                        "\"step\":"+1_1_2_2+"}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel(">")));
                            }
                        }else if(offset >= 8){
                            btns.add(new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1_1_2_1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("<")));
                            if(tasks.size() - offset > 0) {
                                btns.add(new KeyboardButton()
                                        .setColor(KeyboardButtonColor.DEFAULT)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 1_1_2_2 + "}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel(">")));
                            }
                        }
                        buttons.add(btns);
                    }else{
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.DEFAULT)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\""+getClass().getName()+"\"," +
                                                        "\"step\":"+1_1_2_4+"}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Добавить"))
                        ));
                    }
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Задачи")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1_2_4:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1_1_2_4_1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Фото из альбома")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1_1_2_4+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("В разработке"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Выберите тип задачи")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1_2_4_1:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1_1_2_4_1_1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Выбрать"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Отправьте ссылку на альбом и нажмите \"Выбрать\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1_2_4_1_1:
                    var getByIdResponse = new Messages(Config.VK()).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    var url = getByIdResponse.getItems().get(0).getText();
                    if(url == null || url.isEmpty()){
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Отправьте ссылку на альбом и нажмите \"Выбрать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        break;
                    }
                    keyboard.setInline(true);
                    List<KeyboardButton> list = new ArrayList<>();
                    for(int i = 1; i <= 10; i++){
                        if(i == 6){
                            buttons.add(list);
                            list = new ArrayList<>();
                        }
                        list.add(new KeyboardButton()
                                .setColor(KeyboardButtonColor.DEFAULT)
                                .setAction(new KeyboardButtonAction().setPayload(
                                        "{\"script\":\""+getClass().getName()+"\"," +
                                                "\"url\":\""+url+"\"," +
                                                "\"count\":"+i+"," +
                                                "\"step\":"+1_1_2_4_1_2+"}"
                                ).setType(KeyboardButtonActionType.TEXT)
                                        .setLabel(""+i))
                        );
                        if(i == 10) buttons.add(list);
                    }
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Какое количество фото необходимо прикреплять?")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1_2_4_1_2:
                    var payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    url = payload.get("url").getAsString();
                    int photosCount = payload.get("count").getAsInt();
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"url\":\""+url+"\"," +
                                                    "\"count\":"+photosCount+"," +
                                                    "\"step\":"+1_1_2_4_1_3+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Сохранить"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Отправьте название задачи и нажмите \"Сохранить\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1_1_2_4_1_3:
                    getByIdResponse = new Messages(Config.VK()).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    var name = getByIdResponse.getItems().get(0).getText();
                    payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    url = payload.get("url").getAsString();
                    var count = payload.get("count").getAsInt();
                    if(name == null || name.isEmpty()){
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Отправьте название задачи и нажмите \"Сохранить\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        break;
                    }
                    JsonObject task = new JsonObject();
                    task.addProperty("albumurl", url);
                    task.addProperty("count", count);
                    task.addProperty("name", name);
                    autopostingTasks = Settings.find("autopostingtasks");
                    tasks = new JsonParser().parse(autopostingTasks.getValue()).getAsJsonArray();
                    tasks.add(task);
                    Settings.update(autopostingTasks);
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }

}
