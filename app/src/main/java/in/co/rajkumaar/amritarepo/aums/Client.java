package in.co.rajkumaar.amritarepo.aums;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

class Client {
    private AsyncHttpClient client;
    private PersistentCookieStore cookieStore;


    Client(Context context){
        client = new AsyncHttpClient();
        cookieStore=new PersistentCookieStore(context);
        client.setEnableRedirects(true);
        client.setTimeout(10);
        cookieStore.clear();
        client.setCookieStore(cookieStore);
        client.setLoggingEnabled(true);
        client.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
    }

    AsyncHttpClient getClient() {
        return client;
    }

    void clearCookie(){
        cookieStore.clear();
    }
    public PersistentCookieStore getCookieStore() {
        return cookieStore;
    }
}
