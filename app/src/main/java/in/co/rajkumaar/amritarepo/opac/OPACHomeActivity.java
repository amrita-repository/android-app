/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.os.Bundle;
import android.widget.GridView;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;

public class OPACHomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opachome);
        GridView gridView = findViewById(R.id.about_grid);
        ArrayList<OPACHomeItem> items = new ArrayList<>();
        items.add(new OPACHomeItem("#FF201B","Search Books", FontAwesomeIcons.fa_search));
        items.add(new OPACHomeItem("#03a9f4","Account", FontAwesomeIcons.fa_user));
        items.add(new OPACHomeItem("#ffc107","E-Resources", FontAwesomeIcons.fa_database));
        items.add(new OPACHomeItem("#259b24","Useful Sites", FontAwesomeIcons.fa_internet_explorer));
        gridView.setAdapter(new OPACAdapter(this,items));
    }
}
