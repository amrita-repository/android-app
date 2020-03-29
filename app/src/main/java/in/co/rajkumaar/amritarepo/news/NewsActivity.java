/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.news;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class NewsActivity extends BaseActivity {

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


        NewsClient newsClient = new NewsClient(this);
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
                exception.printStackTrace();
                Utils.showUnexpectedError(NewsActivity.this);
                finish();
            }
        });
    }
}
