package com.vk.api.sdk.objects.messages.keyboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

public class Payload {
    private JsonObject parameters;

    public Payload(){
        parameters = new JsonObject();
    }

    public Payload(JsonObject parameters){
        this.parameters = parameters;
    }

    public Payload put(String key, Object value){
        parameters.addProperty(key, value.toString());
        return this;
    }

    @Override
    public String toString() {
        return parameters.toString();
    }
}
