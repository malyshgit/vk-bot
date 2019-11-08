/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.Users;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.actions.Upload;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.responses.GetByIdResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author I1PABIJJA
 */
public class InstaGet implements Script{

    @Override
    public String smile(){
        return "\uD83D\uDCF8";
    }

    @Override
    public String key(){
        return "инстагет";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - рандомное фото по тегу.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.FORALL;
    }

    @Override
    public void update() {
        /*Users.findAll().forEach(user -> {
            if(user.getParameters().has("weatherupdate")){
                boolean b = Boolean.parseBoolean(user.getParameters().get("weatherupdate"));
                if(b){
                    Message m = new Message();
                    m.setFromId(user.getId());
                    m.setPeerId(user.getId());
                    send(m, 0);
                }
            }
        });*/
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
                            .message("Инстагет - описание")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 0:
                    Users.User user = Users.find(message.getFromId());
                    List<Photo> attach = new ArrayList<>();
                    if(user.getParameters().has("instaget")){
                        LOG.debug(1);
                        String instaget = (String)user.getParameters().get("instaget");
                        LOG.debug(2);
                        JsonElement jelement = new JsonParser().parse(instaget);
                        LOG.debug(3);
                        JsonObject  jobject = jelement.getAsJsonObject();
                        LOG.debug(4);
                        JsonArray tags = jobject.get("tags").getAsJsonArray();
                        LOG.debug(instaget);
                        PhotoUpload photoUpload = new Photos(Config.VK).getMessagesUploadServer(Config.GROUP).peerId(message.getPeerId()).execute();
                        tags.forEach(e -> {
                            String url = String
                                    .format("https://www.instagram.com/explore/tags/%s/?__a=1", e.getAsString());
                            try {
                                File photo = new File("temp.jpg");
                                FileUtils.copyURLToFile(new URL(url), photo);
                                MessageUploadResponse messageUploadResponse = new Upload(Config.VK).photoMessage(photoUpload.getUploadUrl().toString(), photo).execute();
                                Files.deleteIfExists(photo.toPath());
                                List<Photo> photos = new Photos(Config.VK)
                                        .saveMessagesPhoto(Config.GROUP, messageUploadResponse.getPhoto())
                                        .server(messageUploadResponse.getServer())
                                        .hash(messageUploadResponse.getHash()).execute();
                                attach.addAll(photos);
                            } catch (IOException | ApiException | ClientException ex) {
                                LOG.error(ex);
                            }
                        });

                        keyboard.setInline(true);
                        if(user.getParameters().has("instagetupdate")){
                            if(Boolean.parseBoolean(user.getParameters().get("instagetupdate"))){
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
                            user.getParameters().put("instagetupdate", false);
                            Users.update(user);
                            send(message, 0);
                            return;
                        }
                        if(!attach.isEmpty()){
                            new Messages(Config.VK)
                                    .send(Config.GROUP)
                                    .attachment(attach.stream().map(photo -> {
                                        return "photo"+photo.getOwnerId()+"_"+photo.getId();
                                    }).collect(Collectors.joining(",")))
                                    .message(tags.toString())
                                    .keyboard(keyboard)
                                    .peerId(message.getPeerId())
                                    .randomId(Utils.getRandomInt32())
                                    .execute();
                        }else{
                            new Messages(Config.VK)
                                    .send(Config.GROUP)
                                    .message(tags.toString())
                                    .keyboard(keyboard)
                                    .peerId(message.getPeerId())
                                    .randomId(Utils.getRandomInt32())
                                    .execute();
                        }
                    }else {
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.PRIMARY)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 1 + "}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Тэги"))
                        ));
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Инстагет - местоположение")
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 1:
                    GetByIdResponse getByIdResponse = new Messages(Config.VK).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    String tags = getByIdResponse.getItems().get(0).getText();
                    LOG.debug(tags);
                    user = Users.find(message.getFromId());
                    JsonArray array = new JsonArray();
                    List.of(tags.split(",")).forEach(array::add);
                    user.getParameters().put("instaget", array.toString());
                    Users.update(user);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Тэги сохранены.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                case 2:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("instagetupdate", "true");
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
                    user.getParameters().put("instagetupdate", "false");
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
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }
    
}
