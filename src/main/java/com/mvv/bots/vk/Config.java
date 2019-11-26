package com.mvv.bots.vk;

import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.main.scripts.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.util.List;

public interface Config {

    int UPDATE_TIME = 3600; //1 hour
    String CONFIRMATION_TOKEN = System.getenv("CONFIRMATION_TOKEN");
    int ADMIN_ID = Integer.parseInt(System.getenv("ADMIN_ID"));
    int GROUP_ID = Integer.parseInt(System.getenv("GROUP_ID"));
    String GROUP_TOKEN = System.getenv("GROUP_TOKEN");
    String ADMIN_TOKEN = System.getenv("ADMIN_TOKEN");
    String JDBC_DATABASE_URL = System.getenv("JDBC_DATABASE_URL");
    String DARKSKY_API_KEY = System.getenv("DARKSKY_API_KEY");
    int APP_ID = Integer.parseInt(System.getenv("APP_ID"));
    String APP_SECRET = System.getenv("APP_SECRET");
    String REDIRECT_URL = System.getenv("REDIRECT_URL");

    VkApiClient VK = new VkApiClient(HttpTransportClient.getInstance());
    UserActor ADMIN = new UserActor(ADMIN_ID, ADMIN_TOKEN);
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

    List<Script> SCRIPTS = Script.getSortedList(List.of(
            new AdminPanel(),
            new Authorization(),
            new Advice(),
            new Donation(),
            new Duty(),
            new Killer(),
            new InstaGet(),
            new WallParser(),
            new Weather(),
            new ScriptList()
    ));
}
