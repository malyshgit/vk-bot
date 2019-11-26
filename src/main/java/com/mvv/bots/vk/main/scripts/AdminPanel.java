/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.settings.Option;
import com.mvv.bots.vk.database.tables.settings.Settings;
import com.mvv.bots.vk.database.tables.users.User;
import com.mvv.bots.vk.database.tables.users.Users;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.*;
import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.*;
import com.vk.api.sdk.objects.base.Image;
import com.vk.api.sdk.objects.enums.DocsType;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.messages.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import java.util.zip.ZipOutputStream;

import com.google.gson.*;
import com.vk.api.sdk.objects.messages.responses.GetByIdResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.GetAlbumsResponse;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.responses.OwnerCoverUploadResponse;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;

public class AdminPanel implements Script {

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
                            /*new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+3+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Авторизация")),*/
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+4+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Обновление"))
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
                    List<Option> options = Settings.findAll();
                    if(!options.isEmpty()){
                        String info = options.stream().map(Option::toString).collect(Collectors.joining("\n"));
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
                    List<User> users = Users.findAll();
                    if(!users.isEmpty()){
                        String info = users.stream().map(User::toString).collect(Collectors.joining("\n"));
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
                    Option debbug = Settings.find("debbug");
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
                        debbug = new Option("debbug", "false");
                        Settings.add(debbug);
                        send(message, 2);
                        return;
                    }
                    Option update = Settings.find("update");
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
                        update = new Option("update", "false");
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
                    /*var url = String.format("https://oauth.vk.com/authorize?client_id=%d&display=page&redirect_uri=%s&scope=groups,docs,offline,photos,wall&response_type=code&v=5.103",
                    Config.APP_ID, Config.REDIRECT_URL);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Перейдите по ссылке:\n"+url)
                            .peerId(message.getPeerId())
                            .dontParseLinks(false)
                            .randomId(Utils.getRandomInt32())
                            .execute();*/
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
        }catch (ApiException | ClientException e){
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
