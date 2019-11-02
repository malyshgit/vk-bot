/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.mvv.bots.vk.Config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author I1PABIJJA
 */
public class ScriptList implements Script{

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
        return AccessMode.FORALL;
    }

    @Override
    public void update() {
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
                            .message("Команды - описание")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 0:
                    buttons.add(List.of(
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.DEFAULT)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\""+getClass().getName()+"\"," +
                                            "\"step\":"+(-1)+"}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Описание")),
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.POSITIVE)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\""+getClass().getName()+"\"," +
                                            "\"step\":"+1+"}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Список"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Команды - меню")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1:
                    int peerId = message.getPeerId();
                    if(message.fromConversation()){
                        StringBuffer sb = new StringBuffer();
                        for (Script script : Config.SCRIPTS) {
                            if (script.accessMode().equals(AccessMode.CONVERSATION) || script.accessMode().equals(AccessMode.FORALL)) {
                                sb.append(script.description() + "\n");
                            }
                        }
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message(sb.toString())
                                .peerId(peerId)
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }else{
                        List<KeyboardButton> row = new ArrayList<>();
                        if(Config.SCRIPTS.size() <= 20){
                            for (Script script : Config.SCRIPTS) {
                                if (script.accessMode().equals(AccessMode.USER) || script.accessMode().equals(AccessMode.FORALL) || (message.getPeerId().equals(Config.ADMIN_ID) && script.accessMode().equals(AccessMode.ADMIN))) {
                                    row.add(new KeyboardButton()
                                            .setColor(KeyboardButtonColor.PRIMARY)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\""+script.getClass().getName()+"\"," +
                                                            "\"step\":"+0+"}"
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
                                    if (script.accessMode().equals(AccessMode.USER) || script.accessMode().equals(AccessMode.FORALL) || (message.getPeerId().equals(Config.ADMIN_ID) && script.accessMode().equals(AccessMode.ADMIN))) {
                                        row.add(new KeyboardButton()
                                                .setColor(KeyboardButtonColor.PRIMARY)
                                                .setAction(new KeyboardButtonAction().setPayload(
                                                        "{\"script\":\"" + script.getClass().getName() + "\"," +
                                                                "\"step\":" + 0 + "}"
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
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 1 + "," +
                                                            "\"offset\":" + lastIndex + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Вперед")));
                                } else if(lastIndex >= Config.SCRIPTS.size() - 1){
                                    row.add(new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 1 + "," +
                                                            "\"offset\":" + (firstIndex - 17) + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Назад")));
                                }else{
                                    row.add(new KeyboardButton()
                                            .setColor(KeyboardButtonColor.NEGATIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 1 + "," +
                                                            "\"offset\":" + (firstIndex - 17) + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Назад")));
                                    row.add(new KeyboardButton()
                                            .setColor(KeyboardButtonColor.POSITIVE)
                                            .setAction(new KeyboardButtonAction().setPayload(
                                                    "{\"script\":\"" + getClass().getName() + "\"," +
                                                            "\"step\":" + 1 + "," +
                                                            "\"offset\":" + lastIndex + "}"
                                            ).setType(KeyboardButtonActionType.TEXT)
                                                    .setLabel("Вперед")));
                                }
                                buttons.add(row);
                            } else {
                                return;
                            }
                        }
                        System.out.println(keyboard.toPrettyString());
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Команды - список")
                                .keyboard(keyboard)
                                .peerId(peerId)
                                .randomId(Utils.getRandomInt32())
                                .execute();
                    }
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            e.printStackTrace();
        }
    }
    
}
