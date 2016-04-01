package com.svvorf.yandex.musicians.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.ListFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Fragment mListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mListFragment = null;
        if (savedInstanceState != null) {
            mListFragment = getSupportFragmentManager().getFragment(savedInstanceState, "list_fragment");
        } else {
            mListFragment = new ListFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mListFragment, "list_fragment").commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "list_fragment", mListFragment);
    }
}
