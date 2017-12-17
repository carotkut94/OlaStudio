package com.death.olastudio.network;

import com.death.olastudio.model.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by deathcode on 15/12/17.
 */

public interface StarlordApi {

    @GET("studio")
    Call<List<Song>> getSongs();

}
