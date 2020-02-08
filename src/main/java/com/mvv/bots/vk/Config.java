package com.mvv.bots.vk;

import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.main.scripts.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.util.List;

public class Config {

    public static final Integer PORT = Integer.parseInt(System.getenv("PORT"));

    public static final String CONFIRMATION_TOKEN = System.getenv("CONFIRMATION_TOKEN");
    public static final int ADMIN_ID = Integer.parseInt(System.getenv("ADMIN_ID"));
    public static final int GROUP_ID = Integer.parseInt(System.getenv("GROUP_ID"));
    public static final String GROUP_TOKEN = System.getenv("GROUP_TOKEN");
    public static final String ADMIN_TOKEN = System.getenv("ADMIN_TOKEN");
    public static final String JDBC_DATABASE_URL = System.getenv("JDBC_DATABASE_URL");
    public static final String DARKSKY_API_KEY = System.getenv("DARKSKY_API_KEY");
    public static final int APP_ID = Integer.parseInt(System.getenv("APP_ID"));
    public static final String APP_SECRET = System.getenv("APP_SECRET");
    public static final String REDIRECT_URL = System.getenv("REDIRECT_URL");

    //public static final VkApiClient VK = new VkApiClient(HttpTransportClient.getInstance());
    public static final UserActor ADMIN = new UserActor(ADMIN_ID, ADMIN_TOKEN);
    public static final GroupActor GROUP = new GroupActor(GROUP_ID, GROUP_TOKEN);

    public static VkApiClient VK(){
        return new VkApiClient(new HttpTransportClient());
    }

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

    public static final List<Script> SCRIPTS = Script.getSortedList(List.of(
            new AdminPanel(),
            //new Administration(),
            new Authorization(),
            new Advice(),
            new Donation(),
            new Resave(),
            new Weather(),
            new ScriptList()
    ));
}
