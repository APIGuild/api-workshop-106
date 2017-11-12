package com.guild.api.demo.model;

public class Logistics {
    private final String id;
    private final String logistics;

    public Logistics(String id, String logistics) {
        this.id = id;
        this.logistics = logistics;
    }

    public String getId() {
        return id;
    }

    public String getLogistics() {
        return logistics;
    }
}
