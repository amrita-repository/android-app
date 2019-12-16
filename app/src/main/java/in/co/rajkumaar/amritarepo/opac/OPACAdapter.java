/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.joanzapata.iconify.IconDrawable;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;

class OPACAdapter extends ArrayAdapter<OPACHomeItem> {

    private Context context;

    public OPACAdapter(Context context, ArrayList<OPACHomeItem> items) {
        super(context, 0, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_opac_grid, parent, false);
        }

        final OPACHomeItem current = getItem(position);

        ImageView icon = listItemView.findViewById(R.id.landing_picture);
        icon.setImageDrawable(new IconDrawable(context, current.getImage()).color(Color.parseColor(current.getColor())));

        TextView title = listItemView.findViewById(R.id.landing_text);
        title.setText(current.getName());

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (current.getName()){
                    case "Search Books" :
                        context.startActivity(new Intent(context,OPACSearchActivity.class));
                        break;
                    case "E-Resources":
                        context.startActivity(new Intent(context,EResourcesActivity.class));
                        break;
                    case "Useful Sites":
                        context.startActivity(new Intent(context,EResourcesActivity.class).putExtra("useful-sites",true));
                        break;
                    case "Account":
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);

                        alert.setTitle("Authentication");
                        alert.setMessage("Enter your member ID (Contact Library Dept if you don\'t know!)");

                        final EditText input = new EditText(context);
                        alert.setView(input);

                        alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                context.startActivity(new Intent(context,OPACAccountActivity.class).putExtra("id",value));
                            }
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });

                        alert.show();
                        break;

                }
            }
        });


        return listItemView;
    }
}
