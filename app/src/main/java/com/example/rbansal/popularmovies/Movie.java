package com.example.rbansal.popularmovies;

/**
 * Created by rbansal on 28/5/16.
 */
//movie class
public class Movie {
    public final String imgUrl;
    public final String name;

    Movie(String imgUrl, String name) {
        this.imgUrl = imgUrl;
        this.name = name;
    }
}
