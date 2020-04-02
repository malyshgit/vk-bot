package com.mvv.bots.vk.database;

import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class HibernateSessionFactoryUtil {
    private static final Logger LOG = LogManager.getLogger(HibernateSessionFactoryUtil.class);
    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                var ps = Config.JDBC_DATABASE_URL;
                var db = ps.split(":")[0].equals("postgres") ? "postgresql" : ps.split(":")[0];
                var user = ps.split(":")[1].substring(2);
                var pass = ps.split(":")[2].split("@")[0];
                var host = ps.split(":")[2].split("@")[1];
                var port = ps.split(":")[3].split("/")[0];
                var name = ps.split(":")[3].split("/")[1];

                Properties prop = new Properties();
                prop.setProperty("hibernate.connection.url",
                        String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                                db,
                                host,
                                port,
                                name
                        )
                );
                prop.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
                prop.setProperty("hibernate.connection.username", user);
                prop.setProperty("hibernate.connection.password", pass);
                prop.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
                prop.setProperty("hibernate.hbm2ddl.auto", "update");
                //prop.setProperty("hibernate.show_sql", "true");
                Configuration configuration = new Configuration();
                configuration.setProperties(prop);
                configuration.addAnnotatedClass(User.class);
                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return sessionFactory;
    }
}