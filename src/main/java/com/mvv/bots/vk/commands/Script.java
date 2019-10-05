/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mvv.bots.vk.commands;

import com.vk.api.sdk.objects.messages.Message;
import com.mvv.bots.vk.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public interface Script {

    String smile();

    String key();

    String description();

    AccessMode accessMode();

    void send(Message message, Integer step);

    void update();

    static String cuteKey(String message, String key){
        if(message == null) return "";
        return Pattern.compile("^("+key+")", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(message).replaceAll("").trim();
    }

    static List<Script> getSortedList(Collection<Script> c){
        List<Script> l = new ArrayList<>(c);
        Comparator<Script> comp = Comparator.comparing(Script::key);
        l.sort(comp);
        return l;
    }

    static Script getByKey(String key){
        for(Script script : Config.SCRIPTS){
            if(Pattern.compile("^("+script.key()+")$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(key).matches()){
                return script;
            }else if(Pattern.compile("^("+script.key()+"\\s+).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(key).matches()){
                return script;
            }
        }
        return null;
    }

    static Script getByName(String classname){
        for(Script script : Config.SCRIPTS){
            if(script.getClass().getName().equalsIgnoreCase(classname)) return script;
        }
        return null;
    }

    static boolean containsByKey(String keys){
        for(Script script : Config.SCRIPTS){
            if(Pattern.compile("^("+script.key()+")$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(keys).matches()){
                return true;
            }else if(Pattern.compile("^("+script.key()+"\\s+).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(keys).matches()){
                return true;
            }
        }
        return false;
    }

}
