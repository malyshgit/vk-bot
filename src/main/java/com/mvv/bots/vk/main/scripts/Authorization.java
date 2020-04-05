/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.models.User;
import com.mvv.bots.vk.database.dao.UsersTable;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.actions.OAuth;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;

import java.util.ArrayList;
import java.util.List;

public class Authorization implements Script {

    @Override
    public String smile(){
        return "\uD83D\uDD10";
    }

    @Override
    public String key(){
        return "авторизация";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - предоставление ключа доступа пользователя сообществу.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.USER;
    }

    @Override
    public void update() {

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
                    if(user.getToken() != null){
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.NEGATIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", ScriptList.class.getName())
                                                        .put("step", 0)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Назад"))
                        ));
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.PRIMARY)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", getClass().getName())
                                                        .put("step", 1)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Обновить токен"))
                        ));
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.NEGATIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", getClass().getName())
                                                        .put("step", 2)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Запретить доступ"))
                        ));
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Авторизация")
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else{
                        send(message, 1);
                    }
                    break;
                case 1:
                    var url = String.format("https://oauth.vk.com/authorize?client_id=%d&display=page&redirect_uri=%s&scope=groups,docs,offline,photos,wall&response_type=code&v=5.103",
                            Config.APP_ID, Config.REDIRECT_URL);
                    var vkcc = new com.vk.api.sdk.actions.Utils(Config.VK()).getShortLink(Config.GROUP, url).execute();
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Перейдя по ссылке, Вы предоставляете доступ к: сообществам, документам, фотографиям и записям на стене." +
                                    "\n"+vkcc.getShortUrl())
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    ScriptList.open(message);
                    break;
                case 2:
                    user = UsersTable.findById(message.getFromId());
                    user.setToken(null);
                    user.update();
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Доступ запрещен.")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    ScriptList.open(message);
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }

    public static boolean getToken(String code){
        try {
            var response = new OAuth(Config.VK()).userAuthorizationCodeFlow(Config.APP_ID, Config.APP_SECRET, Config.REDIRECT_URL, code).execute();
            if (response != null) {
                var user = UsersTable.findById(response.getUserId());
                user.setToken(response.getAccessToken());
                user.update();
                return true;
            }
        }catch (ApiException | ClientException e) {
            LOG.error(e);
            return false;
        }
        return false;
    }
    
}
