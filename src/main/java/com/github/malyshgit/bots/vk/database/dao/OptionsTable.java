package com.github.malyshgit.bots.vk.database.dao;

import com.github.malyshgit.bots.vk.database.models.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


public class OptionsTable {
    private static final Logger LOG = LogManager.getLogger(OptionsTable.class);
    private static final String name = "OPTIONS";

    public final static String tableString = "CREATE TABLE "+name+" " +
            "(ID               SERIAL  PRIMARY KEY," +
            " KEY              TEXT    NOT NULL UNIQUE," +
            " VALUE            TEXT    NOT NULL);";

    public static void create(){

    }

    public static Option findByKey(String key) {
        return null;
    }

    public static void save(Option option) {

    }

    public static void update(Option option) {

    }

    public static void delete(Option option) {

    }

    public static List<Option> findAll() {
        return null;
    }

}
