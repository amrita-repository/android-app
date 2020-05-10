/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.helpers;

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
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.aums.models.CourseResource;

import static android.view.View.GONE;
import static in.co.rajkumaar.amritarepo.helpers.Utils.compressed;
import static in.co.rajkumaar.amritarepo.helpers.Utils.computer;
import static in.co.rajkumaar.amritarepo.helpers.Utils.document;
import static in.co.rajkumaar.amritarepo.helpers.Utils.excel;
import static in.co.rajkumaar.amritarepo.helpers.Utils.folderCheck;
import static in.co.rajkumaar.amritarepo.helpers.Utils.image;
import static in.co.rajkumaar.amritarepo.helpers.Utils.isExtension;
import static in.co.rajkumaar.amritarepo.helpers.Utils.pdf;
import static in.co.rajkumaar.amritarepo.helpers.Utils.powerpoint;
import static in.co.rajkumaar.amritarepo.helpers.Utils.video;
import static in.co.rajkumaar.amritarepo.helpers.Utils.web;

public class CourseResAdapter extends ArrayAdapter<CourseResource> {
    private final Random random;
    private final Context context;

    public CourseResAdapter(Context context, ArrayList<CourseResource> Resources) {
        super(context, 0, Resources);
        this.context = context;
        random = new Random();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.home_item, parent, false);
        }
        final CourseResource current = getItem(position);
        assert current != null;
        String resType = current.getType();

        int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
        TextView title = listItemView.findViewById(R.id.title);
        ImageView imageView = listItemView.findViewById(R.id.image);
        ImageView toRight = listItemView.findViewById(R.id.right);
        Icon icon;
        int colorVal = random.nextInt(mMaterial_Colors.length);

        if (isExtension(web, resType)) {
            icon = FontAwesomeIcons.fa_file_code_o;
            colorVal = 0;
        } else if (resType.equals(folderCheck)) {
            icon = FontAwesomeIcons.fa_folder_open;
        } else if (isExtension(computer, resType)) {
            icon = FontAwesomeIcons.fa_file_code_o;
            colorVal = 7;
        } else if (isExtension(document, resType)) {
            icon = FontAwesomeIcons.fa_file_word_o;
            colorVal = 6;
        } else if (isExtension(pdf, resType)) {
            icon = FontAwesomeIcons.fa_file_pdf_o;
            colorVal = 1;
        } else if (isExtension(powerpoint, resType)) {
            icon = FontAwesomeIcons.fa_file_powerpoint_o;
            colorVal = 2;
        } else if (isExtension(excel, resType)) {
            icon = FontAwesomeIcons.fa_file_excel_o;
            colorVal = 4;
        } else if (isExtension(image, resType)) {
            icon = FontAwesomeIcons.fa_file_image_o;
        } else if (isExtension(video, resType)) {
            icon = FontAwesomeIcons.fa_file_video_o;
        } else if (isExtension(compressed, resType)) {
            icon = FontAwesomeIcons.fa_file_zip_o;
            colorVal = 3;
        } else {
            icon = FontAwesomeIcons.fa_file_text;
        }
        title.setText(current.getResourceFileName());
        title.setTextSize(18);
        imageView.setImageDrawable(new IconDrawable(context, icon)
                .color(mMaterial_Colors[colorVal]));

        if (!(current.getType().equals("Folder"))) {
            toRight.setVisibility(GONE);
        }
        return listItemView;
    }
}