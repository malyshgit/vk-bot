/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.mvv.bots.vk.Config;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author I1PABIJJA
 */
public class Advice implements Script{

    @Override
    public String smile(){
        return "\uD83D\uDCAC";
    }

    @Override
    public String key(){
        return "совет";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - даст дельный совет.";
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
            keyboard.setInline(true);
            keyboard.setButtons(buttons);
            buttons.add(List.of(
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.PRIMARY)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\""+getClass().getName()+"\"," +
                                            "\"step\":"+0+"}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Ещё"))
            ));
            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message(advice())
                    .keyboard(keyboard)
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();
        }catch (ApiException | ClientException | IOException e){
            e.printStackTrace();
        }
    }

    public static String advice() throws IOException {
        URL url = new URL("http://fucking-great-advice.ru/api/random");
        String json =  new String(url.openStream().readAllBytes(), Charset.forName("UTF-8"));
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("text").getAsString();
    }

}
