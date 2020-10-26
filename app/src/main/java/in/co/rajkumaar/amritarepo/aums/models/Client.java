/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.models;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import java.security.KeyStore;

import in.co.rajkumaar.amritarepo.aums.helpers.CustomSSLFactory;

public class Client {
    private AsyncHttpClient client;
    private PersistentCookieStore cookieStore;


    public Client(Context context) {
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(context);
        client.setEnableRedirects(true);
        client.setTimeout(10);
        cookieStore.clear();
        client.setCookieStore(cookieStore);
        client.setLoggingEnabled(true);
        client.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");

        // This is literally an unsafe hack to bypass the intermediate CA check by the HTTP client.
        // This was necessary because AUMS doesn't send its intermediate CA in its certificate chain.
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            CustomSSLFactory socketFactory = new CustomSSLFactory(trustStore);
            client.setSSLSocketFactory(socketFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AsyncHttpClient getClient() {
        return client;
    }

    public void clearCookie() {
        cookieStore.clear();
    }
}
