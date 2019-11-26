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
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.GetAlbumsResponse;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
                    new Messages(Config.VK)
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
                                            .setLabel("Далее"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Парсинг")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 1:
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 0 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Назад"))
                    ));
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 2 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Начать"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Отправьте ссылку на сообщество и нажмите \"Начать\"")
                            .keyboard(keyboard)
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    break;
                case 2:
                    var getByIdResponse = new Messages(Config.VK).getById(Config.GROUP,message.getId()-1).groupId(Config.GROUP_ID).execute();
                    var url = getByIdResponse.getItems().get(0).getText();
                    if(url == null || url.isEmpty()){
                        new Messages(Config.VK)
                                .send(Config.GROUP)
                                .message("Отправьте ссылку на сообщество и нажмите \"Начать\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    parseWall(message, url);
                    keyboard.setInline(true);
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.NEGATIVE)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            "{\"script\":\"" + getClass().getName() + "\"," +
                                                    "\"step\":" + 4 + "}"
                                    ).setType(KeyboardButtonActionType.TEXT)
                                            .setLabel("Остановить"))
                    ));
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .keyboard(keyboard)
                            .message("В любой момент можно остановить процесс")
                            .peerId(message.getPeerId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                    send(message, 0);
                    break;
                case 3:
                    pushPhotos(message);
                    break;
                case 4:
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
            thread = new Thread(this::run);
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
            thread.interrupt();
        }
    }

    private static void parseWall(Message message, String domain) {
        WallParserThread thread = new WallParserThread() {
            @Override
            void run() {
                parseWallThread(message, domain);
            }
        };
        threadHashMap.put(message.getFromId(), thread);
        thread.start();
    }
    private static void pushPhotos(Message message) {
        WallParserThread thread = new WallParserThread() {
            @Override
            void run() {
                pushPhotosThread(message);
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
            var query = new Wall(Config.VK).get(userActor)
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

            int mid = new Messages(Config.VK)
                    .send(Config.GROUP)
                    .peerId(message.getPeerId())
                    .message("Постов обработанно: 0")
                    .randomId(Utils.getRandomInt32())
                    .execute();

            int dt = size/10;
            int nextEdit = dt;
            while (offset < size) {
                if(!threadHashMap.get(message.getFromId()).isStarted()) break;
                if(offset > nextEdit){
                    new Messages(Config.VK)
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
                            var pres = photo.getSizes().stream()
                                    .max(Comparator.comparingInt(o -> o.getWidth() * o.getHeight()));
                            if (pres.isPresent()) {
                                var maxSize = pres.get();
                                String src = maxSize.getUrl();
                                sb.append(photo.getOwnerId())
                                        .append("_")
                                        .append(photo.getAlbumId())
                                        .append("_")
                                        .append(photo.getId())
                                        .append("_")
                                        .append(src)
                                        .append("\n");
                            }
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
            new Messages(Config.VK)
                    .edit(Config.GROUP, message.getPeerId(), mid)
                    .message("Постов обработанно: "+Math.min(offset, size)+"/"+size)
                    .execute();
            File urls = new File(domain + ".txt");
            FileUtils.write(urls, sb.toString(), StandardCharsets.UTF_8);
            var upload = new Docs(Config.VK).getMessagesUploadServer(Config.GROUP).peerId(message.getPeerId()).type(DocsType.DOC).execute();
            var resp = new Upload(Config.VK).doc(upload.getUploadUrl(), urls).execute();
            var save = new Docs(Config.VK).save(Config.GROUP, resp.getFile()).title(domain + ".txt").execute();
            urls.delete();
            mid = new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message(domain)
                    .attachment("doc" + save.getDoc().getOwnerId() + "_" + save.getDoc().getId())
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();
            Keyboard keyboard = new Keyboard();

            List<List<KeyboardButton>> buttons = new ArrayList<>();
            keyboard.setOneTime(false);
            keyboard.setButtons(buttons);
            keyboard.setInline(true);
            buttons.add(List.of(
                    new KeyboardButton()
                            .setColor(KeyboardButtonColor.DEFAULT)
                            .setAction(new KeyboardButtonAction().setPayload(
                                    "{\"script\":\"" + WallParser.class.getName() + "\"," +
                                            "\"step\":" + 3 + "}"
                            ).setType(KeyboardButtonActionType.TEXT)
                                    .setLabel("Заполнить свой альбом"))
            ));
            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .keyboard(keyboard)
                    .forwardMessages(mid)
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();
        } catch (ApiException | ClientException | IOException e) {
            LOG.error(e);
        }
    }
    private static void pushPhotosThread(Message message) {
        try {
            var doc = message.getFwdMessages().get(0).getAttachments().get(0).getDoc();
            List<String> lines = IOUtils.readLines(new URL(doc.getUrl()).openStream(), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            User user = Users.find(message.getFromId());
            UserActor userActor = new UserActor(message.getPeerId(), user.getToken());
            GetAlbumsResponse response = new Photos(Config.VK).getAlbums(userActor).ownerId(message.getFromId()).execute();
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
                            var json = new Execute(Config.VK).batch(userActor, queryList).execute();
                            var array = json.getAsJsonArray();
                            array.forEach(e -> {
                                var resp = new Gson().fromJson(e, GetResponse.class);
                                resp.getItems().forEach(photo -> {
                                    captions.add(photo.getText());
                                });
                            });
                            queryList.clear();
                        }
                        var batch = new Photos(Config.VK).get(userActor)
                                .ownerId(a.getOwnerId())
                                .albumId(String.valueOf(a.getId()))
                                .offset(i)
                                .count(1000);
                        queryList.add(batch);
                        Thread.sleep(1500);
                        i += 1000;
                    }
                    if(!queryList.isEmpty()){
                        var json = new Execute(Config.VK).batch(userActor, queryList).execute();
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
                    var albumQuery = new Photos(Config.VK)
                            .createAlbum(userActor, "AutoAlbum_"+albums.size())
                            .privacyView("only_me");
                    lastAlbum = albumQuery.execute();
                }
                offset = lastAlbum.getSize();
            }else{
                var albumQuery = new Photos(Config.VK).createAlbum(userActor, "AutoAlbum_0")
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

            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message(sb.toString())
                    .peerId(message.getFromId())
                    .randomId(Utils.getRandomInt32())
                    .execute();

            var uploadQuery = new Photos(Config.VK)
                    .getUploadServer(userActor)
                    .albumId(lastAlbum.getId());
            PhotoUpload upload = uploadQuery.execute();
            int mid = new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message("Прогресс: null")
                    .peerId(message.getFromId())
                    .randomId(Utils.getRandomInt32())
                    .execute();
            int savesCount = 0;
            int i = 0;
            for(String line : lines) {
                if(savesCount > 1000){
                    threadHashMap.get(message.getFromId()).stop();
                    new Messages(Config.VK)
                            .send(Config.GROUP)
                            .message("Заполнение остановленно.")
                            .peerId(message.getFromId())
                            .randomId(Utils.getRandomInt32())
                            .execute();
                }
                if(!threadHashMap.get(message.getFromId()).isStarted()) break;
                i++;
                if(i > 400){
                    new Messages(Config.VK)
                            .edit(Config.GROUP, message.getFromId(), mid)
                            .message("Прогресс: "+savesCount+"/"+lines.size())
                            .execute();
                    i = 0;
                }
                String[] lineParts = line.split("_");
                String owner = lineParts[0];
                String album = lineParts[1];
                String id = lineParts[2];
                String src = lineParts[3];

                String caption = owner+"_"+album+"_"+id;

                if(captions.contains(caption))continue;
                long startTime = System.currentTimeMillis();
                if(offset >= 10000){
                    var albumQuery = new Photos(Config.VK).createAlbum(userActor, "AutoAlbum_"+autoAlbumCount)
                            .privacyView("only_me");
                    lastAlbum = albumQuery.execute();
                    uploadQuery = new Photos(Config.VK)
                            .getUploadServer(userActor)
                            .albumId(lastAlbum.getId());
                    upload = uploadQuery.execute();
                    offset = 0;
                }
                File img = new File("temp.jpg");
                FileUtils.copyURLToFile(new URL(src), img);
                PhotoUploadResponse uplresponse = new Upload(Config.VK).photo(upload.getUploadUrl(), img).execute();
                List<Photo> photos = null;
                var saveQuery = new Photos(Config.VK).save(userActor)
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
                if(deltaTime > 0 && deltaTime < 1500){
                    Thread.sleep(deltaTime);
                }
            }
            new Messages(Config.VK)
                    .edit(Config.GROUP, message.getFromId(), mid)
                    .message("Прогресс: "+lines.size()+"/"+lines.size())
                    .execute();
        } catch (ApiException | ClientException | InterruptedException | IOException e) {
            try {
                new Messages(Config.VK)
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
