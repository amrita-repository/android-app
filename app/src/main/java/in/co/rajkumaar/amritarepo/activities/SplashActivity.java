package in.co.rajkumaar.amritarepo.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.VideoView;

import in.co.rajkumaar.amritarepo.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            try {
                String uri = "android.resource://" + getPackageName() + "/" + R.raw.intro;
                VideoView mVideoView = findViewById(R.id.videoView);
                mVideoView.setVideoURI(Uri.parse(uri));
                mVideoView.requestFocus();
                mVideoView.start();
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        loadHome();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                loadHome();
            }
        } else{
            loadHome();
        }
    }

    private void loadHome() {
        startActivity(new Intent(this, LaunchingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}