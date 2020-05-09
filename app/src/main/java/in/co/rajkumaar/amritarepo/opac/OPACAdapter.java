/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Base64;
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

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Encryption;

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
        TextView title = listItemView.findViewById(R.id.landing_text);
        if (current != null) {
            icon.setImageDrawable(new IconDrawable(context, current.getImage()).color(Color.parseColor(current.getColor())));
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

                            final SharedPreferences pref = Encryption.getEncPrefs(context, "opac_account");
                            String rmUsername = pref.getString("OPAC_username", null);
                            String rmPassword = pref.getString("OPAC_password", null);

                            if (rmUsername != null) {
                                rmUsername = new String(Base64.decode(rmUsername, Base64.DEFAULT));
                                username.setText(rmUsername);
                                remember.setChecked(true);
                            }
                            if (rmPassword != null) {
                                rmPassword = new String(Base64.decode(rmPassword, Base64.DEFAULT));
                                password.setText(rmPassword);
                                remember.setChecked(true);
                            }


                            Button bt = dj.findViewById(R.id.submit);
                            bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    SharedPreferences.Editor ed = pref.edit();
                                    if (remember.isChecked()) {
                                        try {
                                            String encName = Base64.encodeToString(username.getText().toString().getBytes(), Base64.DEFAULT);
                                            String encPass = Base64.encodeToString(password.getText().toString().getBytes(), Base64.DEFAULT);
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
        }


        return listItemView;
    }
}
