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
import com.vk.api.sdk.actions.Groups;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.actions.Upload;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Image;
import com.vk.api.sdk.objects.base.UploadServer;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.messages.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import com.vk.api.sdk.objects.responses.OwnerCoverUploadResponse;

import javax.imageio.ImageIO;

/**
 *
 * @author I1PABIJJA
 */
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
                                            .setLabel("Пересоздать БД")),                
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+2+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Отладка"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+5+"}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Обложка"))
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
                    Users.create();
                    Settings.create();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("БД пересоздана.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 2:
                    Settings.Option debbug = Settings.find("debbug");
                    if(debbug != null){
                        keyboard.setInline(true);
                        if(Boolean.parseBoolean(debbug.getValue())){
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 4 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Отключить"))
                            ));
                        }else{
                            buttons.add(List.of(
                                    new KeyboardButton()
                                            .setColor(KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 3 + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Включить"))
                            ));
                        }
                    }else{
                        debbug = new Settings.Option("debbug", "false");
                        Settings.add(debbug);
                        send(message, 2);
                        return;
                    }
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отладка")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 3:
                    debbug = Settings.find("debbug");
                    debbug.setValue("true");
                    Settings.update(debbug);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отладка включена.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 4:
                    debbug = Settings.find("debbug");
                    debbug.setValue("false");
                    Settings.update(debbug);
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отладка отключена.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 5:
                    drawCover();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Обложка перерисована.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
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
            g2d.setFont(Font.getFont("Arial").deriveFont(48f));
            Utils.drawIntoRect(groupFull.getName(), new Rectangle(0,0,1590,400), Utils.Align.CENTER, g2d);
            File coverFile = new File("cover.png");
            ImageIO.write(coverImage, "png", coverFile);
            OwnerCoverUploadResponse coverUploadResponse = new Upload(Config.VK).photoOwnerCover(uploadServer.getUploadUrl().toString(), coverFile).execute();
            List<Image> images = new Photos(Config.VK).saveOwnerCoverPhoto(Config.GROUP, coverUploadResponse.getHash(), coverUploadResponse.getPhoto()).execute();
        } catch (ApiException | ClientException | IOException e) {
            LOG.error(e);
        }
    }
    
}
