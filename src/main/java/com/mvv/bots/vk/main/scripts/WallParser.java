/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.main.scripts;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.users.User;
import com.mvv.bots.vk.database.tables.users.Users;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
import com.vk.api.sdk.actions.*;
import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.enums.DocsType;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.photos.Image;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.GetAlbumsResponse;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WallParser implements Script {

    @Override
    public String smile(){
        return "\uD83D\uDD0D";
    }

    @Override
    public String key(){
        return "парсинг";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - парсинг стены сообщества.";
    }

    @Override
    public AccessMode accessMode() {
        return AccessMode.USER;
    }

    @Override
    public void update() {
        Users.findAll().forEach(user -> {
            if(user.getParameters().has("wallparsernextpush")){
                var options = new JsonParser().parse(user.getParameters().get("wallparsernextpush")).getAsJsonObject();
                var doc = options.get("doc").getAsString();
                var date = options.get("date").getAsLong();
                var dt = System.currentTimeMillis() - date;
                if(dt >= DateUtils.MILLIS_PER_HOUR){
                    Message message = new Message();
                    message.setFromId(user.getId());
                    message.setPeerId(user.getId());
                    message.setPayload("{\"script\":\"" + WallParser.class.getName() + "\"," +
                            "\"step\":" + 2 + "," +
                            "\"doc\":\"" + doc + "\"}");
                    send(message, 2);
                }
            }
        });

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
                                            "{\"script\":\""+getClass().getName()+"\"," +
                                                    "\"step\":"+0+"}"
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
                    User user = Users.find(message.getFromId());
                    if(user.getToken() == null){
                        new Authorization().send(message, 0);
                        break;
                    }
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + ScriptList.class.getName() + "\"," +
                                                    "\"step\":" + 0 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 1 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Начать"))
                    ));
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Отправьте ссылку на сообщество и нажмите \"Начать\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1:
                    var getByIdResponse = new Messages(Config.VK()).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    var url = getByIdResponse.getItems().get(0).getText();
                    if(url == null || url.isEmpty()){
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Отправьте ссылку на сообщество и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    parseWall(message, url);
                    ScriptList.open(message);
                    break;
                case 2:
                    pushPhotos(message);
                    ScriptList.open(message);
                    break;
                case 3:
                    if(threadHashMap.containsKey(message.getFromId())) threadHashMap.get(message.getFromId()).stop();
                    break;
                default:
                    break;
            }
        }catch (ApiException | ClientException e){
            LOG.error(e);
        }
    }

    private static HashMap<Integer, WallParserThread> threadHashMap = new HashMap<>();

    private static abstract class WallParserThread{
        private Thread thread;
        private boolean started;

        WallParserThread(){
            started = false;
            thread = new Thread(()->{
                run();
                thread.interrupt();
            });
        }

        abstract void run();

        public boolean isStarted() {
            return started;
        }

        public void start(){
            started = true;
            thread.start();
        }

        public void stop(){
            started = false;
        }
    }

    private static void parseWall(Message message, String domain) {
        if(threadHashMap.containsKey(message.getFromId())) return;
        WallParserThread thread = new WallParserThread() {
            @Override
            void run() {
                parseWallThread(message, domain);
                threadHashMap.remove(message.getFromId());
            }
        };
        threadHashMap.put(message.getFromId(), thread);
        thread.start();
    }
    private static void pushPhotos(Message message) {
        if(threadHashMap.containsKey(message.getFromId())) return;
        WallParserThread thread = new WallParserThread() {
            @Override
            void run() {
                pushPhotosThread(message);
                threadHashMap.remove(message.getFromId());
            }
        };
        threadHashMap.put(message.getFromId(), thread);
        thread.start();
    }
    private static void parseWallThread(Message message, String domain){
        try {
            int offset = 0;
            int count = 100;
            User user = Users.find(message.getFromId());
            UserActor userActor = new UserActor(message.getPeerId(), user.getToken());
            var query = new Wall(Config.VK()).get(userActor)
                    .offset(offset)
                    .count(count);
            if (domain.matches("-\\d+")) {
                query.ownerId(Integer.valueOf(domain));
            } else if (domain.startsWith("http") || domain.startsWith("vk.com")) {
                domain = domain.replace("https://vk.com/", "");
                domain = domain.replace("http://vk.com/", "");
                domain = domain.replace("vk.com/", "");
                query.domain(domain);
            } else {
                query.domain(domain);
            }
            var response = query.execute();

            StringBuffer sb = new StringBuffer();

            int size = response.getCount();

            Keyboard keyboard = new Keyboard();
            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);
            keyboard.setInline(true);
            buttons.add(List.of(
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.NEGATIVE)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\"" + WallParser.class.getName() + "\"," +
                                            "\"step\":" + 3 + "}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Остановить"))
            ));

            int mid = new Messages(Config.VK())
                    .send(Config.GROUP)
                    .peerId(message.getPeerId())
                    .message("Постов обработанно: 0")
                    .randomId(Utils.getRandomInt32())
                    .execute();

            new Messages(Config.VK())
                    .send(Config.GROUP)
                    .peerId(message.getPeerId())
                    .keyboard(keyboard)
                    .message("В любой момент можно остановить.")
                    .randomId(Utils.getRandomInt32())
                    .execute();
            int dt = size/10;
            int nextEdit = dt;
            while (offset < size) {
                if(!threadHashMap.get(message.getFromId()).isStarted()) break;
                if(offset > nextEdit){
                    new Messages(Config.VK())
                            .edit(Config.GROUP, message.getPeerId(), mid)
                            .message("Постов обработанно: "+offset+"/"+size)
                            .execute();
                    nextEdit += dt;
                }
                response.getItems().forEach(post -> {
                    if (post == null || post.getAttachments() == null) return;
                    post.getAttachments().forEach(attachment -> {
                        if (attachment.getType().equals(WallpostAttachmentType.PHOTO)) {
                            Photo photo = attachment.getPhoto();
                            String sizes = photo.getSizes()
                                    .stream()
                                    .sorted((o1, o2) ->
                                            Integer.compare(
                                                    o2.getWidth()*o2.getHeight(),
                                                    o1.getWidth()*o1.getHeight()
                                            )
                                    )
                                    .map(Image::getUrl).collect(Collectors.joining(","));
                            sb.append(photo.getOwnerId())
                                    .append(" ")
                                    .append(post.getId())
                                    .append(" ")
                                    .append(photo.getId())
                                    .append(" ")
                                    .append(sizes)
                                    .append("\n");
                        }
                    });
                });
                offset += count;
                response = query.offset(offset)
                        .count(count)
                        .execute();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
            new Messages(Config.VK())
                    .edit(Config.GROUP, message.getPeerId(), mid)
                    .message("Постов обработанно: "+Math.min(offset, size)+"/"+size)
                    .execute();
            File urls = new File(message.getPeerId() + ".txt");
            FileUtils.write(urls, sb.toString(), StandardCharsets.UTF_8);
            var upload = new Docs(Config.VK()).getMessagesUploadServer(Config.GROUP).peerId(message.getPeerId()).type(DocsType.DOC).execute();
            var resp = new Upload(Config.VK()).doc(upload.getUploadUrl(), urls).execute();
            var save = new Docs(Config.VK()).save(Config.GROUP, resp.getFile()).title(domain + ".txt").execute();
            urls.delete();
            keyboard = new Keyboard();

            buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);
            keyboard.setInline(true);
            buttons.add(List.of(
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.DEFAULT)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\"" + WallParser.class.getName() + "\"," +
                                            "\"step\":" + 2 + "," +
                                            "\"doc\":\"" + save.getDoc().getUrl() + "\"}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Заполнить альбом сейчас"))
            ));
            new Messages(Config.VK())
                    .send(Config.GROUP)
                    .attachment("doc" + save.getDoc().getOwnerId() + "_" + save.getDoc().getId())
                    .keyboard(keyboard)
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();
        } catch (ApiException | ClientException | IOException e) {
            LOG.error(e);
        }
    }
    private static void pushPhotosThread(Message message) {
        try {
            var payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
            String doc = payload.get("doc").getAsString();
            List<String> lines = IOUtils.readLines(new URL(doc).openStream(), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            User user = Users.find(message.getFromId());
            UserActor userActor = new UserActor(message.getFromId(), user.getToken());

            user.getParameters().put("wallparsernextpush", "{\"doc\":\"" + doc + "\", \"date\":"+System.currentTimeMillis()+"}");
            Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());

            GetAlbumsResponse response = new Photos(Config.VK()).getAlbums(userActor).ownerId(message.getFromId()).execute();
            int offset = 0;
            List<PhotoAlbumFull> albums = new ArrayList<>();
            response.getItems().forEach(a -> {
                if(a.getTitle().startsWith("AutoAlbum_")){
                    albums.add(a);
                }
            });
            albums.sort((o1, o2) -> {
                int s1 = Integer.parseInt(o1.getTitle().replace("AutoAlbum_", ""));
                int s2 = Integer.parseInt(o2.getTitle().replace("AutoAlbum_", ""));
                return Integer.compare(s1, s2);
            });
            if(!albums.isEmpty()) sb.append("Альбомы:\n")
                    .append(albums.stream().map(a -> a.getTitle()+"("+a.getSize()+")")
                            .collect(Collectors.joining("\n")))
                    .append("\n");

            PhotoAlbumFull lastAlbum = null;

            HashSet<String> captions = new HashSet<>();

            if(!albums.isEmpty()){
                for(PhotoAlbumFull a : albums){
                    int i = 0;
                    List<AbstractQueryBuilder> queryList = new ArrayList<>();
                    while(i < a.getSize()) {
                        if(queryList.size() >= 5){
                            var json = new Execute(Config.VK()).batch(userActor, queryList).execute();
                            var array = json.getAsJsonArray();
                            array.forEach(e -> {
                                var resp = new Gson().fromJson(e, GetResponse.class);
                                resp.getItems().forEach(photo -> {
                                    captions.add(photo.getText());
                                });
                            });
                            queryList.clear();
                        }
                        var batch = new Photos(Config.VK()).get(userActor)
                                .ownerId(a.getOwnerId())
                                .albumId(String.valueOf(a.getId()))
                                .offset(i)
                                .count(1000);
                        queryList.add(batch);
                        Thread.sleep(1500);
                        i += 1000;
                    }
                    if(!queryList.isEmpty()){
                        var json = new Execute(Config.VK()).batch(userActor, queryList).execute();
                        var array = json.getAsJsonArray();
                        array.forEach(e -> {
                            var resp = new Gson().fromJson(e, GetResponse.class);
                            resp.getItems().forEach(photo -> {
                                captions.add(photo.getText());
                            });
                        });
                        queryList.clear();
                    }
                    if(a.getSize() >= 10000) continue;
                    if(lastAlbum == null) lastAlbum = a;
                }
                if(lastAlbum == null){
                    var albumQuery = new Photos(Config.VK())
                            .createAlbum(userActor, "AutoAlbum_"+albums.size())
                            .privacyView("only_me");
                    lastAlbum = albumQuery.execute();
                }
                offset = lastAlbum.getSize();
            }else{
                var albumQuery = new Photos(Config.VK()).createAlbum(userActor, "AutoAlbum_0")
                        .privacyView("only_me");
                lastAlbum = albumQuery.execute();
            }
            int autoAlbumCount = Integer.parseInt(lastAlbum.getTitle().replace("AutoAlbum_", ""))+1;

            sb.append("Текущий альбом: ")
                    .append(lastAlbum.getTitle()).append("\n");
            sb.append("Ссылок: ")
                    .append(lines.size()).append("\n");
            sb.append("Заполненно: ")
                    .append(captions.size()).append("\n");

            new Messages(Config.VK())
                    .send(Config.GROUP)
                    .message(sb.toString())
                    .peerId(message.getFromId())
                    .randomId(Utils.getRandomInt32())
                    .execute();

            var uploadQuery = new Photos(Config.VK())
                    .getUploadServer(userActor)
                    .albumId(lastAlbum.getId());
            PhotoUpload upload = uploadQuery.execute();
            Keyboard keyboard = new Keyboard();
            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);
            keyboard.setInline(true);
            buttons.add(List.of(
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.NEGATIVE)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\"" + WallParser.class.getName() + "\"," +
                                            "\"step\":" + 3 + "}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Остановить"))
            ));
            int mid = new Messages(Config.VK())
                    .send(Config.GROUP)
                    .message("Прогресс: 0")
                    .peerId(message.getFromId())
                    .randomId(Utils.getRandomInt32())
                    .execute();
            new Messages(Config.VK())
                    .send(Config.GROUP)
                    .peerId(message.getFromId())
                    .keyboard(keyboard)
                    .message("В любой момент можно остановить.")
                    .randomId(Utils.getRandomInt32())
                    .execute();
            int savesCount = 0;
            int nextProgress = 50;
            int skipCount = 0;
            for(String line : lines) {
                if(savesCount >= 500){
                    threadHashMap.get(message.getFromId()).stop();
                    new Messages(Config.VK())
                            .send(Config.GROUP)
                            .message("Заполнение завершенно. Следующая часть заполнится через час.")
                            .peerId(message.getFromId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                }
                if(!threadHashMap.get(message.getFromId()).isStarted()) break;
                if(savesCount >= nextProgress){
                    new Messages(Config.VK())
                            .edit(Config.GROUP, message.getFromId(), mid)
                            .message("Прогресс: "+savesCount+"/"+500)
                            .execute();
                    nextProgress += 50;
                }
                String[] lineParts = line.split(" ");
                String owner = lineParts[0];
                String post = lineParts[1];
                String id = lineParts[2];
                String sizesString = lineParts[3];
                String[] sizesArray = sizesString.split(",");

                String caption = owner+"_"+post+"_"+id;

                if(captions.contains(caption)){
                    LOG.debug("Skip photo: "+line);
                    continue;
                }
                long startTime = System.currentTimeMillis();
                if(offset >= 10000){
                    var albumQuery = new Photos(Config.VK()).createAlbum(userActor, "AutoAlbum_"+autoAlbumCount)
                            .privacyView("only_me");
                    lastAlbum = albumQuery.execute();
                    uploadQuery = new Photos(Config.VK())
                            .getUploadServer(userActor)
                            .albumId(lastAlbum.getId());
                    upload = uploadQuery.execute();
                    offset = 0;
                }
                HttpURLConnection connection = null;
                for(String src : sizesArray) {
                    var url = new URL(src);
                    connection = (HttpURLConnection) url.openConnection();
                    var code = connection.getResponseCode();
                    if (code != 200) {
                        LOG.debug("Response code: " + code + ", photo: " + line);
                        connection.disconnect();
                        continue;
                    }
                    break;
                }
                if(connection == null){
                    skipCount++;
                    continue;
                }
                File img = new File(message.getPeerId()+".jpg");
                FileUtils.copyURLToFile(connection.getURL(), img);
                connection.disconnect();
                PhotoUploadResponse uplresponse = new Upload(Config.VK()).photo(upload.getUploadUrl(), img).execute();
                var saveQuery = new Photos(Config.VK()).save(userActor)
                        .albumId(uplresponse.getAid())
                        .hash(uplresponse.getHash())
                        .photosList(uplresponse.getPhotosList())
                        .server(uplresponse.getServer())
                        .caption(caption);
                saveQuery.execute();

                img.delete();
                savesCount++;
                offset++;
                long endTime = System.currentTimeMillis();
                long deltaTime = endTime - startTime;
                if(deltaTime > 0 && deltaTime < 1000){
                    Thread.sleep(deltaTime);
                }
            }
            if(lines.size() <= (captions.size()+savesCount+skipCount)){
                user.getParameters().remove("wallparsernextpush");
                Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                new Messages(Config.VK())
                        .edit(Config.GROUP, message.getFromId(), mid)
                        .message("Заполненно.")
                        .execute();
            }else{
                user.getParameters().put("wallparsernextpush", "{\"doc\":\"" + doc + "\", \"date\":"+System.currentTimeMillis()+"}");
                Users.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                new Messages(Config.VK())
                        .edit(Config.GROUP, message.getFromId(), mid)
                        .message("Заполненно: "+savesCount)
                        .execute();
            }
            LOG.debug("Skip count: "+skipCount);
        } catch (ApiException | ClientException | InterruptedException | IOException e) {
            try {
                new Messages(Config.VK())
                        .send(Config.GROUP)
                        .message("Заполнение остановленно с ошибкой.\n"+e.getMessage())
                        .peerId(message.getFromId())
                        .randomId(Utils.getRandomInt32())
                        .execute();
            } catch (ApiException | ClientException ex) {
                LOG.error(e);
            }
            LOG.error(e);
        }
    }
}
