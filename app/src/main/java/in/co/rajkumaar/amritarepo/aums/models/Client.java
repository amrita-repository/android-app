/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.models;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import java.security.KeyStore;
import java.util.Date;

import cz.msebera.android.httpclient.cookie.Cookie;
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
        client.setLoggingEnabled(false);
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

    // Initially, on submitting login form, the JSESSIONID in the response has path as '/aums/Jsp/Core_Common'
    // This prevents the cookie to be sent over requests that don't have such a prefix.
    // Hence we are manually removing that cookie, rewriting the same content with path as '/'
    public void setJSessionIDPathAsRoot() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("JSESSIONID") && !cookie.getPath().equals("/")) {
                cookieStore.deleteCookie(cookie);
                cookieStore.addCookie(new Cookie() {
                    @Override
                    public String getName() {
                        return cookie.getName();
                    }

                    @Override
                    public String getValue() {
                        return cookie.getValue();
                    }

                    @Override
                    public String getComment() {
                        return null;
                    }

                    @Override
                    public String getCommentURL() {
                        return null;
                    }

                    @Override
                    public Date getExpiryDate() {
                        return cookie.getExpiryDate();
                    }

                    @Override
                    public boolean isPersistent() {
                        return cookie.isPersistent();
                    }

                    @Override
                    public String getDomain() {
                        return cookie.getDomain();
                    }

                    @Override
                    public String getPath() {
                        return "/";
                    }

                    @Override
                    public int[] getPorts() {
                        return cookie.getPorts();
                    }

                    @Override
                    public boolean isSecure() {
                        return cookie.isSecure();
                    }

                    @Override
                    public int getVersion() {
                        return cookie.getVersion();
                    }

                    @Override
                    public boolean isExpired(Date date) {
                        return cookie.isExpired(date);
                    }
                });
            }
        }
    }

    public AsyncHttpClient getClient() {
        return client;
    }

    public void clearCookie() {
        cookieStore.clear();
    }
}
