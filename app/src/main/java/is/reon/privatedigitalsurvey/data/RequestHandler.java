package is.reon.privatedigitalsurvey.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by gunnar on 18.4.2018.
 */

public interface RequestHandler {

    String queryServer = "http://<< your computers ip >>:5000/";

    @GET("/")
    Call<ResponseBody> getP12file();
}
