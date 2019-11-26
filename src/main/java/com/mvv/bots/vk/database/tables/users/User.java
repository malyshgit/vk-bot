package com.mvv.bots.vk.database.tables.users;

public class User {

    private int id;
    private int job;
    private int use;
    private String token;
    private Parameters parameters;

    public User(int id){
        this.id = id;
        this.job = 0;
        this.use = 0;
        this.token = null;
        this.parameters = new Parameters();
    }

    public void setJob(int job) {
        this.job = job;
    }

    public void setUse(int use) {
        this.use = use;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public int getJob() {
        return job;
    }

    public int getUse() {
        return use;
    }

    public String getToken() {
        return token;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{id="+id+", job="+job+", use="+use+", token="+token+", parameters="+parameters+"}";
    }

}
