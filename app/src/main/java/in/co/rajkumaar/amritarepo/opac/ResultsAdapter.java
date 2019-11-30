/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

/*
 * Copyright (c) 2019 RAJKUMAR S
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.List;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;

public class ResultsAdapter extends ArrayAdapter<String> {

    private static final Random RANDOM = new Random();
    private final Context context;
    private customListener customListener;

    ResultsAdapter(Context context, List<String> titles) {
        super(context, 0, titles);
        this.context = context;
    }

    void setCustomListener(customListener listener) {
        this.customListener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.paper_item, parent, false);
        }

        final String current = getItem(position);
        TextView name = listItemView.findViewById(R.id.title);
        name.setText(current);

        int[] mMaterialColors = getContext().getResources().getIntArray(R.array.colors);
        listItemView.findViewById(R.id.icon).setVisibility(View.GONE);
        listItemView.findViewById(R.id.image).setVisibility(View.VISIBLE);
        ImageView imageView = listItemView.findViewById(R.id.image);
        imageView.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_book)
                .color(mMaterialColors[RANDOM.nextInt(mMaterialColors.length)]));

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customListener != null) {
                    customListener.onItemClickListener(position);
                }
            }
        });
        return listItemView;

    }


    public interface customListener {
        void onItemClickListener(int position);
    }

}