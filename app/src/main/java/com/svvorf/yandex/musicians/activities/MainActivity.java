package com.svvorf.yandex.musicians.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.ListFragment;
import com.svvorf.yandex.musicians.fragments.MusicianFragment;
import com.svvorf.yandex.musicians.models.Musician;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * The main activity for holding fragments.
 * Handsets: only ListFragment is shown, MusicianActivity is being called when a musician is selected.
 * Tablets: both ListFragment and MusicianFragment are shown with two-pane layout.
 */
public class MainActivity extends AppCompatActivity implements ListFragment.OnMusicianSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private ListFragment mListFragment;

    // Only on tablets
    private MusicianFragment mMusicianFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            mListFragment = (savedInstanceState != null)
                    ? (ListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "savedFragment")
                    : new ListFragment();

            getSupportFragmentManager().beginTransaction().replace(R.id.container, mListFragment, mListFragment.getClass().getSimpleName()).commit();
        } else {
            mListFragment = new ListFragment();
            mMusicianFragment = new MusicianFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.musician_fragment, mMusicianFragment, mMusicianFragment.getClass().getSimpleName()).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.list_fragment, mListFragment, mListFragment.getClass().getSimpleName()).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            getSupportFragmentManager().putFragment(outState, "savedFragment", mListFragment);
        }
    }

    @Override
    public void onMusicianSelected(int musicianId) {
        if (getResources().getBoolean(R.bool.is_tablet)) {
            mMusicianFragment.setMusician(musicianId);
        } else {
            Intent musicianIntent = new Intent(this, MusicianActivity.class);
            musicianIntent.putExtra("id", musicianId);
            startActivity(musicianIntent);

            overridePendingTransition(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? R.anim.enter_from_right : R.anim.enter_from_bottom, R.anim.no_anim);
        }
    }

}
