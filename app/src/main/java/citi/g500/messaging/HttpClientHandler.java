package citi.g500.messaging;

/**
 * Created by ftorres on 21/09/2016.
 */

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class HttpClientHandler {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Accept", "application/json");
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, cz.msebera.android.httpclient.HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Accept", SessionHandler.CONTENT_TYPE);
        client.addHeader("Content-Type", SessionHandler.CONTENT_TYPE);
        client.addHeader("Authorization", SessionHandler.TOKEN_TYPE + " " + SessionHandler.ACCESS_TOKEN);
        client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return SessionHandler.BASE_URL + relativeUrl;
    }

}
