/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.news;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

class NewsClient {

    private static String AMRITA_URL = "https://www.amrita.edu";
    private ArrayList<NewsItem> newsArticles;
    private String newsURL;
    private RequestQueue requestQueue;

    NewsClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        this.newsArticles = new ArrayList<>();
        this.newsURL = "https://www.amrita.edu/campus/Coimbatore/news";
    }

    void getArticles(final NewsResponse newsResponse) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, newsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document doc = Jsoup.parse(response);
                        Elements articles = doc.select("article");
                        for (Element article : articles) {
                            try {
                                String imageUrl = article.select("img.img-responsive").first().attr("src");
                                String title = article.select(".field-name-title").first().text();
                                String url = AMRITA_URL + article.select(".field-name-node-link > div > div > a").first().attr("href");
                                newsArticles.add(new NewsItem(imageUrl, title, url));
                            } catch (Exception e) {
                                newsResponse.onFailure(e);
                            }
                        }
                        newsResponse.onSuccess(newsArticles);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                newsResponse.onFailure(error);
            }
        });
        requestQueue.add(stringRequest);
    }
}
