/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.malyshgit.bots.vk.main.commands;

import com.github.malyshgit.bots.vk.Config;
import com.github.malyshgit.bots.vk.main.AccessMode;
import com.github.malyshgit.bots.vk.main.Command;
import com.github.malyshgit.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.*;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;

import java.util.ArrayList;
import java.util.List;

public class Administration implements Command {

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
                                            new Payload()
                                                .put("script", getClass().getName())
                                                .put("step", 1)
                                                .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Стена"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                .put("script", Commands.class.getName())
                                                .put("step", 1)
                                                .toString()
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
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }

}
