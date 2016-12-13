package com.example.virus.demoauthorusingwebview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.virus.demoauthorusingwebview.retrofit.RetrofitInterface;
import com.example.virus.demoauthorusingwebview.retrofit.TokenResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String BASE_URL = "https://accounts.google.com";
    private static String CLIENT_ID = "345372135922-5fkbks2bmkup55f5dl60g4752uami9gq.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "k783JFFc9yK7NMHO7cJeRo0q";
    private static String REDIRECT_URI = "http://localhost";
    private static String GRANT_TYPE = "authorization_code";
    private static String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static String OAUTH_SCOPE = "https://www.googleapis.com/auth/urlshortener";


    WebView web;
    Button auth;
    SharedPreferences pref;
    TextView Access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        Access = (TextView) findViewById(R.id.Access);
        auth = (Button) findViewById(R.id.auth);
        auth.setOnClickListener(new View.OnClickListener() {
            Dialog auth_dialog;
            @Override
            public void onClick(View v) {
                auth_dialog = new Dialog(MainActivity.this);

                auth_dialog.setContentView(R.layout.auth_dialog);
                web = (WebView) auth_dialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);
                web.loadUrl(OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID + "&scope=" + OAUTH_SCOPE);
                web.setWebChromeClient(new WebChromeClient(){

                });
                web.setWebViewClient(new WebViewClient() {
                    boolean authComplete = false;
                    Intent resultIntent = new Intent();

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    String authCode;

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (url.contains("?code=") && authComplete != true) {
                            Uri uri = Uri.parse(url);
                            authCode = uri.getQueryParameter("code");
                            Log.i("", "CODE : " + authCode);
                            authComplete = true;
                            resultIntent.putExtra("code", authCode);
                            MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);

                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("Code", authCode);
                            edit.commit();
                            auth_dialog.dismiss();
                            getAccessToken(authCode);
                            Toast.makeText(getApplicationContext(), "Authorization Code is: " + authCode, Toast.LENGTH_SHORT).show();
                        } else if (url.contains("error=access_denied")) {
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                            auth_dialog.dismiss();
                        }
                    }
                });
                auth_dialog.show();
                auth_dialog.setTitle("Authorize");
                auth_dialog.setCancelable(true);
            }
        });

    }

    public void getAccessToken(String code) {
        //gson
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        //cache
        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(this.getCacheDir(), cacheSize);
        //httpclient --add log
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().
                readTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS);
        httpClientBuilder.addInterceptor(logging);
        httpClientBuilder.cache(cache);
        //retrofit
        Retrofit service = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        service.create(RetrofitInterface.class)
                .getToken(code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, GRANT_TYPE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<TokenResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(TokenResponse tokenResponse) {
                        Toast.makeText(MainActivity.this, tokenResponse.getAccess_token(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "access_token: " + tokenResponse.getAccess_token());
                        Log.d(TAG, "expires_in: " + tokenResponse.getExpires_in());
                        Log.d(TAG, "refresh_token: " + tokenResponse.getRefresh_token());
                        Log.d(TAG, "token_type: " + tokenResponse.getToken_type());
                    }
                });


    }

}
