/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skyfishjy.library.RippleBackground;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;

import java.util.Objects;

import am.util.ftpserver.FTPHelper;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class FTPActivity extends BaseActivity {

    private FtpServer ftpServer;
    private Button start;
    private RippleBackground rippleBackground;
    private TextView result;

    private LinearLayout tools;
    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            if (wifiStateExtra == WifiManager.WIFI_STATE_DISABLED) {
                if (!result.getText().toString().trim().isEmpty()) {
                    ftpServer.suspend();
                    result.setText("");
                    start.setText("Start Server");
                    start.setTextColor(getResources().getColor(android.R.color.black));
                    start.setBackground(getResources().getDrawable(R.drawable.button));
                    rippleBackground.stopRippleAnimation();
                    rippleBackground.setVisibility(View.GONE);
                    Utils.showToast(getApplicationContext(), "WiFi got disconnected :(");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);
        result = findViewById(R.id.result);
        start = findViewById(R.id.start);
        rippleBackground = findViewById(R.id.content);
        Button copy = findViewById(R.id.copy);
        Button share = findViewById(R.id.share);
        tools = findViewById(R.id.tools);


        final String home = getExternalFilesDir(null) + "/" + "AmritaRepo/";
        final int port = 2304;
        ftpServer = FTPHelper.createServer(port, 10, 5000, true, home);
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo info = wifiManager.getConnectionInfo();

        start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                switch (start.getText().toString().toLowerCase()) {
                    case "start server":
                        if (wifiManager.isWifiEnabled()) {
                            if (!Objects.equals(info.getSSID(), "")) {
                                String IP = Formatter.formatIpAddress(info.getIpAddress());
                                if (!IP.contains("0.0.0.0")) {
                                    try {
                                        if (ftpServer.isSuspended()) ftpServer.resume();
                                        else ftpServer.start();
                                        result.setText(Html.fromHtml(
                                                "You can access your files at <br><strong>ftp://" + IP + ":" + port + "</strong>"
                                        ));
                                        start.setText("Stop Server");
                                        rippleBackground.setVisibility(View.VISIBLE);
                                        rippleBackground.startRippleAnimation();
                                        tools.setVisibility(View.VISIBLE);

                                    } catch (FtpException e) {
                                        Utils.showUnexpectedError(getApplicationContext());
                                        e.printStackTrace();
                                    }
                                } else {
                                    Utils.showToast(getApplicationContext(), "There was an error while turning on FTP. Please connect to your hotspot of your computer and retry.");
                                }
                            } else {
                                Utils.showToast(getApplicationContext(), "Please connect to your hotspot.");
                            }
                        } else {
                            Utils.showToast(getApplicationContext(), "Please enable WiFi and connect to your hotspot.");
                        }
                        break;
                    case "stop server":
                        ftpServer.suspend();
                        result.setText("");
                        start.setText("Start Server");
                        rippleBackground.stopRippleAnimation();
                        rippleBackground.setVisibility(View.GONE);
                        tools.setVisibility(View.GONE);
                        break;
                }
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.getText().toString().trim().isEmpty()) {
                    Utils.showToast(getApplicationContext(), "Start the server to get an address");
                    return;
                }
                final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Source Text", result.getText().toString().split("at")[1].trim());
                clipboardManager.setPrimaryClip(clipData);
                Utils.showToast(FTPActivity.this, "Copied to clipboard");
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.getText().toString().trim().isEmpty()) {
                    Utils.showToast(getApplicationContext(), "Start the server to get an address");
                    return;
                }
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, result.getText().toString().split("at")[1].trim());
                sendIntent.setType("text/plain");
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                } else {
                    Utils.showUnexpectedError(FTPActivity.this);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (!result.getText().toString().trim().isEmpty()) {
            ftpServer.stop();
            Utils.showToast(getApplicationContext(), "File sharing has been stopped.");
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }
}
