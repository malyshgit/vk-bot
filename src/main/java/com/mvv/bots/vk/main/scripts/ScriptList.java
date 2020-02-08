/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.mvv.bots.vk.Config;
import com.vk.api.sdk.objects.messages.keyboard.Payload;

import java.util.ArrayList;
import java.util.List;

public class ScriptList implements Script {

    @Override
    public String smile(){
        return "\uD83D\uDD11";
    }

    @Override
    public String key(){
        return "команды";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - список команд.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.USER;
    }

    @Override
    public void update(){

    }

    @Override
    public void send(Message message, Integer step) {
        try {
            Keyboard keyboard = new Keyboard();

            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);

            int peerId = message.getPeerId();
            if(message.getPeerId() >= 2000000000){
                /*StringBuffer sb = new StringBuffer();
                for (Script script : Config.SCRIPTS) {
                    if (script.accessMode().equals(AccessMode.CONVERSATION) || script.accessMode().equals(AccessMode.FORALL)) {
                        sb.append(script.description() + "\n");
                    }
                }
                new Messages(Config.VK())
                        .send(Config.GROUP)
                        .message(sb.toString())
                        .peerId(peerId)
                        .randomId(Utils.getRandomInt32())
                        .execute();*/
            }else{
                List<KeyboardButton> row = new ArrayList<>();
                if(Config.SCRIPTS.size() <= 20){
                    for (Script script : Config.SCRIPTS) {
                        if (script.accessMode().equals(AccessMode.USER) || (message.getPeerId().equals(Config.ADMIN_ID) && script.accessMode().equals(AccessMode.ADMIN))) {
                            row.add(new KeyboardButton()
                                    .setColor(KeyboardButtonColor.PRIMARY)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", script.getClass().getName())
                                                    .put("step", 0)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel(script.smile() + " " + script.key())));
                            if(row.size() == 2){
                                buttons.add(row);
                                row = new ArrayList<>();
                            }
                        }
                    }
                    if(!row.isEmpty()) buttons.add(row);
                }else {
                    JsonObject payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    int offset = 0;
                    if (payload.has("offset")) {
                        offset = payload.get("offset").getAsInt();
                    }
                    List<Script> list;
                    if (offset < Config.SCRIPTS.size() - 1) {
                        int firstIndex = offset;
                        int lastIndex = offset + 17;
                        if (lastIndex > Config.SCRIPTS.size() - 1) lastIndex = Config.SCRIPTS.size() - 1;

                        list = Config.SCRIPTS.subList(firstIndex, lastIndex);

                        for (Script script : list) {
                            if (script.accessMode().equals(AccessMode.USER) || (message.getPeerId().equals(Config.ADMIN_ID) && script.accessMode().equals(AccessMode.ADMIN))) {
                                row.add(new KeyboardButton()
                                        .setColor(KeyboardButtonColor.PRIMARY)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", script.getClass().getName())
                                                        .put("step", 0)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel(script.smile() + " " + script.key())));

                                if(row.size() == 2){
                                    buttons.add(row);
                                    row = new ArrayList<>();
                                }
                            }
                        }
                        if (!row.isEmpty()) buttons.add(row);

                        row = new ArrayList<>();
                        if (firstIndex == 0) {
                            row.add(new KeyboardButton()
                                    .setColor(KeyboardButtonColor.POSITIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 1)
                                                    .put("offset", lastIndex)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Вперед")));
                        } else if(lastIndex >= Config.SCRIPTS.size() - 1){
                            row.add(new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 1)
                                                    .put("offset", firstIndex - 17)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")));
                        }else{
                            row.add(new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 1)
                                                    .put("offset", firstIndex - 17)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад")));
                            row.add(new KeyboardButton()
                                    .setColor(KeyboardButtonColor.POSITIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 1)
                                                    .put("offset", lastIndex)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Вперед")));
                        }
                        buttons.add(row);
                    } else {
                        return;
                    }
                }
                new Messages(Config.VK())
                        .send(Config.GROUP)
                        .message("Список")
                        .keyboard(keyboard)
                        .peerId(peerId)
                        .randomId(Utils.getRandomInt32())
                        .execute();
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }
    public static void open(Message message){
        new ScriptList().send(message, 0);
    }
    
}
