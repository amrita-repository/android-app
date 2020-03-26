/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;

public class NotificationsAdapter extends ArrayAdapter<Notification> {
    private static final Random RANDOM = new Random();

    NotificationsAdapter(Context context, List<Notification> contributors) {
        super(context, 0, contributors);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.notification_item, parent, false);
        }

        final Notification current = getItem(position);
        int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
        TextView title = listItemView.findViewById(R.id.title);
        TextView body = listItemView.findViewById(R.id.body);
        ImageView imageView = listItemView.findViewById(R.id.image);
        TextView time = listItemView.findViewById(R.id.time);

        Iconify.with(new FontAwesomeModule());

        imageView.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_bell).color(mMaterial_Colors[RANDOM.nextInt(mMaterial_Colors.length)]));
        title.setText(current.getTitle());
        body.setText(current.getBody());

        PrettyTime prettyTime = new PrettyTime();
        time.setText(prettyTime.format(new Date(current.getCreatedAt())));
        return listItemView;
    }
}