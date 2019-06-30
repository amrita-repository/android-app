/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class OPACClient {

    private String domain;
    private AsyncHttpClient client;

    public OPACClient(){
        this.domain = "http://172.17.9.22";
        this.client = new AsyncHttpClient();
    }

    public void init(final InitResponse initResponse){
        this.client.get(this.domain + "/cgi-bin/lsbrows1.cgi?"+ URLEncoder.encode("Database_no_opt=++++"), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                sendInitData(response,initResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                initProxy(initResponse);
            }
        });
    }

    public void searchResults(String username,int docType,int field,String search,final SearchResponse searchResponse){
        RequestParams params = new RequestParams();
        params.add("user_name",username);
        params.add("Docu_type", String.valueOf(docType));
        params.add("FIELD", String.valueOf(field));
        params.add("T",search);
        params.add("OPTION", String.valueOf(2));
        params.add("ch_period", String.valueOf(0));
        params.add("TR","");
        this.client.addHeader("Content-Type","application/x-www-form-urlencoded");
        this.client.post(this.domain + "/cgi-bin/lsbrows6N.cgi", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void initProxy(final InitResponse initResponse){

    }

    private void sendInitData(String response,final InitResponse initResponse){
        Document document = Jsoup.parse(response);
        Element element = document.select("form[name=form_s] > input[name=user_name]").first();
        String username = element.attr("value");
        Elements docTypes = document.select("select[name=Docu_type]").first().getElementsByTag("option");
        Map<String,Integer> docTypesMap = new HashMap<>();
        for (Element type:docTypes) {
            docTypesMap.put(type.text(),Integer.parseInt(type.attr("value")));
        }
        Elements fields = document.select("select[name=FIELD]").first().getElementsByTag("option");
        Map<String,Integer> fieldsMap = new HashMap<>();
        for (Element type:fields) {
            fieldsMap.put(type.text(),Integer.parseInt(type.attr("value")));
        }
        initResponse.onSuccess(username,docTypesMap,fieldsMap);
    }
}
