package com.svvorf.yandex.musicians.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.MusicianFragment;

import butterknife.ButterKnife;

/**
 * The activity is used to hold MusicianFragment and is called when a user selects a musician on handsets.
 * The activity has a different toolbar layout relative to MainActivity (collapsing, contains cover ImageView and title)
 */
public class MusicianActivity extends AppCompatActivity {

    private boolean searchOpened = false;
    private MusicianFragment musicianFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musician);

        setSupportActionBar((Toolbar) ButterKnife.findById(this, R.id.toolbar));

        int musicianId = getIntent().getIntExtra("id", 0);
        musicianFragment = MusicianFragment.newInstance(musicianId);
        getSupportFragmentManager().beginTransaction().add(R.id.container, musicianFragment, MusicianFragment.class.getSimpleName()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        applyTransition();
    }

    /**
     * The activity closing animation (slide) direction depends on the device orientation.
     */
    private void applyTransition() {
        overridePendingTransition(R.anim.no_anim, getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? R.anim.exit_to_right : R.anim.exit_to_bottom);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!getResources().getBoolean(R.bool.is_tablet)) {
                    finish();
                    applyTransition();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
