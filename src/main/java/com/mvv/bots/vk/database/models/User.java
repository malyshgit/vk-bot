package com.mvv.bots.vk.database.models;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    private int id;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int job;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int use;

    public User(int id) {
        this.id = id;
    }

    public User(int id, int job) {
        this.id = id;
        this.job = job;
    }

    public int getId() {
        return id;
    }

    public void setUse(int use) {
        this.use = use;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public int getUse() {
        return use;
    }

    public int getJob() {
        return job;
    }

    @Override
    public String toString() {
        return "User{id:"+id+", job:"+job+", use:"+use+"}";
    }
}
