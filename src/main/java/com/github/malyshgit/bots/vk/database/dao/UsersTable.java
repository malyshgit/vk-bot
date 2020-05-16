package com.github.malyshgit.bots.vk.database.dao;

import com.github.malyshgit.bots.vk.database.PostgreSQL;
import com.github.malyshgit.bots.vk.database.models.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class UsersTable {
    private static final Logger LOG = LogManager.getLogger(UsersTable.class);
    private static final String name = "USERS";

    public final static String tableSQL = "CREATE TABLE IF NOT EXISTS "+name+" " +
            "(ID INT PRIMARY KEY     NOT NULL," +
            " TOKEN          TEXT," +
            " FIELDS         JSONB DEFAULT '[]'::jsonb);";

    public static void create(){
        try {
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute(tableSQL);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void drop(){
        try {
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute("DROP TABLE IF EXISTS "+name+";");
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static User findById(int id) {
        try {
            String sql = "SELECT * from "+name+" WHERE ID="+id;
            var statement = PostgreSQL.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                Integer userId = rs.getInt("ID");
                String userToken = rs.getString("TOKEN");
                var userFields = new JsonParser().parse(rs.getString("FIELDS")).getAsJsonArray();
                var user = new User(userId);
                user.setToken(userToken);
                var fields = new HashMap<String, JsonElement>();
                for(var e : userFields){
                    fields.putAll(e.getAsJsonObject().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }
                user.setFields(fields);
                return user;
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return null;
    }

    public static void save(User user) {
        try {
            var array = new JsonArray();
            user.getFields().forEach((k, v)->{
                JsonObject object = new JsonObject();
                object.add(k, v);
                array.add(object);
            });
            String sql = "INSERT INTO "+name
                            + "(ID, TOKEN, FIELDS) " + "VALUES"
                            + "("+user.getId()+", '"+user.getToken()+"', '"+array.toString()+"')";
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void update(User user) {
        try {
            JsonArray array = new JsonArray();
            user.getFields().forEach((k, v)->{
                JsonObject object = new JsonObject();
                object.add(k, v);
                array.add(object);
            });
            String sql = "UPDATE "+name+" SET token = '"+user.getToken()+"', fields = '"+array.toString()+"' WHERE id = "+user.getId()+";";
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void delete(User user) {
        try {
            String sql = "DELETE FROM "+name+" WHERE ID = "+user.getId()+";";
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * from "+name+";";
            var statement = PostgreSQL.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Integer userId = rs.getInt("ID");
                String userToken = rs.getString("TOKEN");
                var userFields = new JsonParser().parse(rs.getString("FIELDS")).getAsJsonArray();
                var user = new User(userId);
                user.setToken(userToken);
                var fields = new HashMap<String, JsonElement>();
                for(var e : userFields){
                    fields.putAll(e.getAsJsonObject().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }
                user.setFields(fields);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            LOG.error(e);
        }
        return users;
    }

}
