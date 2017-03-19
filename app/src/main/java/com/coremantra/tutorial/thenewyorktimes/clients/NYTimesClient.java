package com.coremantra.tutorial.thenewyorktimes.clients;

import com.coremantra.tutorial.thenewyorktimes.api.NYTimesService;
import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by radhikak on 3/17/17.
 */

public class NYTimesClient {

    public static final String BASE_URL = "https://api.nytimes.com";

    public static final String API_KEY = "2a7c18f40f4a4877961fac1db5144a20";

    private static NYTimesClient instance;
    private NYTimesService nyTimesService;

    private NYTimesClient() {

        final Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        nyTimesService = retrofit.create(NYTimesService.class);
    }

    public static NYTimesClient getInstance() {
        if (instance == null) {
            instance = new NYTimesClient();
        }
        return instance;
    }

    public Observable<ResponseWrapper> getRxArticles(Integer page, String order, String query, String beginDate, String newsDesk) {
        return nyTimesService.getRxArticles(API_KEY, page, order, query, beginDate, newsDesk);
    }

    public Call<ResponseWrapper> getArticles(Integer page, String order, String query, String beginDate, String newsDesk) {
        return nyTimesService.getArticles(API_KEY, page, order, query, beginDate, newsDesk);
    }

}









