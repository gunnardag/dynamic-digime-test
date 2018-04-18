package is.reon.privatedigitalsurvey;

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
                byte[] fileReader = new byte[4096];

                long fileSize = response.body().contentLength();
                long fileSizeDownloaded = 0;

                inputStream = response.body().byteStream();

                Log.d(TAG, "fileSize: "+fileSize);
                if(inputStream != null) {
                    DigiMeClient.getDefaultKeyLoader().addKeyFromPKCS12Stream(inputStream, "<< contract password >>");
                    DigiMeClient.getInstance().createSession("<< contract id >>", new SDKCallback<CASession>() {
                        @Override
                        public void succeeded(SDKResponse<CASession> sdkResponse) {
                            Log.d(TAG, "succeeded: session created");
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
}
