package com.coremantra.tutorial.thenewyorktimes.api;

import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by radhikak on 3/12/17.
 */

public interface NYTimesService {

    @GET("/svc/search/v2/articlesearch.json")
    Call<ResponseWrapper> getArticles(@Query("api_key") String key,
                                      @Query("page") Integer page,
                                      @Query("sort") String order,
                                      @Query("q") String search,
                                      @Query("begin_date") String beginDate,
                                      @Query("fq") String newsDesk
                                      );


    @GET("/svc/search/v2/articlesearch.json")
    Observable<ResponseWrapper> getRxArticles(@Query("api_key") String key,
                                                   @Query("page") Integer page,
                                                   @Query("sort") String order,
                                                   @Query("q") String search,
                                                   @Query("begin_date") String beginDate,
                                                   @Query("fq") String newsDesk
    );
}
