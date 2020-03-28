/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.co.rajkumaar.amritarepo.BuildConfig;

class OPACClient {

    private String domain = BuildConfig.LIB_CATALOG;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    OPACClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        sharedPreferences = context.getSharedPreferences("library-catalog", Context.MODE_PRIVATE);
    }

    void init(final InitResponse initResponse) throws JSONException {
        if (sharedPreferences.contains("mappings")) {
            sendInitData(new JSONObject(sharedPreferences.getString("mappings", "")), initResponse);
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.domain + "/mappings",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            sharedPreferences.edit().putString("mappings", jsonObject.toString()).apply();
                            Log.i("LIBRARY-CATALOG", "MAPPINGS CACHED");
                            sendInitData(jsonObject, initResponse);
                        } catch (JSONException e) {
                            initResponse.onFailure(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                initResponse.onFailure(error);
            }
        });
        requestQueue.add(stringRequest);
    }

    public void searchResults(final int docType, final int field, final String search, final SearchResponse searchResponse) {
        String uri = String.format(this.domain + "/search?docType=%s&field=%s&q=%s", docType, field, search);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
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
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                searchResponse.onFailure(error);
            }
        });
        requestQueue.add(stringRequest);
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

    public void getBookDetails(final String action, final String username, final int id, final BookDetailResponse bookDetailResponse) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.domain + "/get-book-details",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            bookDetailResponse.onSuccess(new JSONObject(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            bookDetailResponse.onFailure(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                bookDetailResponse.onFailure(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", action);
                params.put("user_name", username);
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void getProfile(final String username, final String password, final BookDetailResponse bookDetailResponse) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.domain + "/checkouts",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            bookDetailResponse.onSuccess(new JSONObject(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            bookDetailResponse.onFailure(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                bookDetailResponse.onFailure(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("memberid", username);
                params.put("password", password);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}
