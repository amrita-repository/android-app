/*
 * MIT License
 *
 * Copyright (c) 2019 RAJKUMAR S
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package in.co.rajkumaar.amritarepo.news;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

class NewsClient {

    private static String AMRITA_URL = "https://www.amrita.edu";
    private final AsyncHttpClient client;
    private ArrayList<NewsItem> newsArticles;
    private String newsURL;

    NewsClient() {
        this.client = new AsyncHttpClient();
        this.newsArticles = new ArrayList<>();
        this.newsURL = "https://www.amrita.edu/campus/Coimbatore/news";
    }

    void getArticles(final NewsResponse newsResponse) {
        client.get(this.newsURL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Document doc = Jsoup.parse(new String(bytes));
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

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                newsResponse.onFailure(new Exception(throwable));
            }
        });
    }
}
