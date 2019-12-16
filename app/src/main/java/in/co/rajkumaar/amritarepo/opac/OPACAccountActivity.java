/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.loopj.android.http.AsyncHttpClient;

import in.co.rajkumaar.amritarepo.R;

public class OPACAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opacaccount);
        System.out.println(getIntent().getStringExtra("id"));
        String domain = getString(R.string.lib_catalog_domain);
        init(domain);
    }

    void init(String domain){
        AsyncHttpClient client = new AsyncHttpClient();
    }
}
