package com.mvv.bots.vk.database.tables.users;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Parameters {
    private JsonElement parameters;

    public Parameters(){
        this.parameters = new JsonParser().parse("{}");
    }

    public Parameters(String parameters){
        if(parameters == null) parameters = "{}";
        this.parameters = new JsonParser().parse(parameters);
    }

    public void put(String key, Object value){
        String stringValue = String.valueOf(value);
        parameters.getAsJsonObject().addProperty(key, stringValue);
    }

    public boolean has(String key){
        return parameters.getAsJsonObject().has(key);
    }

    public String get(String key){
        return parameters.getAsJsonObject().get(key).getAsString();
    }

    public void set(JsonElement parameters){
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return parameters.toString();
    }
}
