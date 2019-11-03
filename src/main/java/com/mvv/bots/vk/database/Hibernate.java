package com.mvv.bots.vk.database;

import com.mvv.bots.vk.Config;
import com.mvv.bots.vk.database.models.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class Hibernate {
    private static SessionFactory sessionFactory;

    private Hibernate() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                Properties properties = new Properties();
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
                properties.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
                String url = "jdbc:"
                        +Config.DB_PROPS.get(Config.DB.NAME)
                        +"://"
                        +Config.DB_PROPS.get(Config.DB.HOST)
                        +":"
                        +Config.DB_PROPS.get(Config.DB.PORT)
                        +"/"
                        +Config.DB_PROPS.get(Config.DB.DATABASE);
                properties.setProperty("hibernate.connection.url", url);
                properties.setProperty("hibernate.connection.username", Config.DB_PROPS.get(Config.DB.USER));
                properties.setProperty("hibernate.connection.password", Config.DB_PROPS.get(Config.DB.PASS));
                properties.setProperty("hibernate.show_sql", "true");
                properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
                configuration.setProperties(properties);
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Settings.class);
                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());

            } catch (Exception e) {
                System.err.println(ExceptionUtils.getStackTrace(e));
            }
        }
        return sessionFactory;
    }
}
