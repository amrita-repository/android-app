/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.about;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Objects;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.SupportActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Utils.showBigAd(this, (com.google.android.gms.ads.AdView) findViewById(R.id.banner_container));

        ImageView insta,fb,linkedin,github,website;
        insta = findViewById(R.id.insta);
        fb = findViewById(R.id.fb);
        linkedin = findViewById(R.id.linkedin);
        github = findViewById(R.id.github);
        website = findViewById(R.id.site);

        insta.setImageDrawable(new IconDrawable(this,FontAwesomeIcons.fa_instagram).color(Color.parseColor("#ffffff")));
        fb.setImageDrawable(new IconDrawable(this,FontAwesomeIcons.fa_facebook_square).color(Color.parseColor("#ffffff")));
        linkedin.setImageDrawable(new IconDrawable(this,FontAwesomeIcons.fa_linkedin).color(Color.parseColor("#ffffff")));
        github.setImageDrawable(new IconDrawable(this,FontAwesomeIcons.fa_github).color(Color.parseColor("#ffffff")));
        website.setImageDrawable(new IconDrawable(this,FontAwesomeIcons.fa_user).color(Color.parseColor("#ffffff")));

        insta.setOnClickListener(this);
        fb.setOnClickListener(this);
        linkedin.setOnClickListener(this);
        github.setOnClickListener(this);
        website.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Button disclaimer = findViewById(R.id.disclaimer);
        disclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDisclaimer(AboutActivity.this);
            }
        });
        ((TextView) findViewById(R.id.name)).setText(Html.fromHtml("Crafted with &hearts; by <br><strong>RAJKUMAR</strong>"));
    }

    public static void showDisclaimer(Context context) {
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.disclaimer);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                TextView textView = dialog.findViewById(R.id.text);
                textView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
            }
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    public void ossLicenses(View view) {
        Intent intent = new Intent(this, OssLicensesMenuActivity.class);
        startActivity(intent);
    }

    public void donate(View view) {
        startActivity(new Intent(this, SupportActivity.class));
    }

    @Override
    public void onClick(View v) {
        Bundle params = new Bundle();
        String url = "";
        params.putString("IMAGE_CLICK", "About");
        mFirebaseAnalytics.logEvent("IMAGE_CLICK", params);
        switch (v.getId()){
            case R.id.insta : {
                url = "https://instagram.com/rajkumaar23"; break;
            }
            case R.id.fb : {
                url = "https://facebook.com/rajkumaar23"; break;
            }
            case R.id.github : {
                url = "https://github.com/rajkumaar23"; break;
            }
            case R.id.linkedin : {
                url = "https://linkedin.com/in/rajkumaar23"; break;
            }
            case R.id.site : {
                url = "http://rajkumaar.co.in"; break;
            }
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openWebsite(View view) {
        Bundle params = new Bundle();
        String url = "http://rajkumaar.co.in";
        params.putString("IMAGE_CLICK", "About");
        mFirebaseAnalytics.logEvent("IMAGE_CLICK", params);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
