package com.mvv.bots.vk.database.models;


public class Option {

    private int id;

    private String key;
    private String value;

    public Option(String key, String value){
        this.key = key;
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Option{key="+key+", value="+value+"}";
    }
}
