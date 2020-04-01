package com.mvv.bots.vk.database.dao;

import com.mvv.bots.vk.database.HibernateSessionFactoryUtil;
import com.mvv.bots.vk.database.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Statement;
import java.util.List;


public class UsersTable {
    private static final Logger LOG = LogManager.getLogger(UsersTable.class);
    private static final String name = "USERS";

    public final static String tableString = "CREATE TABLE "+name+" " +
            "(ID INT PRIMARY KEY     NOT NULL," +
            " JOB            INT     DEFAULT 0," +
            " USE            INT     DEFAULT 0," +
            " TOKEN          TEXT," +
            " PARAMETERS     TEXT[]);";

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

    public static User findById(int id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(User.class, id);
    }

    public static void save(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.save(user);
        tx1.commit();
        session.close();
    }

    public static void update(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.update(user);
        tx1.commit();
        session.close();
    }

    public static void delete(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.delete(user);
        tx1.commit();
        session.close();
    }

    public static List<User> findAll() {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("From User", User.class).list();
    }

}
