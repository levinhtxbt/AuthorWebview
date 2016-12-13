package com.example.virus.demoauthorusingwebview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.virus.demoauthorusingwebview.retrofit.RetrofitInterface;
import com.example.virus.demoauthorusingwebview.retrofit.TokenResponse;
import com.example.virus.demoauthorusingwebview.utils.Constant;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
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
import static com.example.virus.demoauthorusingwebview.utils.Constant.CLIENT_ID;
import static com.example.virus.demoauthorusingwebview.utils.Constant.CLIENT_SECRET;
import static com.example.virus.demoauthorusingwebview.utils.Constant.GRANT_TYPE;
import static com.example.virus.demoauthorusingwebview.utils.Constant.OAUTH_SCOPE;
import static com.example.virus.demoauthorusingwebview.utils.Constant.OAUTH_URL;
import static com.example.virus.demoauthorusingwebview.utils.Constant.REDIRECT_URI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    WebView webView;
    Button buttonSignIn;
    SharedPreferences pref;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);
        pref = getSharedPreferences(Constant.PREF_NAME, MODE_PRIVATE);
        textView = (TextView) findViewById(R.id.textView);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(this);
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
                readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);
        httpClientBuilder.addInterceptor(logging)
                .addNetworkInterceptor(new StethoInterceptor());
        httpClientBuilder.cache(cache);
        //retrofit
        Retrofit service = new Retrofit.Builder()
                .baseUrl(Constant.HOST)
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
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("access_token", tokenResponse.getAccess_token());
                        edit.commit();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        final Dialog auth_dialog;
        auth_dialog = new Dialog(MainActivity.this);
        auth_dialog.setContentView(R.layout.auth_dialog);

        webView = (WebView) auth_dialog.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID + "&scope=" + OAUTH_SCOPE);
        webView.setWebViewClient(new WebViewClient() {
            boolean authComplete = false;
            Intent resultIntent = new Intent();
            String authCode;

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter(Constant.ServiceParameter.CODE);
                    authComplete = true;
                    resultIntent.putExtra(Constant.ServiceParameter.CODE, authCode);
                    MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);

                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(Constant.ServiceParameter.CODE, authCode);
                    edit.commit();

                    auth_dialog.dismiss();

                    getAccessToken(authCode);

                    Toast.makeText(getApplicationContext(), "Authorization Code is: " + authCode, Toast.LENGTH_SHORT).show();

                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra(Constant.ServiceParameter.CODE, authCode);
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
}
