package citi.g500.security;

import android.app.Activity;
import android.net.Proxy;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Challenge;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Route;
import com.squareup.okhttp.internal.spdy.FrameReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import citi.g500.core.LoginActivity;
import citi.g500.messaging.SessionHandler;

/**
 * Created by ftorres on 29/09/2016.
 */
public class CustomAuthenticator {

    private static final String TAG = "CustomAuthenticator";
    private Activity activity;
    private final OkHttpClient client;
    private Handler mHandler;

    public CustomAuthenticator(Activity activity) {
        this.activity = activity;
        this.client = this.getUnsafeOkHttpClient();
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void authenticate(final String user, final String password) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("grant_type", "password")
                .add("username", user)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(SessionHandler.AUTH_SERVER_URL)
                .post(formBody)
                .build();
        request = request.newBuilder().addHeader(
                "Authorization",
                Credentials.basic(SessionHandler.CLIENT_KEY, SessionHandler.PRIVATE_KEY)).build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.d(TAG, "Retrieving token failed!: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        try {
                            //Retrieving response
                            JSONObject document = new JSONObject(new String(response.body().bytes()));
                            //Parsing access token and token type
                            String accessToken = document.getString("access_token") + "";
                            String tokenType = document.getString("token_type");
                            //Setting to session variable
                            SessionHandler.ACCESS_TOKEN = accessToken;
                            SessionHandler.TOKEN_TYPE = tokenType;
                            //Callback for checking token and access my database
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //Call Login process
                                    LoginActivity loginActivity = (LoginActivity) activity;
                                    loginActivity.doLogin(user, password);
                                }
                            });
                            Log.d(TAG, accessToken + ":" + tokenType);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "YES");
                    }
                });
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
