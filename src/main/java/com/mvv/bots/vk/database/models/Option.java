package com.mvv.bots.vk.database.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @PrimaryKeyJoinColumn
    private int id;

    private String key;
    private String value;

    public Option(){}

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
