/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.malyshgit.bots.vk.main;

import com.github.malyshgit.bots.vk.Config;
import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public interface Command {
    Logger LOG = LogManager.getLogger(Command.class);

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

    static List<Command> getSortedList(Collection<Command> c){
        List<Command> l = new ArrayList<>(c);
        Comparator<Command> comp = Comparator.comparing(Command::key);
        l.sort(comp);
        return l;
    }

    static Command getByKey(String key){
        for(Command command : Config.COMMANDS){
            if(Pattern.compile("^("+ command.key()+")$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(key).matches()){
                return command;
            }else if(Pattern.compile("^("+ command.key()+"\\s+).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(key).matches()){
                return command;
            }
        }
        return null;
    }

    static Command getByName(String classname){
        for(Command command : Config.COMMANDS){
            if(command.getClass().getName().equalsIgnoreCase(classname)) return command;
        }
        return null;
    }

    static boolean containsByKey(String keys){
        for(Command command : Config.COMMANDS){
            if(Pattern.compile("^("+ command.key()+")$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(keys).matches()){
                return true;
            }else if(Pattern.compile("^("+ command.key()+"\\s+).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL).matcher(keys).matches()){
                return true;
            }
        }
        return false;
    }

}
