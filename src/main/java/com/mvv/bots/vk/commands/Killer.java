/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetConversationMembersResponse;
import com.vk.api.sdk.objects.users.User;
import com.mvv.bots.vk.Config;

/**
 *
 * @author I1PABIJJA
 */
public class Killer implements Script{

    @Override
    public String smile(){
        return "\uD83D\uDD2B";
    }

    @Override
    public String key(){
        return "киллер";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - начнет охоту.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.CONVERSATION;
    }

    @Override
    public void update() {
        return;
    }

    @Override
    public synchronized void send(Message message, Integer step) {
        try {
            if(!(message.getPeerId() >= 2000000000)){
                return;
            }

            GetConversationMembersResponse members = new Messages(Config.VK)
                    .getConversationMembers(Config.GROUP, message.getPeerId()).execute();

            int aim = Utils.getRandom(0, members.getProfiles().size()-1);

            User user = members.getProfiles().get(aim);

            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message("Отслеживаю цель: [id"+user.getId()+"|"+user.getFirstName()+"]")
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();

            Thread.sleep(2000);

            new Messages(Config.VK)
                    .send(Config.GROUP).message("Прицеливаюсь...")
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();

            Thread.sleep(2000);

            if(Utils.getRandom(0,1) == 0) {
                new Messages(Config.VK)
                        .send(Config.GROUP)
                        .message("Цель ликвидирована.")
                        .peerId(message.getPeerId())
                        .randomId(Utils.getRandomInt32())
                        .execute();
            }else{
                new Messages(Config.VK)
                        .send(Config.GROUP)
                        .message("Промах.")
                        .peerId(message.getPeerId())
                        .randomId(Utils.getRandomInt32())
                        .execute();
            }

        }catch (ApiException | ClientException | InterruptedException e){
            LOG.error(e);
        }
    }
    
}
