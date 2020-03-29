/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import in.co.rajkumaar.amritarepo.R;

import static in.co.rajkumaar.amritarepo.helpers.Utils.THEME_DARK;
import static in.co.rajkumaar.amritarepo.helpers.Utils.THEME_LIGHT;

public class SettingsActivity extends BaseActivity {

    private TextView currentProgram;
    private SharedPreferences pref;
    private Button changeProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        currentProgram = findViewById(R.id.current_program);
        changeProgram = findViewById(R.id.change_program);
        pref = getSharedPreferences("user", MODE_PRIVATE);
        Switch darkMode = findViewById(R.id.changeTheme);
        darkMode.setChecked(themePrefs.getString("theme", THEME_LIGHT).equals(THEME_DARK));
        darkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                themePrefs.edit().putString("theme", isChecked ? THEME_DARK : THEME_LIGHT).apply();
                recreate();
            }
        });
        setCurrentProgram();

    }

    /**
     * Changes saved academic program
     */
    private void setCurrentProgram() {
        final String program_text = "Academic Program : \n";
        currentProgram.setText(program_text.concat(pref.getString("program", "N/A")));
        changeProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder programs_builder = new AlertDialog.Builder(SettingsActivity.this);
                programs_builder.setCancelable(true);
                programs_builder.setTitle("Choose your program");
                final String[] categories = {"B.Tech", "BA Communication", "MA Communication", "Integrated MSc & MA", "MCA", "MSW", "M.Tech"};
                final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_list_item_1, categories);
                programs_builder.setItems(categories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("remember_program", true);
                        editor.putInt("pos", which);
                        editor.putString("program", dataAdapter.getItem(which));
                        editor.apply();
                        currentProgram.setText(program_text.concat(pref.getString("program", "N/A")));
                    }
                });
                programs_builder.show();
            }
        });

    }
}
