package com.mvv.bots.vk.database.tables.users;

import com.mvv.bots.vk.database.PostgreSQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


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
            String valueString;
            if(value == null){
                valueString = "NULL";
            }else{
                valueString = value instanceof String ? "'"+value+"'" : String.valueOf(value);
            }
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
        List<User> users = new ArrayList<>();
        try {
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
                user.setParameters(new Parameters(parameters));
                users.add(user);
            }
            resultSet.close();
            statement.close();
            LOG.debug(users);
            return users;
        } catch ( Exception e ) {
            LOG.error(e);
        }
        return users;
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
                user.setParameters(new Parameters(parameters));
                LOG.debug(user);
                return user;
            }
            resultSet.close();
            statement.close();
        } catch ( Exception e ) {
            LOG.error(e);
        }
        return new User(id);
    }

}
