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
import com.vk.api.sdk.objects.messages.*;
import com.mvv.bots.vk.Config;

import java.util.regex.PatternSyntaxException;

/**
 *
 * @author I1PABIJJA
 */
public class Cover implements Script {

    @Override
    public String smile(){
        return "\uD83D\uDD27";
    }

    @Override
    public String key(){
        return "обложка";
    }

    @Override
    public String description(){
        return smile()+"["+ key()+"] - обложка.";
    }

    @Override
    public AccessMode accessMode(){
        return AccessMode.ADMIN;
    }

    @Override
    public void update() {

    }

    @Override
    public void send(Message message, Integer step) {
        try{
            if((message.getPeerId() >= 2000000000) || !message.getPeerId().equals(Config.ADMIN_ID)){
                return;
            }

            new Messages(Config.VK)
                    .send(Config.GROUP)
                    .message("Обложка")
                    .peerId(message.getPeerId())
                    .randomId(Utils.getRandomInt32())
                    .execute();

        }catch (PatternSyntaxException | ApiException | ClientException e){
            LOG.error(e);
        }


    }

    /*public static JSONObject getCoverJson(){
        String resString = DataBase.selectString(DataBase.settings_db, "cover", null, null, false);
        if(resString == null){
            return null;
        }else {
            return new JSONObject(resString);
        }
    }

    public static void setCoverJson(JSONObject json){
        DataBase.update(DataBase.settings_db, "cover", "'"+json.toString()+"'", null, null, false);
    }

    public static void plusUse(int user_id){
        int use = DataBase.selectInteger(DataBase.users_db, "use", "id", user_id, false);
        DataBase.update(DataBase.users_db, "use", (use+1), "id", user_id, false);
    }

    public static int getMaxUseUser(){
        return DataBase.selectInteger(DataBase.users_db, "id", "use", "(SELECT MAX (use) FROM "+ DataBase.users_db+")", false);
    }

    public static int getLastSubscriber(){
        return DataBase.selectInteger(DataBase.users_db, "id", "subdate", "(SELECT MAX (subdate) FROM "+ DataBase.users_db+")", false);
    }

    public static void setGroupCover() {
        try {
            JSONObject cover = getCoverJson();

            if(cover == null){
                return;
            }

            BufferedImage result = new BufferedImage(1590, 400, BufferedImage.TYPE_INT_ARGB);

            LinkedHashMap<Integer, BufferedImage> images = new LinkedHashMap<>();

            BufferedImage tmp;
            Graphics2D g2;

            for(String object : cover.keySet()){
                JSONObject jsonObject = cover.getJSONObject(object);

                int layer;
                if(jsonObject.has("layer")){
                    layer = jsonObject.getInt("layer");
                }else{
                    layer = images.size();
                }
                while(images.containsKey(layer)){
                    layer++;
                }

                int x = jsonObject.getInt("x");
                int y = jsonObject.getInt("y");
                int w = jsonObject.getInt("w");
                int h = jsonObject.getInt("h");
                String[] bgcolorA = jsonObject.getString("bgcolor").split(",");
                Color bgcolor = new Color(Integer.decode(bgcolorA[0]),Integer.decode(bgcolorA[1]),Integer.decode(bgcolorA[2]),Integer.decode(bgcolorA[3]));
                String content;
                Color textcolor;
                Utils.Align textalign;
                int fontsize;
                if(jsonObject.has("content")){
                    content = jsonObject.getString("content");
                    if( jsonObject.has("textcolor")) {
                        String[] colorA = jsonObject.getString("textcolor").split(",");
                        textcolor = new Color(Integer.decode(colorA[0]), Integer.decode(colorA[1]), Integer.decode(colorA[2]), Integer.decode(colorA[3]));
                    }else{
                        textcolor = Color.WHITE;
                    }

                    if( jsonObject.has("textalign")) {
                        if(Utils.Align.valueOf(jsonObject.getString("textalign")) != null) {
                            textalign = Utils.Align.valueOf(jsonObject.getString("textalign"));
                        }else{
                            textalign = Utils.Align.CENTER;
                        }
                    }else{
                        textalign = Utils.Align.CENTER;
                    }

                    if( jsonObject.has("fontsize")) {
                        fontsize = jsonObject.getInt("fontsize");
                    }else{
                        fontsize = h;
                    }
                }else{
                    content = null;
                    textcolor = null;
                    textalign = Utils.Align.CENTER;
                    fontsize = 0;
                }

                tmp = new BufferedImage(1590, 400, BufferedImage.TYPE_INT_ARGB);
                g2 = (Graphics2D) tmp.getGraphics();
                Utils.applyQualityRenderingHints(g2);
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, 1590, 400);
                g2.setComposite(AlphaComposite.Src);
                g2.setColor(bgcolor);
                g2.fillRect(x, y, w, h);

                String shape;
                if(jsonObject.has("shape")){
                    shape = jsonObject.getString("shape");
                    if(!shape.matches("(oval|round)")){
                        shape = null;
                    }
                }else{
                    shape = null;
                }

                if(content != null) {
                    if (content.startsWith("http")) {
                        BufferedImage tmp1 = ImageIO.read(new URL(content));
                        g2.drawImage(tmp1, x, y, w, h, null);
                    } else {
                        g2.setColor(textcolor);
                        g2.setFont(Config.FONT.deriveFont(Font.TRUETYPE_FONT, fontsize));
                        Utils.drawIntoRect(content, new Rectangle(x,y,w,h), textalign, g2);
                    }
                }

                if(shape != null) {
                    BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = mask.createGraphics();
                    Utils.applyQualityRenderingHints(g2d);
                    if (shape.equalsIgnoreCase("oval")) {
                        g2d.fillOval(0, 0, w - 1, h - 1);
                    } else if (shape.equalsIgnoreCase("round")){
                        g2d.fillRoundRect(0, 0, w, h, w / 10, h / 10);
                    }else{
                        g2d.fillRect(0, 0, w, h);
                    }
                    g2d.dispose();

                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
                    g2.drawImage(mask, x, y, null);
                }


                g2.dispose();
                images.put(layer, tmp);
            }

            g2 = (Graphics2D) result.getGraphics();
            Utils.applyQualityRenderingHints(g2);
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, 1590, 400);
            g2.setComposite(AlphaComposite.SrcOver);

            TreeMap<Integer, BufferedImage> copy = new TreeMap<>(images);
            images.clear();
            images.putAll(copy);

            for(Integer biIndex : images.keySet()){
                BufferedImage image = images.get(biIndex);
                g2.drawImage(image, 0, 0, 1590, 400, null);
            }

            g2.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ImageIO.write(result, "PNG", out);

            JSONObject upload_response = new JSONObject(Photos.getOwnerCoverPhotoUploadServer("group_id="+Config.GROUP_ID+"&crop_x2=1590&crop_y2=400")).getJSONObject("response");
            String upload_url = upload_response.getString("upload_url");
            MultipartUtility multipart = new MultipartUtility(upload_url, "UTF-8");
            multipart.addFilePart("groupCover.png","photo", new ByteArrayInputStream(out.toByteArray()));
            String multipart_str = multipart.finish();
            String photo = new JSONObject(multipart_str).getString("photo");
            String hash = new JSONObject(multipart_str).getString("hash");
            Photos.saveOwnerCoverPhoto("photo="+photo+"&hash="+hash);
        } catch (JSONException | IOException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    private class GroupCover{
        HashMap<String, String> commands;

        public GroupCover(){
            commands = new HashMap<>();
        }

        public GroupCover(String json){
            commands = new HashMap<>();
            JsonObject object = new JsonParser().parse(json).getAsJsonObject();
            object.entrySet().forEach(e -> commands.put(e.getKey(),e.getValue().getAsString()));
        }


    }*/
    
}
