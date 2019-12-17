/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;

class OPACClient {

    private String domain;
    private AsyncHttpClient client;
    private Context context;
    private SharedPreferences sharedPreferences;

    OPACClient(Context context) {
        this.domain = context.getString(R.string.lib_catalog_domain);
        this.client = new AsyncHttpClient();
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("library-catalog",Context.MODE_PRIVATE);
    }

    void init(final InitResponse initResponse) throws JSONException {
        if(sharedPreferences.contains("mappings")){
            sendInitData(new JSONObject(sharedPreferences.getString("mappings","")),initResponse);
        }
        this.client.get(this.domain + "/mappings", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    sharedPreferences.edit().putString("mappings",jsonObject.toString()).apply();
                    Log.i("LIBRARY-CATALOG","MAPPINGS CACHED");
                    sendInitData(jsonObject, initResponse);
                } catch (JSONException e) {
                    initResponse.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                initResponse.onFailure(new Exception(error));
            }
        });
    }

    void searchResults(int docType, int field, final String search, final SearchResponse searchResponse) {
        RequestParams params = new RequestParams();
        params.add("docType", String.valueOf(docType));
        params.add("field", String.valueOf(field));
        params.add("q", search);

        this.client.get(this.domain + "/search", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    searchResponse.onSuccess(
                            jsonObject.getJSONArray("data"),
                            jsonObject.getString("action"),
                            jsonObject.getString("user_name")
                    );
                } catch (JSONException e) {
                    searchResponse.onFailure(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                searchResponse.onFailure(new Exception(error));
            }
        });
    }

    private void sendInitData(JSONObject response, final InitResponse initResponse) throws JSONException {
        JSONArray docTypes = response.getJSONArray("docTypes");
        JSONArray fields = response.getJSONArray("fields");
        Map<String, Integer> docTypesMap = new HashMap<>();
        for (int i = 0; i < docTypes.length(); ++i) {
            JSONObject current = docTypes.getJSONObject(i);
            docTypesMap.put(current.getString("text"), current.getInt("id"));
        }

        Map<String, Integer> fieldsMap = new HashMap<>();
        for (int i = 0; i < fields.length(); ++i) {
            JSONObject current = fields.getJSONObject(i);
            fieldsMap.put(current.getString("text"), current.getInt("id"));
        }
        initResponse.onSuccess(docTypesMap, fieldsMap);
    }

    public void getBookDetails(String action, String username, int id, final BookDetailResponse bookDetailResponse){
        RequestParams params = new RequestParams();
        params.add("action",action);
        params.add("user_name",username);
        params.add("id", String.valueOf(id));

        this.client.post(this.domain + "/get-book-details", params,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    bookDetailResponse.onSuccess(new JSONObject(new String(responseBody)));
                } catch (JSONException e) {
                    e.printStackTrace();
                    bookDetailResponse.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                bookDetailResponse.onFailure(new Exception(error));
            }
        });
    }

    public void getProfile(String username,String password,final BookDetailResponse bookDetailResponse){
        RequestParams params = new RequestParams();
        params.add("memberid",username);
        params.add("password",password);

        this.client.post(this.domain + "/checkouts", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    bookDetailResponse.onSuccess(new JSONObject(new String(responseBody)));
                } catch (JSONException e) {
                    e.printStackTrace();
                    bookDetailResponse.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                bookDetailResponse.onFailure(new Exception(error));
            }
        });
    }
}
