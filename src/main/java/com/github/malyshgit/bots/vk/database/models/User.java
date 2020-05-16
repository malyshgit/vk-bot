package com.github.malyshgit.bots.vk.database.models;

import com.google.gson.JsonElement;
import com.github.malyshgit.bots.vk.database.dao.UsersTable;

import java.util.*;
import java.util.stream.Collectors;

public class User {

    private Integer id;

    private String token;

    private Map<String, JsonElement> fields = new HashMap<>();

    public User(int id){
        this.id = id;
        this.token = null;
    }


    public void setToken(String token) {
        this.token = token;
    }

    public void setFields(Map<String, JsonElement> fields) {
        this.fields = fields;
    }

    public String getToken() {
        return token;
    }

    public Map<String, JsonElement> getFields() {
        return fields;
    }

    public int getId() {
        return id;
    }

    public void update(){
        UsersTable.update(this);
    }

    @Override
    public String toString() {
        return "User{id="+id+", token="+token+", fields="+fields.entrySet().stream().map(e-> "{\""+e.getKey()+"\"="+e.getValue()+"}").collect(Collectors.toList())+"}";
    }

}
