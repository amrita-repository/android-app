/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class DocumentsItemAdapter extends ArrayAdapter<String> {
    private final Random random;
    private final Context context;

    public DocumentsItemAdapter(Context context, ArrayList<String> Documents) {
        super(context, 0, Documents);
        this.context = context;
        random = new Random();
    }

    public static boolean isExtension(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.home_item, parent, false);
        }
        final String current = getItem(position);
        String currentType;
        if (Objects.requireNonNull(current).lastIndexOf('.') == -1) {
            currentType = "Folder";
        } else {
            currentType = current.substring(current.lastIndexOf('.') + 1);
        }

        int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
        TextView title = listItemView.findViewById(R.id.title);
        ImageView imageView = listItemView.findViewById(R.id.image);
        ImageView toRight = listItemView.findViewById(R.id.right);
        Icon icon;
        int colorVal = random.nextInt(mMaterial_Colors.length);

        if (isExtension(Utils.web, currentType)) {
            icon = FontAwesomeIcons.fa_file_code_o;
            colorVal = 0;
        } else if (currentType.equals(Utils.folderCheck)) {
            icon = FontAwesomeIcons.fa_folder_open;
        } else if (isExtension(Utils.computer, currentType)) {
            icon = FontAwesomeIcons.fa_file_code_o;
            colorVal = 7;
        } else if (isExtension(Utils.document, currentType)) {
            icon = FontAwesomeIcons.fa_file_word_o;
            colorVal = 6;
        } else if (isExtension(Utils.pdf, currentType)) {
            icon = FontAwesomeIcons.fa_file_pdf_o;
            colorVal = 1;
        } else if (isExtension(Utils.powerpoint, currentType)) {
            icon = FontAwesomeIcons.fa_file_powerpoint_o;
            colorVal = 2;
        } else if (isExtension(Utils.excel, currentType)) {
            icon = FontAwesomeIcons.fa_file_excel_o;
            colorVal = 4;
        } else if (isExtension(Utils.image, currentType)) {
            icon = FontAwesomeIcons.fa_file_image_o;
        } else if (isExtension(Utils.video, currentType)) {
            icon = FontAwesomeIcons.fa_file_video_o;
        } else if (isExtension(Utils.compressed, currentType)) {
            icon = FontAwesomeIcons.fa_file_zip_o;
            colorVal = 3;
        } else {
            icon = FontAwesomeIcons.fa_file_text;
        }
        if (!(currentType.equals(Utils.folderCheck))) {
            toRight.setVisibility(View.INVISIBLE);
        }
        if ("Go Back".equalsIgnoreCase(current)) {
            toRight.setVisibility(View.INVISIBLE);
            colorVal = 5;
            icon = FontAwesomeIcons.fa_caret_left;
        }
        title.setText(current);
        title.setTextSize(18);
        imageView.setImageDrawable(new IconDrawable(context, icon)
                .color(mMaterial_Colors[colorVal]));

        return listItemView;
    }
}