package com.coremantra.tutorial.thenewyorktimes.api;

import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by radhikak on 3/12/17.
 */

public interface NYTimesAPI {

    public static final String BASE_URL = "https://api.nytimes.com";

    public static final String API_KEY = "2a7c18f40f4a4877961fac1db5144a20";

    @GET("/svc/search/v2/articlesearch.json")
    Call<ResponseWrapper> getArticles(@Query("api_key") String key,
                                      @Query("page") Integer page,
                                      @Query("sort") String order,
                                      @Query("q") String search,
                                      @Query("fq") String filters
                                      );
}
