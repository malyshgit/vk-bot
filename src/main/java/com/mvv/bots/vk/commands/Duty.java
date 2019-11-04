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
import com.vk.api.sdk.objects.users.UserFull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author I1PABIJJA
 */
public class Duty implements Script{

    @Override
    public String smile(){
        return "\uD83C\uDF6D";
    }

    @Override
    public String key(){
        return "дежурный";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - назначит дежурного.\n" +
                " "+key()+" n - назначит n дежурных.";
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

            String adds = Script.cuteKey(message.getText(), key()).trim();

            if(adds.matches("\\d+")){
                int count = Integer.valueOf(adds);
                if(count > 1) {
                    if (members.getCount() <= count) {
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Дежурят все!")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    List<User> list = new ArrayList<>();
                    List<UserFull> users = members.getProfiles();
                    for (int i = 0; i < count; i++) {
                        int aim = Utils.getRandom(0, users.size() - 1);
                        list.add(users.get(aim));
                        users.remove(aim);
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("Дежурят:\n");
                    list.forEach(user -> sb
                            .append("[id")
                            .append(user.getId())
                            .append("|")
                            .append(user.getFirstName())
                            .append(" ")
                            .append(user.getLastName())
                            .append("]\n"));

                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message(sb.toString())
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    return;
                }
            }

            int aim = Utils.getRandom(0, members.getProfiles().size()-1);

            User user = members.getProfiles().get(aim);

            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message("Дежурный: [id"+user.getId()+"|"+user.getFirstName()+" "+user.getLastName()+"]")
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();

        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }
    
}
