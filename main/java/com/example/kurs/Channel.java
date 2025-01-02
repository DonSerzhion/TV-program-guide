package com.example.kurs;

import java.io.Serializable;

public class Channel implements Serializable { //Класс для передачи данных между активностями в виде последовательности байт
    private String name;
    private int imageResource;
    private boolean isFavorite;

    public Channel(String name, int imageResource) {
        this.name = name;
        this.imageResource = imageResource;
        this.isFavorite = false; // По умолчанию не избранный
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

