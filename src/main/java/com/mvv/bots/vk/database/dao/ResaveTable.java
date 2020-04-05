package com.mvv.bots.vk.database.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.database.PostgreSQL;
import com.mvv.bots.vk.database.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class ResaveTable {
    private static final Logger LOG = LogManager.getLogger(ResaveTable.class);
    private static final String name = "RESAVE";

    public final static String tableSQL = "CREATE TABLE IF NOT EXISTS "+name+" " +
            "(ID SERIAL PRIMARY KEY," +
            " USERID           INT NOT NULL," +
            " OWNERID          INT NOT NULL," +
            " ALBUMID          INT NOT NULL," +
            " PHOTOIDS         INT[] DEFAULT ARRAY[]::int[]);";

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

    public static List<JsonObject> getAlbumsByUserId(int userId) {
        try {
            String sql = "SELECT * FROM "+name+" WHERE USERID="+userId+";";
            var statement = PostgreSQL.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            var list = new ArrayList<JsonObject>();
            while (rs.next()) {
                var object = new JsonObject();
                object.addProperty("ownerid", rs.getInt("ownerid"));
                object.addProperty("albumid", rs.getInt("albumid"));
                var photoids = (Integer[])rs.getArray("photoids").getArray();
                JsonArray array = new JsonArray();
                for(var pid : photoids){
                    array.add(pid);
                }
                object.add("photoids", array);
                list.add(object);
            }
            return list;
        } catch (SQLException e) {
            LOG.error(e);
        }
        return new ArrayList<>();
    }

    public static int getAlbumPhotosCount(int userId, int ownerId, int ownerAlbumId) {
        try {
            String sql = "SELECT array_length(ARRAY(SELECT DISTINCT unnest (photoids)), 1)::int FROM "+name+" WHERE USERID = "+userId+" AND OWNERID = "+ownerId+" AND ALBUMID = "+ownerAlbumId+";";
            var statement = PostgreSQL.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()){
                return rs.getInt("array_length");
            }
            return -1;
        } catch (SQLException e) {
            LOG.error(e);
        }
        return -1;
    }

    public static void addPhotoIds(int userId, int ownerId, int ownerAlbumId, Integer... photoIds){
        if(!containsUser(userId)){
            insert(userId, ownerId, ownerAlbumId, photoIds);
            return;
        }
        if(!containsAlbum(userId, ownerId, ownerAlbumId)){
            insert(userId, ownerId, ownerAlbumId, photoIds);
            return;
        }
        try {
            String sql = "UPDATE "+name+" SET photoids = photoids || '{"+List.of(photoIds).stream().map(String::valueOf).collect(Collectors.joining(","))+"}'" +
                    " WHERE USERID = "+userId +
                    " AND OWNERID = "+ownerId+
                    " AND ALBUMID = "+ownerAlbumId+";";
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static boolean containsUser(int userId) {
        try {
            String sql = "SELECT * FROM "+name+" WHERE USERID = "+userId+";";
            var statement = PostgreSQL.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return false;
    }

    public static boolean containsAlbum(int userId, int ownerId, int ownerAlbumId) {
        try {
            String sql = "SELECT * FROM "+name+" WHERE USERID = "+userId+" AND OWNERID = "+ownerId+" AND ALBUMID = "+ownerAlbumId+";";
            var statement = PostgreSQL.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return false;
    }

    public static void insert(int userId, int ownerId, int albumId, Integer... photoIds) {
        try {
            String sql = "INSERT INTO "+name
                    + "(USERID, OWNERID, ALBUMID, PHOTOIDS) " + "VALUES"
                    + "("+userId+", "+ownerId+", "+albumId+", '{"+ List.of(photoIds).stream().map(String::valueOf).collect(Collectors.joining(",")) +"}')";
            var statement = PostgreSQL.getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

}
