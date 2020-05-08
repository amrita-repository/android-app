/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.joanzapata.iconify.IconDrawable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.Encryption;

class OPACAdapter extends ArrayAdapter<OPACHomeItem> {

    private Context context;
    private Encryption enc;

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
                switch (current.getName()) {
                    case "Search Books":
                        context.startActivity(new Intent(context, OPACSearchActivity.class));
                        break;
                    case "E-Resources":
                        context.startActivity(new Intent(context, EResourcesActivity.class));
                        break;
                    case "Useful Sites":
                        context.startActivity(new Intent(context, EResourcesActivity.class).putExtra("useful-sites", true));
                        break;
                    case "Account":
                        Dialog dj = new Dialog(context);
                        dj.setContentView(R.layout.library_patron_dialog);
                        final EditText username = dj.findViewById(R.id.id);
                        final EditText password = dj.findViewById(R.id.password);
                        final CheckBox remember = dj.findViewById(R.id.remember_me);

                        final SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
                        String rmusername = null;
                        String rmpassword = null;
                        try {
                            rmusername = pref.getString("OPAC_username", null);
                            rmpassword = pref.getString("OPAC_password", null);

                            enc = new Encryption(context, "user");

                            if (rmusername != null) {
                                rmusername = new String(enc.decrypt(rmusername.getBytes(StandardCharsets.UTF_8)));
                            }
                            if (rmpassword != null) {
                                rmpassword = new String(enc.decrypt(rmpassword.getBytes(StandardCharsets.UTF_8)));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        username.setText(rmusername);
                        password.setText(rmpassword);
                        Button bt = dj.findViewById(R.id.submit);
                        bt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences.Editor ed = pref.edit();
                                if (remember.isChecked()) {
                                    try {
                                        String encName = enc.encrypt(username.getText().toString().getBytes(StandardCharsets.UTF_8));
                                        String encPass = enc.encrypt(password.getText().toString().getBytes(StandardCharsets.UTF_8));
                                        ed.putString("OPAC_username", encName);
                                        ed.putString("OPAC_password", encPass);
                                        ed.apply();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    ed.putString("OPAC_username", null);
                                    ed.putString("OPAC_password", null);
                                    ed.apply();
                                }
                                context.startActivity(new Intent(getContext(), OPACAccountActivity.class)
                                        .putExtra("id", username.getText().toString())
                                        .putExtra("password", password.getText().toString())
                                );
                            }
                        });
                        dj.show();
                        break;
                }
            }
        });


        return listItemView;
    }
}
