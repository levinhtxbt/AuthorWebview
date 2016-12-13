package com.example.virus.demoauthorusingwebview.retrofit;

import com.example.virus.demoauthorusingwebview.utils.Constant;

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
    Observable<TokenResponse> getToken(@Field(Constant.ServiceParameter.CODE) String code,
                                       @Field(Constant.ServiceParameter.CLIENT_ID) String client_id,
                                       @Field(Constant.ServiceParameter.CLIENT_SECRET) String client_secret,
                                       @Field(Constant.ServiceParameter.REDIRECT_URI) String redirect_uri,
                                       @Field(Constant.ServiceParameter.GRANT_TYPE) String grant_type);
}
