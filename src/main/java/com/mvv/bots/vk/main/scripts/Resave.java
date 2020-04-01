package com.mvv.bots.vk.main.scripts;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.models.User;
import com.mvv.bots.vk.database.dao.UsersTable;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendPhoto;
import com.vk.api.sdk.actions.*;
import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.keyboard.Payload;
import com.vk.api.sdk.objects.photos.Image;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.users.Fields;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Resave implements Script {

    @Override
    public String smile(){
        return "\uD83D\uDCBE";
    }

    @Override
    public String key(){
        return "ресейв";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - пересохранение альбомов.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.USER;
    }

    @Override
    public void update() {
        UsersTable.findAll().forEach(user -> {
            if(user.getToken() != null){
                if(user.getParameters().containsKey("resave")){
                    var options = user.getParameters().get("resave");
                    var date = options.get("date").getAsLong();
                    var dt = System.currentTimeMillis() - date;
                    if(dt >= DateUtils.MILLIS_PER_MINUTE*55){
                        Message message = new Message();
                        message.setFromId(user.getId());
                        message.setPeerId(user.getId());
                        message.setPayload(
                                new Payload()
                                        .put("script", getClass().getName())
                                        .put("step", 4)
                                        .toString()
                        );
                        JsonObject object = new JsonObject();
                        object.addProperty("date", System.currentTimeMillis());
                        user.getParameters().put("resave", object);
                        UsersTable.update(user);
                        send(message, 4);
                    }
                }else{
                    JsonObject object = new JsonObject();
                    object.addProperty("date", System.currentTimeMillis());
                    user.getParameters().put("resave", object);
                    UsersTable.update(user);
                }
            }
        });

    }

    public static HashMap<String, Integer> confirmKeys = new HashMap<>();

    @Override
    public void send(Message message, Integer step) {
        try {
            Keyboard keyboard = new Keyboard();

            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);

            Template template = new Template();
            template.setType(TemplateType.CAROUSEL);

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
                    User user = UsersTable.findById(message.getFromId());
                    if(user.getToken() == null){
                        new Authorization().send(message, 0);
                        break;
                    }
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", ScriptList.class.getName())
                                                    .put("step", 0)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    /*if(Config.TELEGRAM_BOT_TOKEN != null){
                        user = UsersTable.findById(message.getFromId());
                        UserActor userActor = new UserActor(user.getId(), user.getToken());
                        if(user.getParameters().containsKey("resave")) {
                            var options = user.getParameters().get("resave");
                            if(options.has("resavetotg")){
                                var resavetotg = options.get("resavetotg").getAsBoolean();
                                if(resavetotg){
                                    if(!options.has("tgchatid")){
                                        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                                                .withinRange('0', 'z')
                                                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                                                .build();
                                        var key = generator.generate(32);
                                        TelegramBot bot = new TelegramBot(Config.TELEGRAM_BOT_TOKEN);
                                        var botName = bot.execute(new GetMe()).user().username();
                                        confirmKeys.put(key, user.getId());
                                        buttons.add(List.of(
                                                new KeyboardButton()
                                                        .setColor(KeyboardButtonColor.DEFAULT)
                                                        .setAction(new KeyboardButtonAction().setPayload(
                                                                new Payload()
                                                                        .put("script", getClass().getName())
                                                                        .put("step", 112)
                                                                        .toString()
                                                        ).setType(KeyboardButtonActionType.TEXT)
                                                                .setLabel("Проверить"))
                                        ));
                                        new Messages(Config.VK())
                                                .send(Config.GROUP)
                                                .message("1. Приглосите в частную группу телеграм бота @"+botName+"\n" +
                                                        "2. Обязательно выдайте права на публикацию сообщений. Другие права необязательны.\n" +
                                                        "3. Отправьте в частную группу в телеграме сообщение \""+key+"\"\n" +
                                                        "4. Нажмите \"Проверить\"")
                                                .keyboard(keyboard)
                                                .peerId(message.getPeerId())
                                                .randomId(Utils.getRandomInt32())
                                                .execute();
                                        break;
                                    }
                                    buttons.add(List.of(
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 111)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("Сохраняется в ТГ"))
                                    ));
                                }else{
                                    buttons.add(List.of(
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 111)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("Сохраняется в ВК"))
                                    ));
                                }
                            }else{
                                options.addProperty("resavetotg", false);
                                user.getParameters().put("resave", options);
                                UsersTable.update(user);
                                send(message, 0);
                                break;
                            }
                        }else{
                            JsonObject object = new JsonObject();
                            user.getParameters().put("resave", object);
                            UsersTable.update(user);
                            send(message, 0);
                            break;
                        }
                    }*/
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 12)
                                                    .toString()
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Добавить"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Пересохранение альбомов")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 1);
                    break;
                case 112:
                    TelegramBot bot = new TelegramBot(Config.TELEGRAM_BOT_TOKEN);
                    user = UsersTable.findById(message.getFromId());
                    var options = user.getParameters().get("resave");
                    var updates = bot.execute(new GetUpdates().offset(0).limit(100)).updates();
                    
                    //while(updates.size() > 0){
                        
                        updates.forEach(update -> {
                            if(update.message() != null){
                            var confirmKey = update.message().text();
                            LOG.error("=============\n"+confirmKey);
                            var chatId = update.message().chat().id();
                            LOG.error(chatId); LOG.error(confirmKeys); LOG.error(Resave.confirmKeys);
                            if(Resave.confirmKeys.containsKey(confirmKey)){
                               // if(Resave.confirmKeys.get(confirmKey) == user.getId()){
                                LOG.error(true);
                                options.addProperty("tgchatid", chatId);
                                user.getParameters().put("resave", options);
                                UsersTable.update(user);
                                Resave.confirmKeys.remove(confirmKey);
                               // }
                            }}
                        });
                        updates = bot.execute(new GetUpdates().offset(updates.stream().max(Comparator.comparingInt(Update::updateId)).get().updateId()).limit(100)).updates();
                        
                    //}
                    
                    if(!options.has("tgchatid")){
                        
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Повторите попытку.")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        break;
                    }
                    send(message, 0);
                    break;
                case 12:
                    var getByIdResponse = new Messages(Config.VK()).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    var url = getByIdResponse.getItems().get(0).getText();
                    if(url == null || url.isEmpty() || !url.matches("https://vk.com/album-?\\d+_-?\\d+")){
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Отправьте ссылку на альбом и нажмите \"Добавить\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    user = UsersTable.findById(message.getFromId());
                    var userActor = new UserActor(user.getId(), user.getToken());
                    String wallName;

                    var albumInfo = url.substring(20).split("_");
                    if(albumInfo[0].startsWith("-")){
                        var groupProfile = new Groups(Config.VK()).getById(userActor).groupId(albumInfo[0].substring(1)).execute();
                        wallName = groupProfile.get(0).getName();
                    }else{
                        var userProfile = new Users(Config.VK()).get(userActor).fields(Fields.PHOTO_200).userIds(albumInfo[0]).execute();
                        wallName = userProfile.get(0).getFirstName()+" "+userProfile.get(0).getLastName();
                    }

                    var albumDesc = "";
                    if(albumInfo[0].startsWith("-")){
                        if(albumInfo[1].matches("(0|-6)")){
                            albumInfo[1] = "-6";
                            albumDesc = "Фотографии страницы";
                        }else if(albumInfo[1].matches("(00|-7)")){
                            albumInfo[1] = "-7";
                            albumDesc = "Фотографии со стены";
                        }else{
                            var album = new Photos(Config.VK()).getAlbums(userActor)
                                    .ownerId(Integer.valueOf(albumInfo[0]))
                                    .albumIds(Integer.valueOf(albumInfo[1])).execute();
                            albumDesc = album.getItems().get(0).getTitle();
                        }
                    }else{
                        if(albumInfo[1].matches("(0|-6)")){
                            albumInfo[1] = "-6";
                            albumDesc = "Фотографии страницы";
                        }else if(albumInfo[1].matches("(00|-7)")){
                            albumInfo[1] = "-7";
                            albumDesc = "Фотографии со стены";
                        }else if(albumInfo[1].matches("(000|-15)")){
                            albumInfo[1] = "-15";
                            albumDesc = "Сохраненные фотографии";
                        }else{
                            var album = new Photos(Config.VK()).getAlbums(userActor)
                                    .ownerId(Integer.valueOf(albumInfo[0]))
                                    .albumIds(Integer.valueOf(albumInfo[1])).execute();
                            albumDesc = album.getItems().get(0).getTitle();
                        }
                    }

                    JsonObject object = new JsonObject();
                    JsonArray albums = new JsonArray();

                    if(user.getParameters().containsKey("resave")){
                        object = user.getParameters().get("resave");
                        if(object.has("albums")) albums = object.get("albums").getAsJsonArray();
                    }

                    JsonObject newAlbum = new JsonObject();
                    newAlbum.addProperty("title", wallName+"("+albumDesc+")");
                    newAlbum.addProperty("ownerid", albumInfo[0]);
                    newAlbum.addProperty("albumid", albumInfo[1]);
                    newAlbum.addProperty("hide", false);
                    newAlbum.addProperty("active", true);
                    newAlbum.addProperty("totg", false);
                    newAlbum.add("photoids", new JsonArray());
                    albums.add(newAlbum);
                    object.add("albums", albums);

                    object.addProperty("date", System.currentTimeMillis());
                    user.getParameters().put("resave", object);
                    UsersTable.update(user);
                    new Messages(Config.VK())
                        .send(Config.GROUP)
                        .message("Альбом в очереди.")
                        .peerId(message.getPeerId())
                        .randomId(Utils.getRandomInt32())
                        .execute();
                    send(message, 1);
                    break;
                case 1:
                    user = UsersTable.findById(message.getFromId());
                    userActor = new UserActor(user.getId(), user.getToken());
                    if(!user.getParameters().containsKey("resave")){
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Отправьте ссылку на альбом и нажмите \"Добавить\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    albums = user.getParameters().get("resave").get("albums").getAsJsonArray();

                    List<TemplateElement> elements = new ArrayList<>();
                    template.setElements(elements);
                    var payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    var offset = payload.has("offset") ? payload.get("offset").getAsInt() : 0;

                    var nextOffset = offset;
                    for(var i = 0; elements.size() < 10; i++) {
                        if (offset + i > albums.size() - 1) {
                            break;
                        }
                        if (i == 0 && offset > 0) {
                            elements.add(new TemplateElement()
                                    .setTitle("Навигация")
                                    .setDescription("Назад")
                                    .setButtons(List.of(
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 1)
                                                                    .put("offset", Math.max(offset - 10, 0))
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("<")),
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 1)
                                                                    .put("offset", 0)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("<<"))
                                    ))
                            );
                        }
                        if (i == 9 && nextOffset < albums.size()-1) {
                            int lastOffset = 0;
                            while(lastOffset < albums.size()-lastOffset){
                                if(lastOffset == 0 || lastOffset == albums.size()){
                                    lastOffset+=9;
                                }else{
                                    lastOffset+=8;
                                }
                            }
                            elements.add(new TemplateElement()
                                    .setTitle("Навигация")
                                    .setDescription("Вперед")
                                    .setButtons(List.of(
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 1)
                                                                    .put("offset", nextOffset)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel(">")),
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 1)
                                                                    .put("offset", lastOffset)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel(">>"))
                                    ))
                            );
                        } else {
                            var album = albums.get(offset + i).getAsJsonObject();
                            var title = album.get("title").getAsString();
                            var ownerId = album.get("ownerid").getAsString();
                            var albumId = album.get("albumid").getAsString();
                            var progress = album.get("photoids").getAsJsonArray().size();
                            var hide = album.get("hide").getAsBoolean();
                            var active = album.get("active").getAsBoolean();
                            var totg = album.get("totg").getAsBoolean();
                            elements.add(new TemplateElement()
                                    .setTitle(title)
                                    .setDescription("Загружено: "+progress)
                                    .setAction(new TemplateElementAction()
                                            .setType(TemplateElementActionType.OPEN_LINK)
                                            .setLink("https://vk.com/album"+ownerId+"_"+albumId)
                                    )
                                    .setButtons(List.of(
                                            new KeyboardButton()
                                                    .setColor(active ? KeyboardButtonColor.POSITIVE : KeyboardButtonColor.NEGATIVE)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 2)
                                                                    .put("offset", offset)
                                                                    .put("active", active)
                                                                    .put("ownerid", ownerId)
                                                                    .put("albumid", albumId)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("Заполнение")),
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.PRIMARY)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 3)
                                                                    .put("offset", offset)
                                                                    .put("totg", totg)
                                                                    .put("ownerid", ownerId)
                                                                    .put("albumid", albumId)
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel(totg ? "Telegram" : "Вконтакте"))
                                    ))
                            );
                            nextOffset++;
                        }
                    }
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Альбомы")
                            .template(template)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 2:
                    user = UsersTable.findById(message.getFromId());
                    userActor = new UserActor(user.getId(), user.getToken());
                    payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    var active = payload.get("active").getAsBoolean();
                    var ownerId = payload.get("ownerid").getAsString();
                    var albumId = payload.get("albumid").getAsString();
                    offset = payload.get("offset").getAsInt();

                    options = user.getParameters().get("resave");
                    var list = options.get("albums").getAsJsonArray();
                    for(var alb : list){
                        var album = alb.getAsJsonObject();
                        if(album.get("ownerid").getAsString().equals(ownerId)
                        && album.get("albumid").getAsString().equals(albumId)){
                            album.addProperty("active", !active);
                        }
                    }
                    user.getParameters().put("resave", options);
                    UsersTable.update(user);

                    message.setPayload(new Payload()
                            .put("script", getClass().getName())
                            .put("step", 1)
                            .put("offset", offset)
                            .toString());
                    send(message, 1);
                    break;
                case 3:
                    user = UsersTable.findById(message.getFromId());
                    if(Config.TELEGRAM_BOT_TOKEN == null) break;
                    if(!user.getParameters().get("resave").has("tgchatid")){
                        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                                .withinRange('0', 'z')
                                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                                .build();
                        var key = generator.generate(32);
                        bot = new TelegramBot(Config.TELEGRAM_BOT_TOKEN);
                        var botName = bot.execute(new GetMe()).user().username();
                        confirmKeys.put(key, user.getId());
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.NEGATIVE)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", ScriptList.class.getName())
                                                        .put("step", 0)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Назад"))
                        ));
                        buttons.add(List.of(
                                new KeyboardButton()
                                        .setColor(KeyboardButtonColor.DEFAULT)
                                        .setAction(new KeyboardButtonAction().setPayload(
                                                new Payload()
                                                        .put("script", getClass().getName())
                                                        .put("step", 112)
                                                        .toString()
                                        ).setType(KeyboardButtonActionType.TEXT)
                                                .setLabel("Проверить"))
                        ));
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("1. Приглосите в частную группу телеграм бота @"+botName+"\n" +
                                        "2. Обязательно выдайте права на публикацию сообщений. Другие права необязательны.\n" +
                                        "3. Отправьте в частную группу в телеграме сообщение \""+key+"\"\n" +
                                        "4. Нажмите \"Проверить\"")
                                .keyboard(keyboard)
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        break;
                    }
                    userActor = new UserActor(user.getId(), user.getToken());
                    payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    var totg = payload.get("totg").getAsBoolean();
                    ownerId = payload.get("ownerid").getAsString();
                    albumId = payload.get("albumid").getAsString();
                    offset = payload.get("offset").getAsInt();

                    list = user.getParameters().get("resave").get("albums").getAsJsonArray();
                    for(var alb : list){
                        var album = alb.getAsJsonObject();
                        if(album.get("ownerid").getAsString().equals(ownerId)
                                && album.get("albumid").getAsString().equals(albumId)){
                            album.addProperty("totg", !totg);
                        }
                    }
                    user.getParameters().put("resave", user.getParameters().get("resave"));
                    UsersTable.update(user);
                    message.setPayload(new Payload()
                            .put("script", getClass().getName())
                            .put("step", 1)
                            .put("offset", offset)
                            .toString());
                    send(message, 1);
                    break;
                case 4:
                    new Thread(() -> uploadThread(message)).start();
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }

    private static void uploadThread(Message message){
        try {
            var user = UsersTable.findById(message.getFromId());
            var userActor = new UserActor(user.getId(), user.getToken());
            var options = user.getParameters().get("resave");
            var albums = user.getParameters().get("resave").get("albums").getAsJsonArray();
            var uploadsCount = 0;
            for (var albumObject : albums) {
                var album = albumObject.getAsJsonObject();
                if (!album.get("active").getAsBoolean()) continue;
                var title = album.get("title").getAsString();
                var ownerId = album.get("ownerid").getAsString();
                var ownerAlbumId = album.get("albumid").getAsString();
                var photoIds = album.get("photoids").getAsJsonArray();
                var totg = album.get("totg").getAsBoolean();
                var ownerAlbum = new Photos(Config.VK()).get(userActor)
                        .ownerId(Integer.valueOf(ownerId)).albumId(ownerAlbumId).count(0).offset(0).execute();
                if (ownerAlbum == null) continue;
                if (photoIds.size() >= ownerAlbum.getCount()) continue;
                List<Photo> ownerAlbumPhotoList = new ArrayList<>();
                List<AbstractQueryBuilder> queryList = new ArrayList<>();
                var offset = 0;
                while (ownerAlbum.getCount() - offset > 0) {
                    var batch = new Photos(Config.VK()).get(userActor)
                            .ownerId(Integer.valueOf(ownerId))
                            .albumId(ownerAlbumId)
                            .photoSizes(true)
                            .offset(offset)
                            .count(1000);
                    queryList.add(batch);
                    offset += 1000;
                    if (queryList.size() >= 5 || ownerAlbum.getCount() - offset <= 1000) {
                        var json = new Execute(Config.VK()).batch(userActor, queryList).execute();
                        var array = json.getAsJsonArray();
                        array.forEach(e -> {
                            var resp = new Gson().fromJson(e, GetResponse.class);
                            ownerAlbumPhotoList.addAll(resp.getItems());
                        });
                        queryList.clear();
                    }
                }

                if(totg){
                    TelegramBot tgBot = new TelegramBot(Config.TELEGRAM_BOT_TOKEN);
                    var tgChatId = user.getParameters().get("resave").get("tgchatid").getAsLong();
                    var tempUploadsCount = 0;
                    var urls = new HashMap<Integer, String>();
                    var lastTime = System.currentTimeMillis();
                    var photos = ownerAlbumPhotoList.stream()
                            .filter(p -> {
                                boolean b = true;
                                for(var id : photoIds){
                                    if(id.getAsInt() == (p.getId())){
                                        b = false;
                                        break;
                                    }
                                }
                                return b;
                            })
                            .collect(Collectors.toList());
                    for (var photo : photos) {
                        if (uploadsCount >= 500) {
                            break;
                        }
                        HttpURLConnection connection = null;
                        for (String src : photo.getSizes().stream()
                                .sorted((o1, o2) ->
                                        Integer.compare(
                                                o2.getWidth() * o2.getHeight(),
                                                o1.getWidth() * o1.getHeight()
                                        )
                                )
                                .map(Image::getUrl)
                                .collect(Collectors.toList())) {
                            var url = new URL(src);
                            connection = (HttpURLConnection) url.openConnection();
                            if (connection == null) {
                                LOG.warn("Connection: unknown, photo: " + src);
                                continue;
                            }
                            var code = connection.getResponseCode();
                            if (code != 200) {
                                LOG.warn("Response code: " + code + ", photo: " + src);
                                connection.disconnect();
                                continue;
                            }
                            urls.put(photo.getId(), src);
                            break;
                        }
                        if (connection == null) {
                            LOG.warn("Connection: unknown, photo: " + photo);
                            continue;
                        }

                        if(urls.size() == 10 || photos.indexOf(photo) == photos.size()-1){
                            var startTime = System.currentTimeMillis();
                            var dt = (lastTime - startTime)/1000;
                            if(dt < 30) dt = 30;
                            Thread.sleep(dt);
                            if(urls.size() == 1){
                                var res = tgBot.execute(new SendPhoto(tgChatId, new ArrayList<>(urls.values()).get(0)));
                                if(!res.isOk()){
                                    lastTime = System.currentTimeMillis();
                                    continue;
                                }
                                uploadsCount++;
                                tempUploadsCount++;
                                photoIds.add(new ArrayList<>(urls.keySet()).get(0));
                                user.getParameters().put("resave", options);
                                UsersTable.update(user);
                            }else{
                                var inputMediaGroup = urls.values().stream().map(InputMediaPhoto::new).toArray(InputMediaPhoto[]::new);
                                var res = tgBot.execute(new SendMediaGroup(tgChatId, inputMediaGroup));
                                if(!res.isOk()){
                                    lastTime = System.currentTimeMillis();
                                    continue;
                                }
                                uploadsCount+=10;
                                tempUploadsCount+=10;
                                urls.keySet().forEach(photoIds::add);
                                user.getParameters().put("resave", options);
                                UsersTable.update(user);
                            }
                        }

                        lastTime = System.currentTimeMillis();

                    }
                    if (uploadsCount >= 500) {
                        break;
                    }
                }else{
                    var userAlbums = new Photos(Config.VK()).getAlbums(userActor).ownerId(user.getId()).execute()
                            .getItems()
                            .stream()
                            .filter(a -> a.getDescription().matches(ownerId+"_"+ownerAlbumId))
                            .sorted((o1, o2) -> Integer.compare(o2.getSize(), o1.getSize()))
                            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
                    var albumToUpload = userAlbums.stream()
                            .filter(a-> (ownerId+"_"+ownerAlbumId).equals(a.getDescription()) && a.getSize() < 10000).findFirst()
                            .orElse(new Photos(Config.VK())
                                    .createAlbum(userActor, title)
                                    .description(ownerId+"_"+ownerAlbumId)
                                    .privacyView("only_me")
                                    .execute());
                    if(!userAlbums.contains(albumToUpload)) userAlbums.add(albumToUpload);
                    var uploadQuery = new Photos(Config.VK())
                            .getUploadServer(userActor)
                            .albumId(albumToUpload.getId());
                    PhotoUpload upload = uploadQuery.execute();
                    int tempUploadsCount = 0;
                    for (var photo : ownerAlbumPhotoList.stream()
                            .filter(p -> {
                                boolean b = true;
                                for(var id : photoIds){
                                    if(id.getAsInt() == (p.getId())){
                                        b = false;
                                        break;
                                    }
                                }
                                return b;
                            })
                            .collect(Collectors.toList())) {

                        if (uploadsCount >= 500) {
                            break;
                        }

                        if (albumToUpload.getSize() + tempUploadsCount >= 10000) {
                            albumToUpload.setSize(albumToUpload.getSize()+tempUploadsCount);
                            if(userAlbums.stream().noneMatch(a -> a.getDescription().equals(ownerId+"_"+ownerAlbumId) && a.getSize() < 10000)){
                                albumToUpload = new Photos(Config.VK())
                                        .createAlbum(userActor, albumToUpload.getTitle())
                                        .description(albumToUpload.getDescription())
                                        .privacyView("only_me")
                                        .execute();
                            }else{
                                albumToUpload = userAlbums.stream()
                                        .filter(a-> a.getSize() < 10000
                                                && a.getDescription().equals(ownerId+"_"+ownerAlbumId)).findFirst().get();
                            }
                            tempUploadsCount = 0;
                            userAlbums.add(albumToUpload);
                            uploadQuery = new Photos(Config.VK())
                                    .getUploadServer(userActor)
                                    .albumId(albumToUpload.getId());
                            upload = uploadQuery.execute();
                        }

                        long startTime = System.currentTimeMillis();
                        HttpURLConnection connection = null;
                        for (String src : photo.getSizes().stream()
                                .sorted((o1, o2) ->
                                        Integer.compare(
                                                o2.getWidth() * o2.getHeight(),
                                                o1.getWidth() * o1.getHeight()
                                        )
                                )
                                .map(Image::getUrl)
                                .collect(Collectors.toList())) {
                            var url = new URL(src);
                            connection = (HttpURLConnection) url.openConnection();
                            if (connection == null) {
                                LOG.warn("Connection: unknown, photo: " + src);
                                continue;
                            }
                            var code = connection.getResponseCode();
                            if (code != 200) {
                                LOG.warn("Response code: " + code + ", photo: " + src);
                                connection.disconnect();
                                continue;
                            }
                            break;
                        }
                        if (connection == null) {
                            LOG.warn("Connection: unknown, photo: " + photo);
                            continue;
                        }
                        File img = new File(message.getPeerId() + ".jpg");
                        FileUtils.copyURLToFile(connection.getURL(), img);
                        connection.disconnect();
                        PhotoUploadResponse resp = new Upload(Config.VK()).photo(upload.getUploadUrl(), img).execute();
                        var saveQuery = new Photos(Config.VK()).save(userActor)
                                .albumId(resp.getAid())
                                .hash(resp.getHash())
                                .photosList(resp.getPhotosList())
                                .server(resp.getServer())
                                .caption(String.valueOf(photo.getId()));
                        saveQuery.execute();
                        img.delete();

                        photoIds.add(photo.getId());
                        user.getParameters().put("resave", options);
                        UsersTable.update(user);

                        uploadsCount++;
                        tempUploadsCount++;
                        long endTime = System.currentTimeMillis();
                        long deltaTime = endTime - startTime;
                        if (deltaTime > 0 && deltaTime < 1000) {
                            Thread.sleep(deltaTime);
                        }
                    }
                    if (uploadsCount >= 500) {
                        break;
                    }
                }
            }
        }catch (ApiException | ClientException | MalformedURLException e){
            LOG.error(e);
        } catch (InterruptedException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static void uploadToVK(Message message){
        try {
            var user = UsersTable.findById(message.getFromId());
            var userActor = new UserActor(user.getId(), user.getToken());
            var userAlbums = new Photos(Config.VK()).getAlbums(userActor).ownerId(user.getId()).execute()
                    .getItems()
                    .stream()
                    .filter(album -> album.getDescription().matches("-?\\d+_-?\\d+_show_on"))
                    .sorted((o1, o2) -> Integer.compare(o2.getSize(), o1.getSize()))
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));

            var userAlbumsDescriptions = userAlbums.stream().map(PhotoAlbumFull::getDescription).distinct().collect(Collectors.toList());
            int uploadsCount = 0;
            for(var userAlbumsDescription : userAlbumsDescriptions){
                String ownerId = userAlbumsDescription.split("_")[0];
                String ownerAlbumId = userAlbumsDescription.split("_")[1];

                var ownerAlbum = new Photos(Config.VK()).get(userActor)
                        .ownerId(Integer.valueOf(ownerId)).albumId(ownerAlbumId).photoSizes(true).count(1).offset(0).execute();
                if (ownerAlbum == null) continue;
                if (userAlbums.stream()
                        .filter(a -> a.getDescription().equals(userAlbumsDescription))
                        .map(PhotoAlbumFull::getSize)
                        .reduce(0, Integer::sum) >= ownerAlbum.getCount()) continue;

                HashSet<Integer> userAlbumPhotoIds = new HashSet<>();
                userAlbums.stream()
                        .filter(a -> a.getDescription().matches(a.getDescription()))
                        .forEachOrdered(a -> {
                            try {
                                var userAlbumPhotos = new Photos(Config.VK()).get(userActor)
                                        .ownerId(user.getId()).albumId(String.valueOf(a.getId())).count(1).offset(0).execute();
                                var offset = 0;
                                List<AbstractQueryBuilder> queryList = new ArrayList<>();
                                while (userAlbumPhotos.getCount() - offset > 0) {
                                    var batch = new Photos(Config.VK()).get(userActor)
                                            .ownerId(user.getId())
                                            .albumId(String.valueOf(a.getId()))
                                            .offset(offset)
                                            .count(1000);
                                    queryList.add(batch);
                                    offset += 1000;
                                    if (queryList.size() >= 5 || userAlbumPhotos.getCount() - offset <= 1000) {
                                        var json = new Execute(Config.VK()).batch(userActor, queryList).execute();
                                        var array = json.getAsJsonArray();
                                        array.forEach(e -> {
                                            var resp = new Gson().fromJson(e, GetResponse.class);
                                            resp.getItems().forEach(photo -> {
                                                userAlbumPhotoIds.add(Integer.valueOf(photo.getText()));
                                            });
                                        });
                                        queryList.clear();
                                    }
                                }
                            } catch (ApiException | ClientException e) {
                                LOG.error(e);
                            }
                        });

                List<Photo> ownerAlbumPhotoList = new ArrayList<>();
                List<AbstractQueryBuilder> queryList = new ArrayList<>();
                var offset = 0;
                while (ownerAlbum.getCount() - offset > 0) {
                    var batch = new Photos(Config.VK()).get(userActor)
                            .ownerId(Integer.valueOf(ownerId))
                            .albumId(ownerAlbumId)
                            .photoSizes(true)
                            .offset(offset)
                            .count(1000);
                    queryList.add(batch);
                    offset += 1000;
                    if (queryList.size() >= 5 || ownerAlbum.getCount() - offset <= 1000) {
                        var json = new Execute(Config.VK()).batch(userActor, queryList).execute();
                        var array = json.getAsJsonArray();
                        array.forEach(e -> {
                            var resp = new Gson().fromJson(e, GetResponse.class);
                            ownerAlbumPhotoList.addAll(resp.getItems());
                        });
                        queryList.clear();
                    }
                }

                var albumToUpload = userAlbums.stream()
                        .filter(a-> userAlbumsDescription.equals(a.getDescription())).findFirst().get();
                if (albumToUpload.getSize() >= 10000) {
                    if(userAlbums.stream().noneMatch(a -> a.getDescription().equals(userAlbumsDescription) && a.getSize() < 10000)){
                        albumToUpload = new Photos(Config.VK())
                                .createAlbum(userActor, albumToUpload.getTitle())
                                .description(albumToUpload.getDescription())
                                .privacyView("only_me")
                                .execute();
                    }else{
                        albumToUpload = userAlbums.stream()
                                .filter(a-> a.getSize() < 10000
                                        && a.getDescription().equals(userAlbumsDescription)).findFirst().get();
                    }
                }
                var uploadQuery = new Photos(Config.VK())
                        .getUploadServer(userActor)
                        .albumId(albumToUpload.getId());
                PhotoUpload upload = uploadQuery.execute();
                int tempUploadsCount = 0;
                for (var photo : ownerAlbumPhotoList.stream()
                        .filter(p -> !userAlbumPhotoIds.contains(p.getId()))
                        .collect(Collectors.toList())) {

                    if (uploadsCount >= 500) {
                        break;
                    }

                    if (albumToUpload.getSize() + tempUploadsCount >= 10000) {
                        albumToUpload.setSize(albumToUpload.getSize()+tempUploadsCount);
                        if(userAlbums.stream().noneMatch(a -> a.getDescription().equals(userAlbumsDescription) && a.getSize() < 10000)){
                            albumToUpload = new Photos(Config.VK())
                                    .createAlbum(userActor, albumToUpload.getTitle())
                                    .description(albumToUpload.getDescription())
                                    .privacyView("only_me")
                                    .execute();
                        }else{
                            albumToUpload = userAlbums.stream()
                                    .filter(a-> a.getSize() < 10000
                                            && a.getDescription().equals(userAlbumsDescription)).findFirst().get();
                        }
                        tempUploadsCount = 0;
                        userAlbums.add(albumToUpload);
                        uploadQuery = new Photos(Config.VK())
                                .getUploadServer(userActor)
                                .albumId(albumToUpload.getId());
                        upload = uploadQuery.execute();
                    }

                    long startTime = System.currentTimeMillis();
                    HttpURLConnection connection = null;
                    for (String src : photo.getSizes().stream()
                            .sorted((o1, o2) ->
                                    Integer.compare(
                                            o2.getWidth() * o2.getHeight(),
                                            o1.getWidth() * o1.getHeight()
                                    )
                            )
                            .map(Image::getUrl)
                            .collect(Collectors.toList())) {
                        var url = new URL(src);
                        connection = (HttpURLConnection) url.openConnection();
                        if (connection == null) {
                            LOG.warn("Connection: unknown, photo: " + src);
                            continue;
                        }
                        var code = connection.getResponseCode();
                        if (code != 200) {
                            LOG.warn("Response code: " + code + ", photo: " + src);
                            connection.disconnect();
                            continue;
                        }
                        break;
                    }
                    if (connection == null) {
                        LOG.warn("Connection: unknown, photo: " + photo);
                        continue;
                    }
                    File img = new File(message.getPeerId() + ".jpg");
                    FileUtils.copyURLToFile(connection.getURL(), img);
                    connection.disconnect();
                    PhotoUploadResponse resp = new Upload(Config.VK()).photo(upload.getUploadUrl(), img).execute();
                    var saveQuery = new Photos(Config.VK()).save(userActor)
                            .albumId(resp.getAid())
                            .hash(resp.getHash())
                            .photosList(resp.getPhotosList())
                            .server(resp.getServer())
                            .caption(String.valueOf(photo.getId()));
                    saveQuery.execute();
                    img.delete();
                    uploadsCount++;
                    tempUploadsCount++;
                    long endTime = System.currentTimeMillis();
                    long deltaTime = endTime - startTime;
                    if (deltaTime > 0 && deltaTime < 1000) {
                        Thread.sleep(deltaTime);
                    }
                }
                if (uploadsCount >= 500) {
                    break;
                }
            }
            var object = new JsonObject();
            object.addProperty("date", System.currentTimeMillis());
            user.getParameters().put("resave", object);
            UsersTable.update(user);
        }catch (ApiException | ClientException | InterruptedException | IOException e){
            LOG.error(e);
        }
    }
}
