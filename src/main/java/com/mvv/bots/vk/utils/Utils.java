package com.mvv.bots.vk.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static final Logger LOG = LogManager.getLogger(Utils.class);

    public static int getRandom(int f, int l){
        return ThreadLocalRandom.current().nextInt(f, l+1);
    }

    public static int getRandomInt32(){
        return ThreadLocalRandom.current().nextInt();
    }

    public static long getRandomInt64(){
        return ThreadLocalRandom.current().nextLong();
    }

    public static void applyQualityRenderingHints(Graphics2D g2d) {

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    }

    public static enum Align{
        LEFT,
        RIGHT,
        CENTER,
        UP,
        DOWN,
        UPLEFT,
        UPRIGHT,
        DOWNLEFT,
        DOWNRIGHT
    }

    public static void drawIntoRect(String s, Rectangle r, Align align, Graphics2D g){
        String[] words = s.trim().replace("-", " - ").split(" ");
        StringBuilder sb = new StringBuilder();
        LinkedList<String> lines = new LinkedList<>();

        if(g.getFontMetrics().stringWidth(s)*g.getFontMetrics().getAscent() > r.width*r.height){
            g.setFont(g.getFont().deriveFont(g.getFont().getSize()*(r.width*r.height/g.getFontMetrics().stringWidth(s))/g.getFontMetrics().getAscent()+0.0f));
        }
        for(String word : words){
            if(g.getFontMetrics().stringWidth(sb.toString()+word) > r.width){
                if(!sb.toString().trim().isEmpty()) {
                    lines.addLast(sb.toString().trim());
                    sb = new StringBuilder();
                    sb.append(word).append(" ");
                }
            }else{
                sb.append(word).append(" ");
            }
        }
        lines.addLast(sb.toString().trim());

        int lineCount = 1;
        float blockH = lines.size()*g.getFontMetrics().getAscent();
        float hLine = g.getFontMetrics().getAscent();
        if(blockH > r.height){
            g.setFont(g.getFont().deriveFont(g.getFont().getSize()*(r.height/blockH)));
            blockH = lines.size()*g.getFontMetrics().getAscent();
            hLine = g.getFontMetrics().getAscent();
        }
        blockH -= g.getFontMetrics().getDescent()/2;
        hLine -= g.getFontMetrics().getDescent()/2;
        for(String line : lines){
            int fW = g.getFontMetrics().stringWidth(line);
            switch (align){
                case LEFT: g.drawString(line, r.x, r.y + (r.height-blockH)/2 + hLine*lineCount); break;
                case RIGHT: g.drawString(line, r.x + r.width - fW, r.y + (r.height-blockH)/2 + hLine*lineCount); break;
                case UP: g.drawString(line, r.x + r.width/2 - fW/2, r.y + hLine*lineCount); break;
                case DOWN: g.drawString(line, r.x + r.width/2 - fW/2, r.y + r.height - blockH + hLine*lineCount); break;
                case CENTER: g.drawString(line, r.x + r.width/2 - fW/2, r.y + (r.height-blockH)/2 + hLine*lineCount); break;
                case UPLEFT: g.drawString(line, r.x, r.y + hLine*lineCount); break;
                case UPRIGHT: g.drawString(line, r.x + r.width - fW, r.y + hLine*lineCount); break;
                case DOWNLEFT: g.drawString(line, r.x, r.y + r.height - blockH + hLine*lineCount); break;
                case DOWNRIGHT: g.drawString(line, r.x + r.width - fW, r.y + r.height - blockH + hLine*lineCount); break;
                default: g.drawString(line, r.x + r.width/2 - fW/2, r.y + (r.height-blockH)/2 + hLine*lineCount); break;
            }
            lineCount++;
        }
    }

}
