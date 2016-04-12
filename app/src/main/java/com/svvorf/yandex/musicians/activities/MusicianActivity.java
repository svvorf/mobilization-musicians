package com.svvorf.yandex.musicians.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.MusicianFragment;

public class MusicianActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musician);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int musicianId = getIntent().getIntExtra("id", 0);
        getSupportFragmentManager().beginTransaction().add(R.id.container, MusicianFragment.newInstance(musicianId), MusicianFragment.class.getSimpleName()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        commenceTransition();
    }

    private void commenceTransition() {
        overridePendingTransition(R.anim.no_anim, getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? R.anim.exit_to_right : R.anim.exit_to_bottom);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!getResources().getBoolean(R.bool.is_tablet)) {
                    finish();
                    commenceTransition();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
