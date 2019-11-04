package com.mvv.bots.vk.database.tables;

import com.mvv.bots.vk.database.PostgreSQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;


public class Users {
    private static final Logger LOG = LogManager.getLogger(Users.class);

    public final static String tableString = "CREATE TABLE USERS " +
            "(ID INT PRIMARY KEY     NOT NULL," +
            " JOB            INT     DEFAULT 0," +
            " USE            INT     DEFAULT 0," +
            " PARAMETERS     TEXT    DEFAULT '')";

    public static void create(){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS USERS;");
            statement.close();
            PostgreSQL.commit();
            statement = PostgreSQL.getConnection().createStatement();
            statement.executeUpdate(tableString);
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static void update(User user){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "UPDATE USERS SET JOB=%d, USE=%d, PARAMETERS=%s "
                            +"WHERE ID=%d;",
                    user.getJob(), user.getUse(), user.getParameters(), user.getId());
            statement.executeUpdate(sql);
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static void add(User user){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "INSERT INTO USERS (ID,JOB,USE,PARAMETERS) "
                   +"VALUES (%d, %d, %d, %s);",
                    user.getId(), user.getJob(), user.getUse(), user.getParameters());
            statement.executeUpdate(sql);
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            LOG.error(e);
        }
    }

    public static User find(int id){
        try {
            User user;
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format("SELECT * FROM USERS WHERE ID=%d;", id);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                int job = resultSet.getInt("job");
                int use = resultSet.getInt("use");
                String parameters = resultSet.getString("parameters");
                user = new User(id);
                user.setJob(job);
                user.setUse(use);
                user.setParameters(new User.Parameters(parameters));
                return user;
            }
            resultSet.close();
            statement.close();
            PostgreSQL.commit();
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
        private Parameters parameters;

        public User(int id){
            this.id = id;
            this.job = 0;
            this.use = 0;
            this.parameters = new Parameters();
        }

        public void setJob(int job) {
            this.job = job;
        }

        public void setUse(int use) {
            this.use = use;
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

        public Parameters getParameters() {
            return parameters;
        }

        public int getId() {
            return id;
        }

        public static class Parameters{
            private String parameters;
            private HashMap<String, Object> map;

            public Parameters(){
                this.parameters = "";
                map = new HashMap<>();
            }

            public Parameters(String parameters){
                if(parameters == null) parameters = "";
                this.parameters = parameters;
                if(parameters.matches("(\\w+=\\w+\n?)+")){
                    map = (HashMap<String, Object>) Arrays
                            .stream(parameters.split("\n"))
                            .map(s -> s.split("="))
                            .collect(Collectors.toMap(e -> e[0], e -> (Object)e[1]));
                }else{
                    map = new HashMap<>();
                }
            }

            public void put(String key, Object value){
                map.put(key, value);
            }

            public boolean has(String key){
                return map.containsKey(key);
            }

            public Object get(String key){
                return map.get(key);
            }

            public void set(HashMap<String, Object> map){
                this.map = map;
            }

            @Override
            public String toString() {
                return map.entrySet().stream().map(Object::toString).collect(Collectors.joining("\n"));
            }
        }
    }

}
