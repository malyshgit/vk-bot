package com.mvv.bots.vk.main.scripts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.tables.users.User;
import com.mvv.bots.vk.database.tables.users.UsersTable;
import com.mvv.bots.vk.main.AccessMode;
import com.mvv.bots.vk.main.Script;
import com.mvv.bots.vk.utils.Utils;
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
                if(user.getParameters().has("resave")){
                    var options = new JsonParser().parse(user.getParameters().get("resave")).getAsJsonObject();
                    var date = options.get("date").getAsLong();
                    var dt = System.currentTimeMillis() - date;
                    if(dt >= DateUtils.MILLIS_PER_HOUR){
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
                        UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                        send(message, 4);
                    }
                }else{
                    JsonObject object = new JsonObject();
                    object.addProperty("date", System.currentTimeMillis());
                    user.getParameters().put("resave", object);
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
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
                    User user = UsersTable.find(message.getFromId());
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
                    buttons.add(List.of(
                            new KeyboardButton()
                                    .setColor(KeyboardButtonColor.DEFAULT)
                                    .setAction(new KeyboardButtonAction().setPayload(
                                            new Payload()
                                                    .put("script", getClass().getName())
                                                    .put("step", 10)
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
                case 10:
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
                    user = UsersTable.find(message.getFromId());
                    UserActor userActor = new UserActor(user.getId(), user.getToken());
                    var query = new Wall(Config.VK()).get(userActor);
                    String wallAvatarUrl;
                    String wallName;

                    var albumInfo = url.substring(20).split("_");
                    if(albumInfo[0].startsWith("-")){
                        var groupProfile = new Groups(Config.VK()).getById(userActor).groupId(albumInfo[0].substring(1)).execute();
                        wallAvatarUrl = groupProfile.get(0).getPhoto200();
                        wallName = groupProfile.get(0).getName();
                    }else{
                        var userProfile = new Users(Config.VK()).get(userActor).fields(Fields.PHOTO_200).userIds(albumInfo[0]).execute();
                        wallAvatarUrl = userProfile.get(0).getPhoto200();
                        wallName = userProfile.get(0).getFirstName()+" "+userProfile.get(0).getLastName();
                    }
                    query.ownerId(Integer.valueOf(albumInfo[0]));

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

                    new Photos(Config.VK())
                            .createAlbum(userActor, wallName+"("+albumDesc+")")
                            .description(albumInfo[0]+"_"+albumInfo[1]+"_show_on")
                            .privacyView("only_me")
                            .execute();
                    JsonObject object = new JsonObject();
                    object.addProperty("date", System.currentTimeMillis());
                    user.getParameters().put("resave", object);
                    UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
                    new Messages(Config.VK())
                        .send(Config.GROUP)
                        .message("Альбом создан.")
                        .peerId(message.getPeerId())
                        .randomId(Utils.getRandomInt32())
                        .execute();
                    send(message, 1);
                    break;
                case 1:
                    user = UsersTable.find(message.getFromId());
                    userActor = new UserActor(user.getId(), user.getToken());
                    var userAlbums = new Photos(Config.VK()).getAlbums(userActor).ownerId(user.getId()).execute();
                    var tempList = new HashSet<String>();
                    var list = userAlbums.getItems()
                            .stream()
                            .filter(album -> album.getDescription().matches("-?\\d+_-?\\d+_show_(on|off)"))
                            .filter(album -> {
                                if(tempList.contains(album.getDescription())) return false;
                                tempList.add(album.getDescription());
                                return true;
                            })
                            .collect(Collectors.toList());
                    if(list.size() < 1){
                        new Messages(Config.VK())
                                .send(Config.GROUP)
                                .message("Отправьте ссылку на альбом и нажмите \"Добавить\"")
                                .peerId(message.getPeerId())
                                .randomId(Utils.getRandomInt32())
                                .execute();
                        return;
                    }
                    List<TemplateElement> elements = new ArrayList<>();
                    template.setElements(elements);
                    var payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    var offset = payload.has("offset") ? payload.get("offset").getAsInt() : 0;

                    var nextOffset = offset;
                    for(var i = 0; elements.size() < 10; i++) {
                        if (offset + i > list.size() - 1) {
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
                        if (i == 9 && nextOffset < list.size()-1) {
                            int lastOffset = 0;
                            while(lastOffset < list.size()-lastOffset){
                                if(lastOffset == 0 || lastOffset == list.size()){
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
                            var album = list.get(offset + i);
                            var desc = album.getDescription().split("_");
                            elements.add(new TemplateElement()
                                    .setTitle(album.getTitle())
                                    .setDescription(album.getDescription())
                                    .setAction(new TemplateElementAction()
                                            .setType(TemplateElementActionType.OPEN_LINK)
                                            .setLink("https://vk.com/album"+desc[0]+"_"+desc[1])
                                    )
                                    .setButtons(List.of(
                                            new KeyboardButton()
                                                    .setColor(desc[3].equals("on") ? KeyboardButtonColor.POSITIVE : KeyboardButtonColor.NEGATIVE)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 2)
                                                                    .put("offset", offset)
                                                                    .put("auto", desc[3].equals("on") ? "off" : "on")
                                                                    .put("desc", album.getDescription())
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("Заполнение")),
                                            new KeyboardButton()
                                                    .setColor(KeyboardButtonColor.DEFAULT)
                                                    .setAction(new KeyboardButtonAction().setPayload(
                                                            new Payload()
                                                                    .put("script", getClass().getName())
                                                                    .put("step", 3)
                                                                    .put("desc", album.getDescription())
                                                                    .toString()
                                                    ).setType(KeyboardButtonActionType.TEXT)
                                                            .setLabel("Не показывать"))
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
                    user = UsersTable.find(message.getFromId());
                    userActor = new UserActor(user.getId(), user.getToken());
                    payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    var auto = payload.get("auto").getAsString();
                    var desc = payload.get("desc").getAsString();
                    offset = payload.get("offset").getAsInt();
                    userAlbums = new Photos(Config.VK()).getAlbums(userActor).ownerId(user.getId()).execute();
                    list = userAlbums.getItems()
                            .stream()
                            .filter(album -> album.getDescription().matches(desc))
                            .collect(Collectors.toList());
                    for(var album : list){
                        var newDesc = album.getDescription().split("_");
                        newDesc[3] = auto;
                        new Photos(Config.VK())
                                .editAlbum(userActor, album.getId())
                                .description(String.join( "_", newDesc))
                                .execute();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    message.setPayload(new Payload()
                            .put("script", getClass().getName())
                            .put("step", 1)
                            .put("offset", offset)
                            .toString());
                    send(message, 1);
                    break;
                case 3:
                    user = UsersTable.find(message.getFromId());
                    userActor = new UserActor(user.getId(), user.getToken());
                    userAlbums = new Photos(Config.VK()).getAlbums(userActor).ownerId(user.getId()).execute();
                    payload = new JsonParser().parse(message.getPayload()).getAsJsonObject();
                    desc = payload.get("desc").getAsString();
                    userAlbums.getItems()
                            .stream()
                            .filter(album -> album.getDescription().equals(desc))
                            .forEach(album -> {
                        try {
                            var newDesc = album.getDescription().split("_");
                            newDesc[2] = "hide";
                            new Photos(Config.VK())
                                    .editAlbum(userActor, album.getId())
                                    .description(String.join("_", newDesc))
                                    .execute();
                        } catch (ApiException | ClientException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
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
            var user = UsersTable.find(message.getFromId());
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
                                        Thread.sleep(1500);
                                    }
                                }
                            } catch (ApiException | ClientException | InterruptedException e) {
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
                        Thread.sleep(1500);
                    }
                }

                var albumToUpload = userAlbums.stream()
                        .filter(a-> userAlbumsDescription.equals(a.getDescription())).findFirst().orElse(null);
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
                        userAlbums.stream().filter(a -> a.equals(albumToUpload)).findFirst().get().setSize(albumToUpload.getSize()+tempUploadsCount);
                        //albumToUpload.setSize(albumToUpload.getSize()+tempUploadsCount);
                        if(userAlbums.stream().noneMatch(a -> a.getDescription().equals(userAlbumsDescription) && a.getSize() < 10000)){
                            albumToUpload = new Photos(Config.VK())
                                    .createAlbum(userActor, albumToUpload.getTitle())
                                    .description(albumToUpload.getDescription())
                                    .privacyView("only_me")
                                    .execute();
                        }else{
                            tempUploadsCount = 0;
                            albumToUpload = userAlbums.stream()
                                    .filter(a-> a.getSize() < 10000
                                            && userAlbumsDescription.equals(a.getDescription())).findFirst().get();
                                    /*.orElse(new Photos(Config.VK())
                                            .createAlbum(userActor, albumToUpload.getTitle())
                                            .description(albumToUpload.getDescription())
                                            .privacyView("only_me")
                                            .execute());*/
                        }
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
                            LOG.debug("Connection: unknown, photo: " + src);
                            continue;
                        }
                        var code = connection.getResponseCode();
                        if (code != 200) {
                            LOG.debug("Response code: " + code + ", photo: " + src);
                            connection.disconnect();
                            continue;
                        }
                        break;
                    }
                    if (connection == null) {
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
            UsersTable.update(user.getId(), "PARAMETERS", user.getParameters().toString());
        }catch (ApiException | ClientException | InterruptedException | IOException e){
            LOG.error(e);
        }
    }
}
