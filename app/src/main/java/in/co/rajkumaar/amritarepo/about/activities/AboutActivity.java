/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.about.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

import java.util.Objects;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.activities.SupportActivity;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class AboutActivity extends BaseActivity {


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AboutView view = AboutBuilder.with(this)
                .setAppIcon(R.drawable.logosq)
                .setAppName(R.string.app_name)
                .setPhoto(R.drawable.raj)
                .setCover(R.mipmap.profile_cover)
                .setLinksAnimated(true)
                .setDividerDashGap(13)
                .setName("Rajkumar S")
                .setSubTitle("Full Stack & Mobile App Developer")
                .setLinksColumnsCount(3)
                .setBrief("Ever in awe of the wonders that 0s and 1s could create.")
                .addGitHubLink("rajkumaar23")
                .addFacebookLink("rajkumaar23")
                .addInstagramLink("rajkumaar23")
                .addLinkedInLink("in/rajkumaar23")
                .addEmailLink("rajkumaar2304@icloud.com")
                .addWebsiteLink("http://rajkumaar.co.in")
                .setVersionNameAsAppSubTitle()
                .setActionsColumnsCount(2)
                .addAction(BitmapFactory.decodeResource(getResources(), R.drawable.github), "Contributors", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(AboutActivity.this, ContributorsActivity.class));
                    }
                })
                .addAction(BitmapFactory.decodeResource(getResources(), R.drawable.disclaimer), "Disclaimer", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDisclaimer(AboutActivity.this);
                    }
                })
                .addAction(BitmapFactory.decodeResource(getResources(), R.drawable.donate), "Donate", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        donate();
                    }
                })
                .addShareAction(R.string.app_name)
                .setWrapScrollView(true)
                .setShowAsCard(true)
                .build();
        final FrameLayout frameLayout = findViewById(R.id.holder);
        frameLayout.addView(view);
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    public void donate() {
        startActivity(new Intent(this, SupportActivity.class));
    }

}
