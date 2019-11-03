package com.mvv.bots.vk.database.dao;

import com.mvv.bots.vk.database.Hibernate;
import com.mvv.bots.vk.database.models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserDao {

    public User findById(Integer id) {
        return Hibernate.getSessionFactory().openSession().get(User.class, id);
    }

    public void save(User user) {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.save(user);
        tx1.commit();
        session.close();
    }

    public void update(User user) {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.update(user);
        tx1.commit();
        session.close();
    }

    public void delete(User user) {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.delete(user);
        tx1.commit();
        session.close();
    }

    public List<User> findAll() {
        List users = Hibernate.getSessionFactory().openSession().createQuery("From User").list();
        return users;
    }
}