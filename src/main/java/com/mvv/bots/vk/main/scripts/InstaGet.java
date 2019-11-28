/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.users.User;
import com.mvv.bots.vk.database.tables.users.Users;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
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

public class InstaGet implements Script {

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
        Users.findAll().forEach(user -> {
            if(user.getParameters().has("instagetupdate")){
                boolean b = Boolean.parseBoolean(user.getParameters().get("instagetupdate"));
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
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Инстагет - описание")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 0:
                    User user = Users.find(message.getFromId());
                    List<Photo> attach = new ArrayList<>();
                    if(user.getParameters().has("instagettags")){
                        String instagettags = (String)user.getParameters().get("instagettags");
                        JsonElement jelement = new JsonParser().parse(instagettags);
                        JsonArray  tags = jelement.getAsJsonArray();
                        PhotoUpload photoUpload = new Photos(Config.VK()).getMessagesUploadServer(Config.GROUP).peerId(message.getPeerId()).execute();
                        tags.forEach(e -> {
                            String url = String
                                    .format("https://www.instagram.com/explore/tags/%s/?__a=1", e.getAsString());
                            try {
                                String json = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                                JsonElement el = new JsonParser().parse(json);
                                JsonObject el1 = el.getAsJsonObject();
                                JsonObject el2 = el1.get("graphql").getAsJsonObject();
                                JsonObject el3 = el2.get("hashtag").getAsJsonObject();
                                JsonObject el4 = el3.get("edge_hashtag_to_media").getAsJsonObject();
                                JsonArray el5 = el4.get("edges").getAsJsonArray();
                                JsonObject el6 = el5.get(0).getAsJsonObject();
                                JsonObject el7 = el6.get("node").getAsJsonObject();
                                String phurl = el7.get("display_url").getAsString();
                                File photo = new File("temp.jpg");
                                FileUtils.copyURLToFile(new URL(phurl), photo);
                                MessageUploadResponse messageUploadResponse = new Upload(Config.VK())
                                        .photoMessage(photoUpload.getUploadUrl().toString(), photo).execute();
                                Files.deleteIfExists(photo.toPath());
                                List<Photo> photos = new Photos(Config.VK())
                                        .saveMessagesPhoto(Config.GROUP, messageUploadResponse.getPhoto())
                                        .server(messageUploadResponse.getServer())
                                        .hash(messageUploadResponse.getHash()).execute();
                                attach.addAll(photos);
                            } catch (IOException | ApiException | ClientException ex) {
                                LOG.error(ex);
                            }
                        });

                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.PRIMARY)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 1 + "}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Изменить теги"))
                        ));
                        if(user.getParameters().has("instagetupdate")){
                            if(Boolean.parseBoolean(user.getParameters().get("instagetupdate"))){
                                buttons.add(List.of(
                                        new KeyboardButton()
                                                .setColor(KeyboardButtonColor.NEGATIVE)
                                                .setAction(new KeyboardButtonAction().setPayload(
                                                        "{\"script\":\"" + getClass().getName() + "\"," +
                                                                "\"step\":" + 22 + "}"
                                                ).setType(KeyboardButtonActionType.TEXT)
                                                        .setLabel("Отписаться"))
                                ));
                            }else{
                                buttons.add(List.of(
                                        new KeyboardButton()
                                                .setColor(KeyboardButtonColor.POSITIVE)
                                                .setAction(new KeyboardButtonAction().setPayload(
                                                        "{\"script\":\"" + getClass().getName() + "\"," +
                                                                "\"step\":" + 21 + "}"
                                                ).setType(KeyboardButtonActionType.TEXT)
                                                        .setLabel("Подписаться"))
                                ));
                            }
                        }else{
                            user.getParameters().put("instagetupdate", false);
                            Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                            send(message, 0);
                            return;
                        }
                        if(!attach.isEmpty()){
                            new Messages(Config.VK())
                                    .send(Config.GROUP)
                                    .attachment(attach.stream().map(photo -> {
                                        return "photo"+photo.getOwnerId()+"_"+photo.getId();
                                    }).collect(Collectors.joining(",")))
                                    .message(tags.toString()
                                            .replace("[", "")
                                            .replace("]", "")
                                            .replace("\"", "")
                                    )
                                    .keyboard(keyboard)
                                    .peerId(message.getPeerId())
                                    .randomId(Utils.getRandomInt32())
                                    .execute();
                        }else{
                            new Messages(Config.VK())
                                    .send(Config.GROUP)
                                    .message(tags.toString()
                                            .replace("[", "")
                                            .replace("]", "")
                                            .replace("\"", "")
                                    )
                                    .keyboard(keyboard)
                                    .peerId(message.getPeerId())
                                    .randomId(Utils.getRandomInt32())
                                    .execute();
                        }
                    }else {
                        send(message, 1);
                    }
                    break;
                case 1:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 11 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Сохранить"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 0 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Отмена"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Отправьте теги через запятую и нажмите \"Сохранить\".")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 11:
                    GetByIdResponse getByIdResponse = new Messages(Config.VK()).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    String tags = getByIdResponse.getItems().get(0).getText().replaceAll("\\s+", "");
                    LOG.debug(tags);
                    user = Users.find(message.getFromId());
                    JsonArray array = new JsonArray();
                    List.of(tags.split(",")).forEach(array::add);
                    user.getParameters().put("instagettags", array.toString());
                    Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Тэги сохранены.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                case 21:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("instagetupdate", "true");
                    Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Подписка активирована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    ScriptList.open(message);
                    break;
                case 22:
                    user = Users.find(message.getFromId());
                    user.getParameters().put("instagetupdate", "false");
                    Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
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
