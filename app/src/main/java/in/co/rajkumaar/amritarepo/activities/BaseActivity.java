/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import in.co.rajkumaar.amritarepo.R;

import static in.co.rajkumaar.amritarepo.helpers.Utils.THEME_DARK;
import static in.co.rajkumaar.amritarepo.helpers.Utils.THEME_LIGHT;

public class BaseActivity extends AppCompatActivity {
    public SharedPreferences themePrefs;
    private String currentTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themePrefs = getSharedPreferences("theming", MODE_PRIVATE);
        currentTheme = themePrefs.getString("theme", THEME_LIGHT);
        setAppTheme(currentTheme);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String selectedTheme = themePrefs.getString("theme", THEME_LIGHT);
        if (!selectedTheme.equals(currentTheme)) {
            recreate();
        }
    }

    public void setAppTheme(String currentTheme) {
        if (this.getClass().getName().equals(LaunchingActivity.class.getName())) {
            if (currentTheme.equals(THEME_DARK)) {
                setTheme(R.style.Theme_App_NoActionBar_Dark);
            } else {
                setTheme(R.style.Theme_App_NoActionBar_Light);
            }
        } else {
            if (currentTheme.equals(THEME_DARK)) {
                setTheme(R.style.Theme_App_Dark);
            } else {
                setTheme(R.style.Theme_App_Light);
            }
        }
    }
}
