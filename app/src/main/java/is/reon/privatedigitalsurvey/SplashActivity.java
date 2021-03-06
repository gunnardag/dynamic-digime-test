package is.reon.privatedigitalsurvey;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import is.reon.privatedigitalsurvey.data.RequestHandler;
import me.digi.sdk.core.DigiMeAuthorizationManager;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKResponse;
import me.digi.sdk.core.session.CASession;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = "SplashActivity";
    private RequestHandler queryService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        queryService = getQueryService();
    }

    @OnClick(R.id.get_data_button)
    public void getDigiMeData() {
        Log.d(TAG, "getDigiMeData: starting the process");
        DigiMeClient.init(SplashActivity.this);
        DigiMeClient.setApplicationName("Private Digital Survey");
        Call<ResponseBody> call = queryService.getP12file();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "onResponse: success");

                InputStream inputStream = null;

                long fileSize = response.body().contentLength();
                Log.d(TAG, "fileSize: "+fileSize);

                inputStream = response.body().byteStream();

                if(inputStream != null) {
                    DigiMeClient.getDefaultKeyLoader().addKeyFromPKCS12Stream(inputStream, "<< contract password >>");
                    DigiMeClient.getInstance().createSession("<< contract id >>", new SDKCallback<CASession>() {
                        @Override
                        public void succeeded(SDKResponse<CASession> sdkResponse) {
                            Log.d(TAG, "succeeded creating session");
                            final DigiMeAuthorizationManager manager = new DigiMeAuthorizationManager("", "", sdkResponse.body);
                            DigiMeClient.getInstance().authorizeInitializedSessionWithManager(manager, SplashActivity.this, new SDKCallback<CASession>() {
                                @Override
                                public void succeeded(SDKResponse<CASession> sdkResponse) {
                                    Log.d(TAG, "succeeded authorization");
                                    if(manager.isInProgress()) {
                                        manager.cancelOngoingAuthorization();
                                    }
                                }

                                @Override
                                public void failed(SDKException e) {
                                    Log.d(TAG, "failed authorization");
                                    if(manager.isInProgress()) {
                                        manager.cancelOngoingAuthorization();
                                    }
                                }
                            });
                        }

                        @Override
                        public void failed(SDKException e) {
                            Log.d(TAG, "failed: session not created");
                        }
                    });
                    Log.d(TAG, "onResponse: completed");    
                } else {
                    Log.d(TAG, "onResponse: input stream null");
                }
                
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: failed");
            }
        });
    }

    public synchronized RequestHandler getQueryService() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        if (queryService == null) {
            queryService = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(RequestHandler.queryServer)
                    .client(okHttpClient)
                    .build()
                    .create(RequestHandler.class);
        }
        return queryService;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: got result");
        Log.d(TAG, "onActivityResult: requestCode: "+requestCode);
        Log.d(TAG, "onActivityResult: resultCode: "+resultCode);
        DigiMeClient.getInstance().getAuthManager().onActivityResult(requestCode, resultCode, data);
        if(resultCode!= 0){
//            getFiles();
        } else {
//            restartApp();
        }
    }
}
