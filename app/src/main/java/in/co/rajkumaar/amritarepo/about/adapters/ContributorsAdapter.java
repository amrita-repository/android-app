/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.about.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.models.Contributor;

public class ContributorsAdapter extends ArrayAdapter<Contributor> {

    public ContributorsAdapter(Context context, ArrayList<Contributor> contributors) {
        super(context, 0, contributors);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.contributor_item, parent, false);
        }

        final Contributor current = getItem(position);
        ImageView imageView = listItemView.findViewById(R.id.image);
        TextView name = listItemView.findViewById(R.id.name);
        TextView username = listItemView.findViewById(R.id.username);

        Picasso.get().load(current.getImage()).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileIntent(Uri.parse(current.getProfile()));
            }
        });

        name.setText(current.getName());

        SpannableString content = new SpannableString('@' + current.getUsername());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        username.setText(content);
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileIntent(Uri.parse(current.getProfile()));
            }
        });

        return listItemView;
    }

    private void profileIntent(Uri data) {
        Intent profileIntent = new Intent(Intent.ACTION_VIEW);
        profileIntent.setData(data);
        getContext().startActivity(profileIntent);
    }
}
