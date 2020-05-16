package com.github.malyshgit.bots.vk.database;

import com.github.malyshgit.bots.vk.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class PostgreSQL {
    private static final Logger LOG = LogManager.getLogger(PostgreSQL.class);

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if(connection != null) return connection;
            Class.forName("org.postgresql.Driver");
            return connection = DriverManager.getConnection(Config.JDBC_DATABASE_URL);
        } catch (SQLException | ClassNotFoundException e) {
            LOG.error(e);
        }
        return connection;
    }

    private static void createTable() {
        try {
            String sql =
                    "CREATE TABLE DBUSER("
                + "USER_ID NUMBER(5) NOT NULL, "
                + "USERNAME VARCHAR(20) NOT NULL, "
                + "CREATED_BY VARCHAR(20) NOT NULL, "
                + "CREATED_DATE DATE NOT NULL, " + "PRIMARY KEY (USER_ID) "
                + ")";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void addColumn(String table, String name, String type) {
        try {
            String sql =
                    "ALTER TABLE "+table+" ADD COLUMN "+name+" "+type+";";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void jsonbSet(String table, String column, String whereKey, String whereValue, String query, String value){
        try {
            var tmpQ = query.split(",");
            for(var i = 0; i < tmpQ.length; i++){
                tmpQ[i] = tmpQ[i].trim();
            }
            StringBuilder sb = new StringBuilder();
            if(tmpQ.length > 1){
                sb.append(" CASE ");
                sb.append(" WHEN "+column+"->'"+tmpQ[0]+"' IS NULL THEN '");
                for(var i = 1; i < tmpQ.length; i++){
                    sb.append("{\""+tmpQ[i]+"\":");
                }
                sb.append(value);
                for(var i = 1; i < tmpQ.length; i++){
                    sb.append("}");
                }
                sb.append("' ");
                for(var i = 1; i < tmpQ.length; i++) {
                    sb.append(" WHEN " + column);
                    for(var j = 0; j <= i; j++){
                        sb.append("->'" + tmpQ[j] + "'");
                    }
                    sb.append(" IS NULL THEN ");
                    sb.append(" jsonb_set("+column);
                    for(var j = 0; j <= i-1; j++){
                        sb.append("->'" + tmpQ[j] + "'");
                    }
                    sb.append(", '{"+tmpQ[i]+"}', '"+value+"') ");
                }
                sb.append(" ELSE jsonb_set("+column+"->'"+tmpQ[0]+"', '{"+tmpQ[1]+"}', "+column+"->'"+tmpQ[0]+"'->'"+tmpQ[1]+"' || '"+value+"')");
                sb.append(" END ");
            }else{
                sb.append("    '"+value+"'");
            }
            var sql = "UPDATE "+table+" SET "+column+" = JSONB_SET("+
                "    "+column+"," +
                "    '{"+tmpQ[0]+"}'," +
                sb.toString()+
                ") WHERE "+whereKey+" = "+whereValue+";";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void addIntoArray(String table, String column, String whereKey, String whereValue, String array) {
        try {
            String sql =
                    "UPDATE "+table+" SET "+column+" = "+column+" || '["+array+"]' WHERE "+whereKey+" = "+whereValue+";";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }


    /*"update users " +
            "set fields = " +
            "jsonb_set(fields, '{album2}', (select array_to_json(array(select distinct jsonb_array_elements(fields->'album2' || '[4,5,6,7,8,9]')))::jsonb " +
            "from users where id = 0)) " +
            "where id = 0";*/

    //UPDATE test
    //SET data = jsonb_set(data, '{a}', '5'::jsonb);

    //UPDATE testjsonb SET object = object - 'b' || '{"a":1,"d":4}';
    //"UPDATE USERS SET TESTARRAY = jsonb_insert(TESTARRAY, '{album4,0}', '4444') WHERE ID = 2;";
    public static void appendIntoJsonArray(String table, String column, String whereKey, String whereValue, String query, Object... values) {
        try {
            var arr = new String[values.length];
            for(var i = 0; i < arr.length; i++){
                if(values[i] instanceof Number){
                    arr[i] = String.valueOf(values[i]);
                }else{
                    arr[i] = "\""+values[i]+"\"";
                }
            }
            String sql =
                    "UPDATE "+table+" " +
                            "SET "+column+" = " +
                            "jsonb_set("+column+", '{"+query+"}', " +
                            //массив без повторов
                            "(SELECT array_to_json(ARRAY(SELECT DISTINCT jsonb_array_elements("+column+"#>'{"+query+"}' || '["+String.join(",", arr)+"]')))::jsonb " +
                            "FROM "+table+" WHERE "+whereKey+" = "+whereValue+")" +
                            //
                            ") " +
                            "WHERE "+whereKey+" = "+whereValue+";";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void setObjectToJsonb(String table, String column, String whereKey, String whereValue, String query, String value) {
        try {
            String sql = "UPDATE "+table+" SET "+column+" = jsonb_set("+column+", '{"+query+"}' ,'"+value+"') WHERE "+whereKey+" = "+whereValue+";";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private static void insert() {
        try {
            String sql =
                    "INSERT INTO DBUSER"
                    + "(USER_ID, USERNAME) " + "VALUES"
                    + "(1,'mkyong'))";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private static void get() {
        try {
            String sql = "SELECT USER_ID, USERNAME from DBUSER";
            var statement = getConnection().createStatement();

            // выбираем данные с БД
            ResultSet rs = statement.executeQuery(sql);

            // И если что то было получено то цикл while сработает
            while (rs.next()) {
                String userid = rs.getString("USER_ID");
                String username = rs.getString("USERNAME");
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private static void delete() {
        try {
            String sql =
                    "DELETE DBUSER WHERE USER_ID = 1";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private static void update() {
        try {
            String sql =
                    "UPDATE DBUSER SET USERNAME = 'mkyong_new' WHERE USER_ID = 1";
            var statement = getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

}
