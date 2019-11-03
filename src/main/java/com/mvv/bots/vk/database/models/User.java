package com.mvv.bots.vk.database.models;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    private Integer id;

    private Integer job;

    private Integer use;

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, Integer job) {
        this.id = id;
        this.job = job;
    }

    public Integer getId() {
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
