package com.SEG.rentify;

public class Category {

    private String name;
    private String description;
    private String id;

    public Category(String name, String description, String id){
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
