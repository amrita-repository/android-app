package in.co.rajkumaar.amritarepo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import in.co.rajkumaar.amritarepo.R;

public class SettingsActivity extends AppCompatActivity {

    TextView current_program;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button change_program;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        current_program = findViewById(R.id.current_program);
        change_program = findViewById(R.id.change_program);
        pref = getSharedPreferences("user", MODE_PRIVATE);
        editor = pref.edit();
        setCurrentProgram();

    }

    /**
     * Changes saved academic program
     */
    private void setCurrentProgram() {
        final String program_text = "Academic Program : ";
        current_program.setText(program_text.concat(pref.getString("program", "N/A")));
        change_program.setOnClickListener(new View.OnClickListener() {
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
                        editor.putBoolean("remember_program", true);
                        editor.putInt("pos", which);
                        editor.putString("program", dataAdapter.getItem(which));
                        editor.apply();
                        current_program.setText(program_text.concat(pref.getString("program", "N/A")));
                    }
                });
                programs_builder.show();
            }
        });

    }
}
