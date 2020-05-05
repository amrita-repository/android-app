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
        String folderCheck = "Folder";
        String[] web = {"html", "htm", "mhtml"};
        String[] computer = {"exe", "dmg", "iso", "msi"};
        String[] document = {"doc", "docx", "rtf", "odt"};
        String[] pdf = {"pdf"};
        String[] powerpoint = {"ppt", "pps", "pptx"};
        String[] excel = {"xls", "xlsx", "ods"};
        String[] image = {"png", "gif", "jpg", "jpeg", "bmp"};
        String[] video = {"mp4", "mp3", "avi", "mov", "mpg", "mkv", "wmv"};
        String[] compressed = {"rar", "zip", "zipx", "tar", "7z", "gz"};

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

    private boolean isExtension(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }
}