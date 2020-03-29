/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.papers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.List;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;

public class PaperAdapter extends ArrayAdapter<String> {
    private static final Random RANDOM = new Random();
    private final Context context;
    private final String type;
    private customListener customListener;

    public PaperAdapter(Context context, List<String> settings, String type) {
        super(context, 0, settings);
        this.context = context;
        this.type = type;
    }

    public void setCustomListener(customListener listener) {
        this.customListener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.paper_item, parent, false);
        }

        final String current = getItem(position);
        TextView name = listItemView.findViewById(R.id.title);
        name.setText(current);

        int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
        if (type.equals("sem")) {
            listItemView.findViewById(R.id.image).setVisibility(View.GONE);
            listItemView.findViewById(R.id.icon).setVisibility(View.VISIBLE);
            MaterialLetterIcon icon = listItemView.findViewById(R.id.icon);
            icon.setLetter(current.substring(0, 1));
            icon.setShapeColor(mMaterial_Colors[RANDOM.nextInt(mMaterial_Colors.length)]);
        } else {
            listItemView.findViewById(R.id.icon).setVisibility(View.GONE);
            listItemView.findViewById(R.id.image).setVisibility(View.VISIBLE);
            ImageView imageView = listItemView.findViewById(R.id.image);
            imageView.setImageDrawable(new IconDrawable(context,
                    (type.equals("assessments") || type.equals("examcategory") ? FontAwesomeIcons.fa_folder_open :
                            type.equals("subjects") ? FontAwesomeIcons.fa_file_pdf_o : FontAwesomeIcons.fa_file_o))
                    .color(mMaterial_Colors[RANDOM.nextInt(mMaterial_Colors.length)]));
        }
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