package com.example.intershiptest;

import io.realm.RealmObject;

public class ReposModel extends RealmObject {
    String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
