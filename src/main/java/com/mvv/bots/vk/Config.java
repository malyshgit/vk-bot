package com.mvv.bots.vk;

import com.mvv.bots.vk.commands.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Config {

    int UPDATE_TIME = 3600; //1 hour
    String CONFIRMATION_TOKEN = System.getenv("CONFIRMATION_TOKEN");
    int ADMIN_ID = Integer.parseInt(System.getenv("ADMIN_ID"));
    int GROUP_ID = Integer.parseInt(System.getenv("GROUP_ID"));
    String GROUP_TOKEN = System.getenv("GROUP_TOKEN");
    String ADMIN_TOKEN = System.getenv("ADMIN_TOKEN");
    String JDBC_DATABASE_URL = System.getenv("JDBC_DATABASE_URL");

    VkApiClient VK = new VkApiClient(HttpTransportClient.getInstance());
    GroupActor GROUP = new GroupActor(GROUP_ID, GROUP_TOKEN);

    /*Font FONT = loadFont();

    static Font loadFont(){
        try {
            System.out.println("#load font file: "+Config.class.getResource( "/open-sans.ttf").getFile());
            return Font.createFont(Font.TRUETYPE_FONT, Config.class.getResourceAsStream( "/open-sans.ttf"));
        } catch (IOException | FontFormatException e) {
            System.out.println("#load system font: Arial");
            return new Font("Arial", Font.TRUETYPE_FONT, 0);
        }
    }*/

    List<Script> SCRIPTS = Script.getSortedList(List.of(new Script[]{
            new AdminPanel(),
            new Advice(),
            new Cover(),
            new Duty(),
            new Killer(),
            new Weather(),

            new ScriptList()
    }));
}
