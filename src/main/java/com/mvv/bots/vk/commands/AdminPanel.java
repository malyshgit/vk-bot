/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.Settings;
import com.mvv.bots.vk.database.tables.Users;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.*;
import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.RequiredFieldException;
import com.vk.api.sdk.objects.base.*;
import com.vk.api.sdk.objects.base.Image;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.docs.responses.DocUploadResponse;
import com.vk.api.sdk.objects.docs.responses.SaveResponse;
import com.vk.api.sdk.objects.enums.DocsType;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.messages.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.google.gson.*;
import com.vk.api.sdk.objects.messages.responses.GetByIdResponse;
import com.vk.api.sdk.objects.pages.PrivacySettings;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbum;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.GetAlbumsResponse;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.responses.OwnerCoverUploadResponse;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;

public class AdminPanel implements Script{

    @Override
    public String smile(){
        return "⚙";
    }

    @Override
    public String key(){
        return "настройки";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - админ панель.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.ADMIN;
    }

    @Override
    public void update() {
        return;
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
                case -1:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+ScriptList.class.getName()+"\"," +
                                                    "\"step\":"+1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+0+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Опции"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Меню")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 0:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+ScriptList.class.getName()+"\"," +
                                                    "\"step\":"+1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Базы данных")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+2+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Переключатели"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+3+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Авторизация")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+4+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Обновление")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+5+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Утилиты"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Меню")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+0+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+10+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Список")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+11+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Пересоздать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Меню")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 10:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+101+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Настройки"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+102+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Пользователи"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Список")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 101:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1011+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Показать"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1012+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Пересоздать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("База данных \"Настройки\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1011:
                    List<Settings.Option> options = Settings.findAll();
                    if(!options.isEmpty()){
                        String info = options.stream().map(Settings.Option::toString).collect(Collectors.joining("\n"));
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message(info)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 1012:
                    Settings.create();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("База данных пересоздана.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 102:
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1021+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Показать"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+1022+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Пересоздать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("База данных \"Пользователи\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1021:
                    List<Users.User> users = Users.findAll();
                    if(!users.isEmpty()){
                        String info = users.stream().map(Users.User::toString).collect(Collectors.joining("\n"));
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message(info)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 1022:
                    Users.create();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("База данных пересоздана.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 11:
                    Users.create();
                    Settings.create();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Базы данных пересозданы.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 2:
                    Settings.Option debbug = Settings.find("debbug");
                    buttons.add(List.of(
                            new KeyboardButton()
                            .setColor(KeyboardButtonColor.NEGATIVE)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\""+getClass().getName()+"\"," +
                                            "\"step\":"+0+"}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Назад"))
                    ));
                    if(debbug != null){
                        if(Boolean.parseBoolean(debbug.getValue())){
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 212 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Отладка"))
                            ));
                        }else{
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 211 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Отладка"))
                            ));
                        }
                    }else{
                        debbug = new Settings.Option("debbug", "false");
                        Settings.add(debbug);
                        send(message, 2);
                        return;
                    }
                    Settings.Option update = Settings.find("update");
                    if(update != null){
                        if(Boolean.parseBoolean(update.getValue())){
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 222 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Обновление"))
                            ));
                        }else{
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 221 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Обновление"))
                            ));
                        }
                    }else{
                        update = new Settings.Option("update", "false");
                        Settings.add(update);
                        send(message, 2);
                        return;
                    }
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Переключатели")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 211:
                    debbug = Settings.find("debbug");
                    debbug.setValue("true");
                    Settings.update(debbug);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отладка включена.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 2);
                    break;
                case 212:
                    debbug = Settings.find("debbug");
                    debbug.setValue("false");
                    Settings.update(debbug);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отладка отключена.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 2);
                    break;
                case 221:
                    update = Settings.find("update");
                    update.setValue("true");
                    Settings.update(update);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Обновление включено.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 2);
                    break;
                case 222:
                    update = Settings.find("update");
                    update.setValue("false");
                    Settings.update(update);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Обновление отключено.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 2);
                    break;
                case 3:
                    var url = String.format("https://oauth.vk.com/authorize?client_id=%d&display=page&redirect_uri=%s&scope=groups,docs,offline,photos,wall&response_type=code&v=5.103",
                    Config.APP_ID, Config.REDIRECT_URL);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Перейдите по ссылке:\n"+url)
                            .peerId(message.getPeerId())
                            .dontParseLinks(false)
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 4:
                    drawCover();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Обложка перерисована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 5:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 0 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 51 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Заполнение альбомов"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 52 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Парсинг сообществ"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Действия со списком ссылок")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 51:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 5 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 511 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Альбомы группы"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 512 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Альбомы админа"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Выберите какие альбомы заполнить")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 511:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 51 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 5111 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Начать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .keyboard(keyboard)
                            .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 5111:
                    GetByIdResponse getByIdResponse = new Messages(Config.VK).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    List<MessageAttachment> attachments = getByIdResponse.getItems().get(0).getAttachments();
                    if(attachments.isEmpty()){
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    if(attachments.get(0).getType().equals(MessageAttachmentType.DOC)) {
                        url = attachments.get(0).getDoc().getUrl();
                        List<String> lines = IOUtils.readLines(new URL(url).openStream(), StandardCharsets.UTF_8);
                        pushPhotos(true, lines);
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.NEGATIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 51111 + "}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Остановить"))
                        ));
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .keyboard(keyboard)
                                .message("В любой момент можно остановить процесс")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else{
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 512:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 51 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")),
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 5121 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Начать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .keyboard(keyboard)
                            .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 5121:
                    getByIdResponse = new Messages(Config.VK).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    attachments = getByIdResponse.getItems().get(0).getAttachments();
                    if(attachments.isEmpty()){
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    if(attachments.get(0).getType().equals(MessageAttachmentType.DOC)) {
                        url = attachments.get(0).getDoc().getUrl();
                        List<String> lines = IOUtils.readLines(new URL(url).openStream(), StandardCharsets.UTF_8);
                        pushPhotos(false, lines);
                        keyboard.setInline(true);
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.NEGATIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                "{\"script\":\"" + getClass().getName() + "\"," +
                                                        "\"step\":" + 51111 + "}"
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Остановить"))
                        ));
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .keyboard(keyboard)
                                .message("В любой момент можно остановить процесс")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else{
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                case 51111:
                    threadStarted = false;
                    break;
                case 52:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 5 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 521 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Начать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отправьте ID сообщества и нажмите \"Начать\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 521:
                    getByIdResponse = new Messages(Config.VK).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    url = getByIdResponse.getItems().get(0).getText();
                    if(url == null || url.isEmpty()){
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте ID сообщества и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    parseWall(url);
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 51111 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Остановить"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .keyboard(keyboard)
                            .message("В любой момент можно остановить процесс")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 56:
                    /*getByIdResponse = new Messages(Config.VK).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    attachments = getByIdResponse.getItems().get(0).getAttachments();
                    if(attachments.isEmpty()){
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    if(attachments.get(0).getType().equals(MessageAttachmentType.DOC)){
                        URL url = attachments.get(0).getDoc().getUrl();
                        List<String> lines = IOUtils.readLines(url.openStream(), StandardCharsets.UTF_8);
                        File dir = new File("./tmp");
                        if (!dir.exists()){
                            dir.mkdirs();
                        }
                        lines.forEach(s -> {
                            try {
                                long size = FileUtils.sizeOfDirectory(dir);
                                LOG.debug(size);
                                if(size > 48*1024*1024){
                                    File file = new File(System.currentTimeMillis()+".zip");
                                    pack(dir.getPath(), file.getPath());
                                    UploadServer uploadServer = new Docs(Config.VK).getMessagesUploadServer(Config.GROUP).peerId(message.getPeerId()).execute();
                                    DocUploadResponse messageUploadResponse = new Upload(Config.VK)
                                            .doc(uploadServer.getUploadUrl().toString(), file).execute();
                                    SaveResponse doc = new Docs(Config.VK)
                                            .save(Config.GROUP, messageUploadResponse.getFile())
                                            .title(FilenameUtils.getName(file.getPath())).execute();
                                    Files.deleteIfExists(file.toPath());
                                    FileUtils.deleteDirectory(dir);
                                    dir.mkdirs();
                                    new Messages(Config.VK)
                                            .send(Config.GROUP)
                                            .attachment("doc"+doc.getDoc().getOwnerId()+"_"+doc.getDoc().getId())
                                            .peerId(message.getPeerId())
                                            .randomId(Utils.getRandomInt32())
                                            .execute();
                                }
                                URL urlS = new URL(s);
                                String name = System.currentTimeMillis()+"."+FilenameUtils.getExtension(urlS.getPath());
                                LOG.debug(name);
                                File fileS = new File(dir, name);
                                FileUtils.copyURLToFile(
                                        urlS,
                                        fileS);
                            } catch (IOException | ApiException | ClientException e) {
                                LOG.error(e);
                            }
                        });
                        if(dir.listFiles().length < 1) break;
                        File file = new File(System.currentTimeMillis()+".zip");
                        pack(dir.getPath(), file.getPath());
                        UploadServer uploadServer = new Docs(Config.VK).getMessagesUploadServer(Config.GROUP).peerId(message.getPeerId()).execute();
                        DocUploadResponse messageUploadResponse = new Upload(Config.VK)
                                .doc(uploadServer.getUploadUrl().toString(), file).execute();
                        SaveResponse doc = new Docs(Config.VK)
                                .save(Config.GROUP, messageUploadResponse.getFile())
                                .title(FilenameUtils.getName(file.getPath())).execute();
                        Files.deleteIfExists(file.toPath());
                        FileUtils.deleteDirectory(dir);
                        dir.mkdirs();
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .attachment("doc"+doc.getDoc().getOwnerId()+"_"+doc.getDoc().getId())
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else{
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте файл со ссылками и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }*/
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException | IOException e){
            LOG.error(e);
        }
    }

    private static boolean threadStarted = false;
    private static void parseWall(String domain) {
        if(threadStarted) return;
        new Thread(() -> {
            threadStarted = true;
            parseWallThread(domain);
            threadStarted = false;
        }).start();
    }
    private static void pushPhotos(boolean isGroup, List<String> urls) {
        if(threadStarted) return;
        new Thread(() -> {
            threadStarted = true;
            pushPhotosThread(isGroup, urls);
            threadStarted = false;
        }).start();
    }
    private static void parseWallThread(String domain){
        try {
            int offset = 0;
            int count = 100;
            var query = new Wall(Config.VK).get(Config.ADMIN)
                    .offset(offset)
                    .count(count);
            if (domain.matches("-\\d+")) {
                query.ownerId(Integer.valueOf(domain));
            } else if (domain.startsWith("http") || domain.startsWith("vk.com")) {
                domain = domain.replace("https://vk.com/", "");
                domain = domain.replace("http://vk.com/", "");
                domain = domain.replace("vk.com/", "");
                query.domain(domain);
            } else {
                query.domain(domain);
            }
            var response = query.execute();

            StringBuffer sb = new StringBuffer();

            int size = response.getCount();

            int mid = new Messages(Config.VK)
                    .send(Config.GROUP)
                    .peerId(Config.ADMIN_ID)
                    .message("Прогресс: null")
                    .randomId(Utils.getRandomInt32())
                    .execute();

            int i = 0;
            while (offset < size) {
                if(!threadStarted) break;
                i++;
                if(i > 2){
                    new Messages(Config.VK)
                            .edit(Config.GROUP, Config.ADMIN_ID, mid)
                            .message("Прогресс: "+offset+"/"+size)
                            .execute();
                    i = 0;
                }
                response.getItems().forEach(post -> {
                    if (post == null || post.getAttachments() == null) return;
                    post.getAttachments().forEach(attachment -> {
                        if (attachment.getType().equals(WallpostAttachmentType.PHOTO)) {
                            Photo photo = attachment.getPhoto();
                            var pres = photo.getSizes().stream()
                                    .max(Comparator.comparingInt(o -> o.getWidth() * o.getHeight()));
                            if (pres.isPresent()) {
                                var maxSize = pres.get();
                                String src = maxSize.getUrl();
                                sb.append(src).append("\n");
                            }
                        }
                    });
                });
                offset += count;
                response = query.offset(offset)
                        .count(count)
                        .execute();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
            new Messages(Config.VK)
                    .edit(Config.GROUP, Config.ADMIN_ID, mid)
                    .message("Прогресс: "+size+"/"+size)
                    .execute();
            File urls = new File(domain + ".txt");
            FileUtils.write(urls, sb.toString(), StandardCharsets.UTF_8);
            var upload = new Docs(Config.VK).getMessagesUploadServer(Config.GROUP).peerId(Config.ADMIN_ID).type(DocsType.DOC).execute();
            var resp = new Upload(Config.VK).doc(upload.getUploadUrl(), urls).execute();
            var save = new Docs(Config.VK).save(Config.GROUP, resp.getFile()).title(domain + ".txt").execute();
            urls.delete();
            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message(domain)
                    .attachment("doc" + save.getDoc().getOwnerId() + "_" + save.getDoc().getId())
                    .peerId(Config.ADMIN_ID)
                    .randomId(Utils.getRandomInt32())
                    .execute();
        } catch (ApiException | ClientException | IOException e) {
            LOG.error(e);
        }
    }
    private static void pushPhotosThread(boolean isGroup, List<String> urls) {
        try {
            StringBuilder sb = new StringBuilder();
            int owner = isGroup ? Config.GROUP_ID : Config.ADMIN_ID;
            GetAlbumsResponse response = new Photos(Config.VK).getAlbums(Config.ADMIN).ownerId(owner).execute();
            int offset = 0;
            List<PhotoAlbumFull> albums = new ArrayList<>();
            response.getItems().forEach(a -> {
                if(a.getTitle().startsWith("AutoAlbum_")){
                    albums.add(a);
                }
            });
            albums.sort((o1, o2) -> {
                int s1 = Integer.parseInt(o1.getTitle().replace("AutoAlbum_", ""));
                int s2 = Integer.parseInt(o2.getTitle().replace("AutoAlbum_", ""));
                return Integer.compare(s2, s1);
            });
            if(!albums.isEmpty()) sb.append("Альбомы:\n")
                    .append(albums.stream().map(a -> a.getTitle()+"("+a.getSize()+")")
                            .collect(Collectors.joining("\n")))
                    .append("\n");

            PhotoAlbumFull lastAlbum = null;

            HashSet<String> captions = new HashSet<>();

            if(!albums.isEmpty()){
                for(PhotoAlbumFull a : albums){
                    int i = 0;
                    List<AbstractQueryBuilder> queryList = new ArrayList<>();
                    while(i < a.getSize()) {
                        if(queryList.size() >= 5){
                            var json = new Execute(Config.VK).batch(Config.ADMIN, queryList).execute();
                            var array = json.getAsJsonArray();
                            array.forEach(e -> {
                                var resp = new Gson().fromJson(e, GetResponse.class);
                                resp.getItems().forEach(photo -> {
                                    captions.add(photo.getText());
                                });
                            });
                            queryList.clear();
                        }
                        var batch = new Photos(Config.VK).get(Config.ADMIN)
                                .ownerId(a.getOwnerId())
                                .albumId(String.valueOf(a.getId()))
                                .offset(i)
                                .count(1000);
                        queryList.add(batch);
                        Thread.sleep(1500);
                        i += 1000;
                    }
                    if(!queryList.isEmpty()){
                        var json = new Execute(Config.VK).batch(Config.ADMIN, queryList).execute();
                        var array = json.getAsJsonArray();
                        array.forEach(e -> {
                            var resp = new Gson().fromJson(e, GetResponse.class);
                            resp.getItems().forEach(photo -> {
                                captions.add(photo.getText());
                            });
                        });
                        queryList.clear();
                    }
                    if(a.getSize() >= 10000) continue;
                    if(lastAlbum == null) lastAlbum = a;
                }
                if(lastAlbum == null){
                    var albumQuery = new Photos(Config.VK)
                            .createAlbum(Config.ADMIN, "AutoAlbum_"+albums.size());
                    if(isGroup){
                        albumQuery.uploadByAdminsOnly(true)
                            .groupId(Math.abs(Config.GROUP_ID));
                    }else{
                        albumQuery.privacyView("only_me");
                    }
                    lastAlbum = albumQuery.execute();
                }
                offset = lastAlbum.getSize();
            }else{
                var albumQuery = new Photos(Config.VK).createAlbum(Config.ADMIN, "AutoAlbum_0");
                if(isGroup){
                    albumQuery.uploadByAdminsOnly(true)
                            .groupId(Math.abs(Config.GROUP_ID));
                }else{
                    albumQuery.privacyView("only_me");
                }
                lastAlbum = albumQuery.execute();
            }
            int autoAlbumCount = Integer.parseInt(lastAlbum.getTitle().replace("AutoAlbum_", ""))+1;

            sb.append("Текущий альбом: ")
                    .append(lastAlbum.getTitle()).append("\n");
            sb.append("Ссылок: ")
                    .append(urls.size()).append("\n");
            sb.append("Заполненно: ")
                    .append(captions.size()).append("\n");

            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message(sb.toString())
                    .peerId(Config.ADMIN_ID)
                    .randomId(Utils.getRandomInt32())
                    .execute();

            var uploadQuery = new Photos(Config.VK)
                    .getUploadServer(Config.ADMIN)
                    .albumId(lastAlbum.getId());
                    if(isGroup) uploadQuery.groupId(Math.abs(Config.GROUP_ID));
            PhotoUpload upload = uploadQuery.execute();
            int mid = new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message("Прогресс: null")
                    .peerId(Config.ADMIN_ID)
                    .randomId(Utils.getRandomInt32())
                    .execute();
            int savesCount = 0;
            int i = 0;
            for(String url : urls) {
                if(savesCount > 1000){
                    threadStarted = false;
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Заполнение остановленно.")
                            .peerId(Config.ADMIN_ID)
                            .randomId(Utils.getRandomInt32())
                            .execute();
                }
                if(!threadStarted) break;
                i++;
                if(i > 400){
                    new Messages(Config.VK)
                            .edit(Config.GROUP, Config.ADMIN_ID, mid)
                            .message("Прогресс: "+savesCount+"/"+urls.size())
                            .execute();
                    i = 0;
                }
                if(captions.contains(url)){LOG.debug("skip"); continue;}
                long startTime = System.currentTimeMillis();
                if(offset >= 10000){
                    var albumQuery = new Photos(Config.VK).createAlbum(Config.ADMIN, "AutoAlbum_"+autoAlbumCount);
                    if(isGroup){
                        albumQuery.uploadByAdminsOnly(true)
                                .groupId(Math.abs(Config.GROUP_ID));
                    }else{
                        albumQuery.privacyView("only_me");
                    }
                    lastAlbum = albumQuery.execute();
                    uploadQuery = new Photos(Config.VK)
                            .getUploadServer(Config.ADMIN)
                            .albumId(lastAlbum.getId());
                    if(isGroup) uploadQuery.groupId(Math.abs(Config.GROUP_ID));
                    upload = uploadQuery.execute();
                    offset = 0;
                }
                File img = new File("temp.jpg");
                FileUtils.copyURLToFile(new URL(url), img);
                PhotoUploadResponse uplresponse = new Upload(Config.VK).photo(upload.getUploadUrl(), img).execute();
                List<Photo> photos = null;
                var saveQuery = new Photos(Config.VK).save(Config.ADMIN)
                        .albumId(uplresponse.getAid())
                        .hash(uplresponse.getHash())
                        .photosList(uplresponse.getPhotosList())
                        .server(uplresponse.getServer())
                        .caption(url);
                        if(isGroup) saveQuery.groupId(Math.abs(Config.GROUP_ID));
                saveQuery.execute();

                img.delete();
                savesCount++;
                offset++;
                long endTime = System.currentTimeMillis();
                long deltaTime = endTime - startTime;
                if(deltaTime > 0 && deltaTime < 1500){
                    Thread.sleep(deltaTime);
                }
            }
            new Messages(Config.VK)
                    .edit(Config.GROUP, Config.ADMIN_ID, mid)
                    .message("Прогресс: "+urls.size()+"/"+urls.size())
                    .execute();
        } catch (ApiException | ClientException | InterruptedException | IOException e) {
            try {
                new Messages(Config.VK)
                        .send(Config.GROUP)
                        .message("Заполнение остановленно с ошибкой.\n"+e.getMessage())
                        .peerId(Config.ADMIN_ID)
                        .randomId(Utils.getRandomInt32())
                        .execute();
            } catch (ApiException | ClientException ex) {
                LOG.error(e);
            }
            LOG.error(e);
        }
    }

    private static void drawCover(){
        try {
            UploadServer uploadServer = new Photos(Config.VK)
                    .getOwnerCoverPhotoUploadServer(Config.GROUP, Config.GROUP_ID)
                    .cropX(0).cropX2(1590)
                    .cropY(0).cropY2(400)
                    .execute();
            BufferedImage coverImage = new BufferedImage(1590,400,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = coverImage.createGraphics();
            Utils.applyQualityRenderingHints(g2d);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0,0,1590,400);
            GroupFull groupFull = new Groups(Config.VK).getById(Config.GROUP).groupId(String.valueOf(Config.GROUP_ID)).execute().get(0);
            g2d.setColor(Color.WHITE);
            Utils.drawIntoRect(groupFull.getName(), new Rectangle(0,0,1590,400), Utils.Align.CENTER, g2d);
            File coverFile = new File("cover.png");
            ImageIO.write(coverImage, "png", coverFile);
            OwnerCoverUploadResponse coverUploadResponse = new Upload(Config.VK).photoOwnerCover(uploadServer.getUploadUrl().toString(), coverFile).execute();
            List<Image> images = new Photos(Config.VK).saveOwnerCoverPhoto(Config.GROUP, coverUploadResponse.getHash(), coverUploadResponse.getPhoto()).execute();
        } catch (ApiException | ClientException | IOException e) {
            LOG.error(e);
        }
    }

    public static void pack(String sourceDirPath, String zipFilePath) {
        try {
            Path p = Files.createFile(Paths.get(zipFilePath));
            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
                Path pp = Paths.get(sourceDirPath);
                Files.walk(pp)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                            try {
                                zs.putNextEntry(zipEntry);
                                Files.copy(path, zs);
                                zs.closeEntry();
                            } catch (IOException e) {
                                LOG.error(e);
                            }
                        });
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }
    
}
