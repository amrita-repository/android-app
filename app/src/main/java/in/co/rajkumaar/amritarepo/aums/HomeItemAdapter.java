package in.co.rajkumaar.amritarepo.aums;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;

public class HomeItemAdapter extends ArrayAdapter<HomeItem> {

    HomeItemAdapter(Context context, ArrayList<HomeItem> HomeItems) {
        super(context, 0,HomeItems);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.home_item, parent, false);
        }


        final HomeItem current=getItem(position);


        TextView title = listItemView.findViewById(R.id.title);
        ImageView imageView = listItemView.findViewById(R.id.image);

        title.setText(current.getName());
        imageView.setImageResource(current.getImage());




        return listItemView;

    }
}
