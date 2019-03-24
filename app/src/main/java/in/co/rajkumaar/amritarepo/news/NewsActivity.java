package in.co.rajkumaar.amritarepo.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.WebViewActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class NewsActivity extends AppCompatActivity {

    private ArrayList<NewsItem> newsArticles;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        listView = findViewById(R.id.list);
        retrieveNews();
    }

    private void retrieveNews() {
        newsArticles = new ArrayList<>();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://www.amrita.edu/campus/Coimbatore/news", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Document doc = Jsoup.parse(new String(bytes));
                Elements articles = doc.select("article");
                for (Element article : articles) {
                    try {
                        String imageUrl = article.select("img.img-responsive").first().attr("src");
                        String title = article.select(".field-name-title").first().text();
                        String url = "https://www.amrita.edu" + article.select(".field-name-node-link > div > div > a").first().attr("href");
                        newsArticles.add(new NewsItem(imageUrl, title, url));
                    } catch (Exception e) {
                        Utils.showUnexpectedError(NewsActivity.this);
                        finish();
                        Crashlytics.log(e.getLocalizedMessage());
                    }
                }
                NewsAdapter gradesAdapter = new NewsAdapter(NewsActivity.this, newsArticles);
                listView.setAdapter(gradesAdapter);
                listView.setVisibility(View.VISIBLE);
                listView.setEmptyView(findViewById(R.id.empty_view));
                findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Utils.showUnexpectedError(NewsActivity.this);
                finish();
            }
        });
    }


    private class NewsItem {
        private final String imageUrl;
        private final String title;
        private final String link;

        NewsItem(String imageUrl, String title, String link) {
            this.imageUrl = imageUrl;
            this.title = title;
            this.link = link;
        }


        public String getImageUrl() {
            return imageUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }
    }

    class NewsAdapter extends ArrayAdapter<NewsItem> {
        NewsAdapter(Context context, ArrayList<NewsItem> HomeItems) {
            super(context, 0, HomeItems);
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.news_item, parent, false);
            }


            final NewsItem current = getItem(position);


            TextView title = listItemView.findViewById(R.id.title);
            TextView url = listItemView.findViewById(R.id.url);
            ImageView image = listItemView.findViewById(R.id.image);

            url.setText(current.getLink());
            title.setText(current.getTitle());
            Picasso.get().load(current.getImageUrl()).into(image);

            listItemView.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isConnected(getContext()))
                        startActivity(new Intent(getContext(), WebViewActivity.class).putExtra("webview", current.getLink())
                                .putExtra("title", current.getTitle()));
                    else
                        Utils.showInternetError(getContext());
                }
            });


            return listItemView;

        }
    }


}
