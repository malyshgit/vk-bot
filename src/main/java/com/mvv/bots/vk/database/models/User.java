package com.mvv.bots.vk.database.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
public class User {

    @Id
    private int id;

    private int job;
    private int use;

    @Column(name = "token", nullable = true)
    private String token;
    @Column(name = "parameters", nullable = true)
    private String[] parameters;

    private Parameters parametersMap;

    public User(){

    }

    public User(int id){
        this.id = id;
        this.job = 0;
        this.use = 0;
        this.token = null;
        this.parameters = null;
        this.parametersMap = null;
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
        var map = List.of(parameters).stream().map(e->{
            var indexOfEq = e.indexOf("=");
            var key = e.substring(0, indexOfEq);
            var value = e.substring(indexOfEq+1);
            return Map.entry(key, new JsonParser().parse(value).getAsJsonObject());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return parametersMap = new Parameters(this, map);
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{id="+id+", job="+job+", use="+use+", token="+token+", parameters="+Arrays.toString(parameters)+"}";
    }

    public static class Parameters{
        private Map<String, JsonObject> parameters;
        private final User user;
        public Parameters(User user){
            this.user = user;
            parameters = new HashMap<>();
            set(parameters);
        }

        public Parameters(User user, Map<String, JsonObject> parameters){
            this.user = user;
            this.parameters = parameters;
            set(parameters);
        }

        public void set(Map<String, JsonObject> parameters) {
            this.parameters = parameters;
            user.parameters = parameters.entrySet().stream().map(e->e.getKey()+"="+e.getValue()).toArray(String[]::new);
        }

        public boolean containsKey(String key){
            return parameters.containsKey(key);
        }

        public JsonObject get(String key) {
            return parameters.get(key);
        }

        public Parameters put(String key, JsonObject object){
            parameters.put(key, object);
            set(parameters);
            return this;
        }
    }
}
