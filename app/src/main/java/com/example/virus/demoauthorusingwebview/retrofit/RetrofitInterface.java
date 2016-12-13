package com.example.virus.demoauthorusingwebview.retrofit;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by virus on 12/13/2016.
 */

public interface RetrofitInterface {

    @FormUrlEncoded
    @POST("o/oauth2/token")
    Observable<TokenResponse> getToken(@Field("code") String code,
                                       @Field("client_id") String client_id,
                                       @Field("client_secret") String client_secret,
                                       @Field("redirect_uri") String redirect_uri,
                                       @Field("grant_type") String grant_type);
}
