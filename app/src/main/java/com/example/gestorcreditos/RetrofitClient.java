package com.example.gestorcreditos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // URL centralizada en Config.java
    private static final String BASE_URL = Config.URL_BASE;
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        // Gson lenient: tolera JSON con BOM, espacios iniciales, comas finales, etc.
        Gson lenientGson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(lenientGson))
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }
}