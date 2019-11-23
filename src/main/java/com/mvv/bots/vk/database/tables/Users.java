package com.mvv.bots.vk.database.tables;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mvv.bots.vk.database.PostgreSQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Users {
    private static final Logger LOG = LogManager.getLogger(Users.class);
    private static final String name = "USERS";

    public final static String tableString = "CREATE TABLE "+name+" " +
            "(ID INT PRIMARY KEY     NOT NULL," +
            " JOB            INT     DEFAULT 0," +
            " USE            INT     DEFAULT 0," +
            " TOKEN          TEXT," +
            " PARAMETERS     TEXT);";

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

    public static void update(Integer id, String key, Object value){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String valueString = value instanceof String ? "'"+value+"'" : String.valueOf(value);
            String sql = String.format(
                    "UPDATE "+name+" SET %s=%s WHERE ID=%d;",
                    key, valueString, id);
            LOG.debug(sql);
            statement.executeUpdate(sql);
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    /*public static void update(User user){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "UPDATE "+name+" SET JOB=%d, USE=%d, PARAMETERS='%s' WHERE ID=%d;",
                    user.getJob(), user.getUse(), user.getParameters(), user.getId());
            LOG.debug(sql);
            statement.executeUpdate(sql);
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }*/

    public static void add(User user){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "INSERT INTO "+name+" (ID,JOB,USE,PARAMETERS) "
                                +"VALUES (%d, %d, %d, '%s');",
                    user.getId(), user.getJob(), user.getUse(), user.getParameters());
            LOG.debug(sql);
            statement.executeUpdate(sql);
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static List<User> findAll(){
        try {
            List<User> users = new ArrayList<>();
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = "SELECT * FROM "+name+";";
            LOG.debug(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                User user;
                int id = resultSet.getInt("id");
                int job = resultSet.getInt("job");
                int use = resultSet.getInt("use");
                String token = resultSet.getString("token");
                String parameters = resultSet.getString("parameters");
                user = new User(id);
                user.setJob(job);
                user.setUse(use);
                user.setToken(token);
                user.setParameters(new User.Parameters(parameters));
                users.add(user);
            }
            resultSet.close();
            statement.close();
            LOG.debug(users);
            return users;
        } catch ( Exception e ) {
            LOG.error(e);
        }
        return null;
    }

    public static User find(Integer id){
        try {
            User user;
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format("SELECT * FROM "+name+" WHERE ID=%d;", id);
            LOG.debug(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                int job = resultSet.getInt("job");
                int use = resultSet.getInt("use");
                String token = resultSet.getString("token");
                String parameters = resultSet.getString("parameters");
                user = new User(id);
                user.setJob(job);
                user.setUse(use);
                user.setToken(token);
                user.setParameters(new User.Parameters(parameters));
                LOG.debug(user);
                return user;
            }
            resultSet.close();
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
        return null;
    }

    public static boolean contains(int id){
        return find(id) != null;
    }

    public static class User{

        private int id;
        private int job;
        private int use;
        private String token;
        private Parameters parameters;

        public User(int id){
            this.id = id;
            this.job = 0;
            this.use = 0;
            this.token = "";
            this.parameters = new Parameters();
        }

        public void setJob(int job) {
            this.job = job;
        }

        public void setUse(int use) {
            this.use = use;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setParameters(Parameters parameters) {
            this.parameters = parameters;
        }

        public int getJob() {
            return job;
        }

        public int getUse() {
            return use;
        }

        public String getToken() {
            return token;
        }

        public Parameters getParameters() {
            return parameters;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "User{id="+id+", job="+job+", use="+use+", token="+token+", parameters="+parameters+"}";
        }

        public static class Parameters{
            private JsonElement parameters;

            public Parameters(){
                this.parameters = new JsonParser().parse("{}");
            }

            public Parameters(String parameters){
                if(parameters == null) parameters = "{}";
                this.parameters = new JsonParser().parse(parameters);
            }

            public void put(String key, Object value){
                String stringValue = String.valueOf(value);
                parameters.getAsJsonObject().addProperty(key, stringValue);
            }

            public boolean has(String key){
                return parameters.getAsJsonObject().has(key);
            }

            public String get(String key){
                return parameters.getAsJsonObject().get(key).getAsString();
            }

            public void set(JsonElement parameters){
                this.parameters = parameters;
            }

            @Override
            public String toString() {
                return parameters.toString();
            }
        }
    }

}
