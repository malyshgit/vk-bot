package com.mvv.bots.vk.database.tables;

import com.mvv.bots.vk.database.PostgreSQL;

import java.sql.ResultSet;
import java.sql.Statement;


public class Users {

    public final static String tableString = "CREATE TABLE USERS " +
            "(ID INT PRIMARY KEY     NOT NULL," +
            " JOB            INT     NOT NULL, " +
            " USE            INT     NOT NULL)";

    public static void create(){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            statement.executeUpdate(tableString);
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void update(User user){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "UPDATE USERS SET JOB=%d, USE=%d "
                            +"WHERE ID=%d;",
                    user.getJob(), user.getUse(), user.getId());
            statement.executeUpdate(sql);
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void add(User user){
        try {
            Statement statement = PostgreSQL.getConnection().createStatement();
            String sql = String.format(
                    "INSERT INTO USERS (ID,JOB,USE) "
                   +"VALUES (%d, %d, %d);",
                    user.getId(), user.getJob(), user.getUse());
            statement.executeUpdate(sql);
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            e.printStackTrace();
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
                user = new User(id);
                user.setJob(job);
                user.setUse(use);
                return user;
            }
            resultSet.close();
            statement.close();
            PostgreSQL.commit();
        } catch ( Exception e ) {
            e.printStackTrace();
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

        public User(int id){
            this.id = id;
            this.job = 0;
            this.use = 0;
        }

        public void setJob(int job) {
            this.job = job;
        }

        public void setUse(int use) {
            this.use = use;
        }

        public int getJob() {
            return job;
        }

        public int getUse() {
            return use;
        }

        public int getId() {
            return id;
        }
    }

}
