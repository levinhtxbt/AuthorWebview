package com.example.virus.demoauthorusingwebview.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by virus on 12/13/2016.
 */

public class TokenResponse {

    @SerializedName("access_token")
    private String access_token;
    @SerializedName("expires_in")
    private int expires_in;
    @SerializedName("refresh_token")
    private String refresh_token;
    @SerializedName("token_type")
    private String token_type;



    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
}
