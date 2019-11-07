package com.mvv.bots.vk.database.tables;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.database.PostgreSQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Settings {
    private static final Logger LOG = LogManager.getLogger(Settings.class);
    private static final String name = "SETTINGS";

    public final static String tableString = "CREATE TABLE "+name+" " +
            "(KEY TEXT PRIMARY KEY     NOT NULL," +
            " VALUE            INT     NOT NULL);";

    public static void create(){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS "+name+";");
            statement.close();
            statement = PostgreSQL.getConnection().createStatement();
            statement.executeUpdate(tableString);
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static void update(Option option){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "UPDATE "+name+" SET VALUE='%s' WHERE KEY='%s';",
                    option.getValue(), option.getKey());
            LOG.debug(sql);
            statement.executeUpdate(sql);
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static void add(Option option){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "INSERT INTO "+name+" (KEY, VALUE) "
                                +"VALUES ('%s', '%s');",
                    option.getKey(), option.getValue());
            LOG.debug(sql);
            statement.executeUpdate(sql);
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static List<Option> findAll(){
        try {
            List<Option> options = new ArrayList<>();
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = "SELECT * FROM "+name+";";
            LOG.debug(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                Option option;
                String key = resultSet.getString("key");
                String value = resultSet.getString("value");
                option = new Option(key, value);
                options.add(option);
            }
            resultSet.close();
            statement.close();
            LOG.debug(options);
            return options;
        } catch ( Exception e ) {
            LOG.error(e);
        }
        return null;
    }

    public static Option find(String key){
        try {
            Option option;
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format("SELECT * FROM "+name+" WHERE KEY='%s';", key);
            LOG.debug(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                String value = resultSet.getString("value");
                option = new Option(key, value);
                LOG.debug(option);
                return option;
            }
            resultSet.close();
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
        return null;
    }

    public static boolean contains(String key){
        return find(key) != null;
    }

    public static class Option{
        private String key;
        private String value;

        public Option(String key, String value){
            this.key = key;
            this.value = value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Option{key="+key+", value="+value+"}";
        }
    }

}
