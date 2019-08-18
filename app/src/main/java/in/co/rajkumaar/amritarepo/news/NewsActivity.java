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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class NewsActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        listView = findViewById(R.id.list);

        TextView quoteView = findViewById(R.id.quote);
        String[] quotes = getResources().getStringArray(R.array.quotes);
        String randomQuote = quotes[new Random().nextInt(quotes.length)];
        quoteView.setText(randomQuote);

        Utils.showSmallAd(this, (com.google.android.gms.ads.AdView) findViewById(R.id.banner_container));

        NewsClient newsClient = new NewsClient();
        newsClient.getArticles(new NewsResponse() {
            @Override
            public void onSuccess(ArrayList<NewsItem> newsItems) {
                NewsAdapter newsAdapter = new NewsAdapter(NewsActivity.this, newsItems);
                listView.setAdapter(newsAdapter);
                listView.setVisibility(View.VISIBLE);
                listView.setEmptyView(findViewById(R.id.empty_view));
                findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e("Exception", exception.getMessage());
                Utils.showUnexpectedError(NewsActivity.this);
                finish();
            }
        });
    }
}
