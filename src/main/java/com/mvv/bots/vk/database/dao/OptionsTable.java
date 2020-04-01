package com.mvv.bots.vk.database.dao;

import com.mvv.bots.vk.database.HibernateSessionFactoryUtil;
import com.mvv.bots.vk.database.models.Option;
import com.mvv.bots.vk.database.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;


public class OptionsTable {
    private static final Logger LOG = LogManager.getLogger(OptionsTable.class);
    private static final String name = "OPTIONS";

    public final static String tableString = "CREATE TABLE "+name+" " +
            "(ID               SERIAL  PRIMARY KEY," +
            " KEY              TEXT    NOT NULL UNIQUE," +
            " VALUE            TEXT    NOT NULL);";

    public static void create(){
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        String sql = "DROP TABLE IF EXISTS "+name+";";
        session.createSQLQuery(sql).executeUpdate();
        session.getTransaction().commit();
        session.close();

        session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        sql = tableString;
        session.createSQLQuery(sql).executeUpdate();
        session.getTransaction().commit();
        session.close();
    }

    public static Option findByKey(String key) {
        return findAll().stream().filter(o->o.getKey().equals(key)).findFirst().orElse(null);
    }

    public static void save(Option option) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.save(option);
        tx1.commit();
        session.close();
    }

    public static void update(Option option) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.update(option);
        tx1.commit();
        session.close();
    }

    public static void delete(Option option) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.delete(option);
        tx1.commit();
        session.close();
    }

    public static List<Option> findAll() {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("From Option", Option.class).list();
    }

}
