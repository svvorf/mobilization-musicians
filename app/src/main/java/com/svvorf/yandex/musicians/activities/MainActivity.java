package com.svvorf.yandex.musicians.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.ListFragment;
import com.svvorf.yandex.musicians.fragments.MusicianFragment;

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
    private boolean musicianFragmentRetained = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mListFragment = (savedInstanceState != null)
                ? (ListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "listFragment")
                : new ListFragment();

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mListFragment, mListFragment.getClass().getSimpleName()).commit();
        } else {
            //MusicianFragment is set to retain its instance
            mMusicianFragment = (MusicianFragment) getSupportFragmentManager().findFragmentById(R.id.musician_fragment);
            if (mMusicianFragment == null) {
                mMusicianFragment = MusicianFragment.newInstance(0);
                getSupportFragmentManager().beginTransaction().replace(R.id.musician_fragment, mMusicianFragment, MusicianFragment.class.getSimpleName()).commit();
            } else {
                musicianFragmentRetained = true;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.list_fragment, mListFragment, ListFragment.class.getSimpleName()).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "listFragment", mListFragment);

    }

    @Override
    public void onBackPressed() {
        // Pressing back button closes search
        if (!getResources().getBoolean(R.bool.is_tablet) && !mListFragment.getSearchView().isIconified()) {
            mListFragment.getSearchView().setIconified(true);
        } else {
            super.onBackPressed();
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

    @Override
    public void loadInitialMusician() {
        if (!musicianFragmentRetained)
            mMusicianFragment.setMusician(0);
    }

}
