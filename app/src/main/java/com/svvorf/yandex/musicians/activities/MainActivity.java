package com.svvorf.yandex.musicians.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            String tag = savedInstanceState.getString("fragmentTag");
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, tag);
        } else {
            mCurrentFragment = new ListFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mCurrentFragment, mCurrentFragment.getClass().getSimpleName()).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        String tag = mCurrentFragment.getClass().getSimpleName();
        outState.putString("fragmentTag", tag);
        getSupportFragmentManager().putFragment(outState, mCurrentFragment.getClass().getSimpleName(), mCurrentFragment);
    }

    @Override
    public void onMusicianSelected(int musicianId) {
        mCurrentFragment = MusicianFragment.newInstance(musicianId);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mCurrentFragment, mCurrentFragment.getClass().getSimpleName()).addToBackStack(null).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
