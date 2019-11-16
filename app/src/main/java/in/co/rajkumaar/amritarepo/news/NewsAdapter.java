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

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.WebViewActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

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
                    openWebView(current.getLink(), current.getTitle());
                else
                    Utils.showInternetError(getContext());
            }
        });
        return listItemView;
    }

    private void openWebView(String link, String title) {
        getContext().startActivity(
                new Intent(getContext(), WebViewActivity.class)
                        .putExtra("webview", link)
                        .putExtra("title", title));
    }
}