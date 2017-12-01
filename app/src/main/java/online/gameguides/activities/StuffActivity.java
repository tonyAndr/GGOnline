package online.gameguides.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import online.gameguides.App;
import online.gameguides.R;

public class StuffActivity extends AppCompatActivity {

    private int mLayoutId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.ActivityThemeDark);
        else
            setTheme(R.style.ActivityThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stuff);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            mLayoutId = savedInstanceState.getInt("layoutId");
        } else {
            mLayoutId = getIntent().getIntExtra("layoutId", R.layout.frag_about);
        }

        setHeader();

        StuffFragment stuffFragment = new StuffFragment();
        Bundle b = new Bundle();
        b.putInt("layoutId", mLayoutId);
        stuffFragment.setArguments(b);

        getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.stuff_container, stuffFragment).commit();
    }

    private void setHeader () {
        switch (mLayoutId) {
            case R.layout.frag_about:
                setTitle(getString(R.string.menu_about));
                break;
            case R.layout.frag_faq:
                setTitle(getString(R.string.menu_faq));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("layoutId", mLayoutId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class StuffFragment extends Fragment {
        public StuffFragment () {

        }

        private int mLayoutId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mLayoutId = getArguments().getInt("layoutId");
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(this.mLayoutId, container, false);


            return v;
        }


    }
}
