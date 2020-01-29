package com.mvv.bots.vk.database;

import com.mvv.bots.vk.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQL {
    private static final Logger LOG = LogManager.getLogger(PostgreSQL.class);

    private static Connection connection = null;

    public PostgreSQL(){}

    public static Connection getConnection(){
        if(connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(Config.JDBC_DATABASE_URL);
                //connection.setAutoCommit(false);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return connection;
    }

    public static void commit(){
        try {
            connection.commit();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public static void close(){
        try {
            connection.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

}
