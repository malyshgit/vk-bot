package com.mvv.bots.vk;

import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.main.scripts.*;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.util.List;

public class Config {

    public static Integer PORT;

    public static String CONFIRMATION_TOKEN;
    public static int ADMIN_ID;
    public static int GROUP_ID;
    public static String GROUP_TOKEN;
    public static String ADMIN_TOKEN;
    public static String JDBC_DATABASE_URL; //= System.getenv("JDBC_DATABASE_URL");
    public static String DARKSKY_API_KEY;
    public static String TELEGRAM_BOT_TOKEN;
    public static int APP_ID;
    public static String APP_SECRET;
    public static String REDIRECT_URL;

    //public static final VkApiClient VK = new VkApiClient(HttpTransportClient.getInstance());
    public static UserActor ADMIN;
    public static GroupActor GROUP;

    public static VkApiClient VK(){
        return new VkApiClient(new HttpTransportClient());
    }

    public static void load(){
        load(
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("CONFIRMATION_TOKEN"),
                Integer.parseInt(System.getenv("ADMIN_ID")),
                Integer.parseInt(System.getenv("GROUP_ID")),
                System.getenv("GROUP_TOKEN"),
                System.getenv("ADMIN_TOKEN"),
                System.getenv("JDBC_DATABASE_URL"), //= System.getenv("JDBC_DATABASE_URL");
                System.getenv("DARKSKY_API_KEY"),
                System.getenv("TELEGRAM_BOT_TOKEN"),
                Integer.parseInt(System.getenv("APP_ID")),
                System.getenv("APP_SECRET"),
                System.getenv("REDIRECT_URL")
        );
    }

    public static void load(int PORT, String CONFIRMATION_TOKEN, int ADMIN_ID, int GROUP_ID,
                            String GROUP_TOKEN, String ADMIN_TOKEN, String JDBC_DATABASE_URL,
                            String DARKSKY_API_KEY, String TELEGRAM_BOT_TOKEN, int APP_ID,
                            String APP_SECRET, String REDIRECT_URL){
        Config.PORT = PORT;

        Config.CONFIRMATION_TOKEN = CONFIRMATION_TOKEN;
        Config.ADMIN_ID = ADMIN_ID;
        Config.GROUP_ID = GROUP_ID;
        Config.GROUP_TOKEN = GROUP_TOKEN;
        Config.ADMIN_TOKEN = ADMIN_TOKEN;
        Config.JDBC_DATABASE_URL = JDBC_DATABASE_URL; //= System.getenv("JDBC_DATABASE_URL");
        Config.DARKSKY_API_KEY = DARKSKY_API_KEY;
        Config.TELEGRAM_BOT_TOKEN = TELEGRAM_BOT_TOKEN;
        Config.APP_ID = APP_ID;
        Config.APP_SECRET = APP_SECRET;
        Config.REDIRECT_URL = REDIRECT_URL;

        //public static final VkApiClient VK = new VkApiClient(HttpTransportClient.getInstance());
        Config.ADMIN = new UserActor(ADMIN_ID, ADMIN_TOKEN);
        Config.GROUP = new GroupActor(GROUP_ID, GROUP_TOKEN);
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
            new WikiRandom(),
            new ScriptList()
    ));
}
