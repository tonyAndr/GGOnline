package online.gameguides.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.LinearLayout;

import online.gameguides.App;
import online.gameguides.BuildConfig;
import online.gameguides.R;
import online.gameguides.notifications.NotifyService;

public class LaunchActivity extends AppCompatActivity {

    private long SPLASH_SCREEN_DELAY = 1800;
    private LinearLayout logo;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = getSharedPreferences(App.PREFS_NAME, MODE_PRIVATE);

        if (mPrefs.getBoolean(App.PREFS_FIRST_LAUNCH, true)) {
                mPrefs.edit().putBoolean(App.PREFS_DATA_SAVE, true).apply();
                mPrefs.edit().putBoolean(App.KEY_NIGHTMODE, true).apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            mPrefs.edit().putBoolean(App.PREFS_FIRST_LAUNCH, false).apply();
        }

        if (mPrefs.getBoolean(App.KEY_NIGHTMODE, true))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.ActivityThemeDark);
        else
            setTheme(R.style.ActivityThemeLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        logo = (LinearLayout) findViewById(R.id.launch_logo);
        logo.setAlpha(0f);
        logo.setVisibility(View.VISIBLE);
        logo.animate().setDuration(1000).alpha(1f).setStartDelay(500).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startNextActivity();
                finish();
            }
        }, SPLASH_SCREEN_DELAY);

    }

    private void startNextActivity() {
//        if (BuildConfig.FLAVOR.contains("UNDERTALE")) {
//            startActivity(new Intent(LaunchActivity.this, OopsActivity.class));
//        } else {
            startActivity(new Intent(LaunchActivity.this, MainActivity.class));
//        }
    }

    private void updateNotificationTime() {
        startService(new Intent(this, NotifyService.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mPrefs.getBoolean(App.PREFS_NOTIFICATIONS_ENABLED, true))
            updateNotificationTime();
    }

}
