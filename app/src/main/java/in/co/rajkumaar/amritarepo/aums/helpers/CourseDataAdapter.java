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

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.aums.models.CourseData;

public class CourseDataAdapter extends ArrayAdapter<CourseData> {
    private final Random random;
    private final Context context;

    public CourseDataAdapter(Context context, ArrayList<CourseData> Courses) {
        super(context, 0, Courses);
        this.context = context;
        random = new Random();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.course_item, parent, false);
        }
        final CourseData current = getItem(position);

        int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
        TextView courseName = listItemView.findViewById(R.id.title);
        TextView courseCode = listItemView.findViewById(R.id.code);
        ImageView imageView = listItemView.findViewById(R.id.image);

        courseName.setText(current.getCourseName());
        courseCode.setText(current.getCourseCode());
        imageView.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_folder_open)
                .color(mMaterial_Colors[random.nextInt(mMaterial_Colors.length)]));

        return listItemView;
    }
}