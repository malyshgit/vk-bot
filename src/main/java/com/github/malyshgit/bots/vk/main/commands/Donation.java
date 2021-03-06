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
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;

import java.util.ArrayList;
import java.util.List;

public class Donation implements Command {

    @Override
    public String smile(){
        return "\uD83D\uDCB0";
    }

    @Override
    public String key(){
        return "донат";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - пожертвование.";
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
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setAction(new KeyboardButtonAction()
                                            .setType(KeyboardButtonActionType.VKPAY)
                                            .setHash(String.format("action=transfer-to-group&group_id=%d&aid=%d",
                                                    Math.abs(Config.GROUP_ID), Config.APP_ID))
                                    )
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Донат")
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
