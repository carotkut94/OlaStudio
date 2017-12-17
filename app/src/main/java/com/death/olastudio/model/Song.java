package com.death.olastudio.model;

/**
 * Author: deathcode
 * Created by: ModelGenerator on 15/12/17
 */
public class Song {

    private String song;
    private String url;
    private String artists;
    private String cover_image;

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getCoverImage() {
        return cover_image;
    }

    public void setCoverImage(String cover_image) {
        this.cover_image = cover_image;
    }
}