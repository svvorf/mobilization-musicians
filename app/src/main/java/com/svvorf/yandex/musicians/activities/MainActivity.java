package com.svvorf.yandex.musicians.activities;

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

public class MainActivity extends AppCompatActivity implements ListFragment.OnMusicianSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    /*
    Handset fragment
     */
    private Fragment mCurrentFragment;

    /*
    Tablet fragments
     */
    private ListFragment mListFragment;
    private MusicianFragment mMusicianFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            if (savedInstanceState != null) {
                String tag = savedInstanceState.getString("fragmentTag");
                mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, tag);
            } else {
                mCurrentFragment = new ListFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mCurrentFragment, mCurrentFragment.getClass().getSimpleName()).commit();
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
            mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            String tag = mCurrentFragment.getClass().getSimpleName();
            outState.putString("fragmentTag", tag);
            getSupportFragmentManager().putFragment(outState, mCurrentFragment.getClass().getSimpleName(), mCurrentFragment);
        }
    }

    @Override
    public void onMusicianSelected(int musicianId) {
        if (getResources().getBoolean(R.bool.is_tablet)) {
            mMusicianFragment.setMusician(musicianId);
        } else {
            mCurrentFragment = MusicianFragment.newInstance(musicianId);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mCurrentFragment, mCurrentFragment.getClass().getSimpleName()).addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!getResources().getBoolean(R.bool.is_tablet))
                    getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
