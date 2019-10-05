package com.mvv.bots.vk.database;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;

public class DataBase {

    private static Connection connection = connect();
    private static Statement statement = createStatement();

    public static Connection getConnection(){
        return connection;
    }

    public static Statement getStatement() {
        return statement;
    }

    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            String dbUrl = System.getenv("JDBC_DATABASE_URL");

            return DriverManager.getConnection(dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static Statement createStatement(){
        try {
            return getConnection().createStatement();
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static void createDataBase(){
        try {
            String sql = "DROP TABLE IF EXISTS users";
            getStatement().executeUpdate(sql);
            sql = "CREATE TABLE users " +
                    "(id INT PRIMARY KEY     NOT NULL," +
                    " parameters     TEXT    DEFAULT '{}'," +
                    " subdate        INT     DEFAULT 0," +
                    " use            INT     DEFAULT 0," +
                    " job            INT     DEFAULT 0)";
            getStatement().executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void insert(String db, Object key, Object value, Object whereKey, Object whereValue){
        try {
            if(!contains(db, key, whereKey, whereValue, false)){
                String ins;
                if(whereKey != null && whereValue != null){
                    ins = "INSERT INTO "+db+" ("+whereKey+", "+key+") VALUES ("+whereValue+", "+value+");";
                }else{
                    ins = "INSERT INTO "+db+" ("+key+") VALUES ("+value+");";
                }
                DataBase.getStatement().executeUpdate(ins);
                System.out.println("#db:"+ins);
            }else{
                update(db, key, value, whereKey, whereValue, false);
            }
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void update(String db, Object key, Object value, Object whereKey, Object whereValue, boolean useRegex){
        try {
            if(contains(db, key, whereKey, whereValue, useRegex)){
                String ins;
                if(whereKey != null && whereValue != null){
                    if(useRegex) {
                        ins = "UPDATE " + db + " SET " + key + " = " + value + " WHERE " + whereKey + " ~* " + whereValue + ";";
                    }else{
                        ins = "UPDATE " + db + " SET " + key + " = " + value + " WHERE " + whereKey + " = " + whereValue + ";";
                    }
                }else{
                    ins = "UPDATE "+db+" SET "+key+" = "+value+";";
                }
                DataBase.getStatement().executeUpdate(ins);
                System.out.println("#db:"+ins);
            }else{
                insert(db, key, value, whereKey, whereValue);
            }
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    public static boolean contains(String db, Object key, Object whereKey, Object whereValue, boolean useRegex){
        try {
            String sel;
            if(whereKey != null && whereValue != null){
                if(useRegex){
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" ~* "+whereValue+";";
                }else{
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" = "+whereValue+";";
                }
            }else{
                sel = "SELECT ("+key+") FROM "+db+";";
            }
            if(DataBase.getStatement().executeQuery(sel).next()){
                return true;
            }else{
               return false;
            }
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    public static int selectInteger(String db, Object key, Object whereKey, Object whereValue, boolean useRegex){
        try {
            String sel;
            if(whereKey != null && whereValue != null){
                if(useRegex){
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" ~* "+whereValue+";";
                }else{
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" = "+whereValue+";";
                }
            }else{
                sel = "SELECT "+key+" FROM "+ db+";";
            }
            ResultSet res = DataBase.getStatement().executeQuery(sel);
            System.out.println("#db:"+sel);
            if(res.next()){
                return res.getInt(key.toString());
            }else{
                return -1;
            }
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            return -1;
        }
    }

    public static String selectString(String db, Object key, Object whereKey, Object whereValue, boolean useRegex){
        try {
            String sel;
            if(whereKey != null && whereValue != null){
                if(useRegex){
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" ~* "+whereValue+";";
                }else{
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" = "+whereValue+";";
                }
            }else{
                sel = "SELECT "+key+" FROM "+ db+";";
            }
            ResultSet res = DataBase.getStatement().executeQuery(sel);
            System.out.println("#db:"+sel);
            if(res.next()){
                return res.getString(key.toString());
            }else{
                return null;
            }
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static boolean selectBoolean(String db, Object key, Object whereKey, Object whereValue, boolean useRegex){
        try {
            String sel;
            if(whereKey != null && whereValue != null){
                if(useRegex){
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" ~* "+whereValue+";";
                }else{
                    sel = "SELECT ("+key+") FROM "+db+" WHERE "+whereKey+" = "+whereValue+";";
                }
            }else{
                sel = "SELECT "+key+" FROM "+ db+";";
            }
            ResultSet res = DataBase.getStatement().executeQuery(sel);
            System.out.println("#db:"+sel);
            if(res.next()){
                return res.getBoolean(key.toString());
            }else{
                return false;
            }
        } catch (SQLException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
}
