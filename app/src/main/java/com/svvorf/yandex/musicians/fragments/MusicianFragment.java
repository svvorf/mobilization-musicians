package com.svvorf.yandex.musicians.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.models.RealmString;
import com.svvorf.yandex.musicians.network.RequestManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianFragment extends Fragment {

    private Realm mRealm;
    private Musician mMusician;

    @Bind(R.id.name)
    TextView name;
    @Bind(R.id.description)
    TextView description;
    @Bind(R.id.statistics)
    TextView statistics;
    @Bind(R.id.genres_container)
    LinearLayout genresContainer;

    @Bind(R.id.big_cover)
    ImageView cover;
    @Bind(R.id.cover_progress)
    ProgressBar coverProgress;


    public MusicianFragment() {
        // Required empty public constructor
    }

    public static MusicianFragment newInstance(int musicianId) {

        Bundle args = new Bundle();
        args.putInt("id", musicianId);
        MusicianFragment fragment = new MusicianFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int musicianId = getArguments().getInt("id");
        mRealm = Realm.getDefaultInstance();
        mMusician = mRealm.where(Musician.class).equalTo("id", musicianId).findFirst();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musician, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name.setText(mMusician.getName());
        description.setText(mMusician.getDescription());
        statistics.setText(getResources().getString(R.string.statistics, mMusician.getAlbums(), mMusician.getTracks()));
        createGenresChips();

        RequestManager.getInstance().getPicasso().load(mMusician.getBigCover()).fit().centerCrop().into(cover, new Callback() {
            @Override
            public void onSuccess() {
                coverProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                coverProgress.setVisibility(View.GONE);
            }
        });
    }

    private void createGenresChips() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (RealmString genreString : mMusician.getGenres()) {
            TextView genre = (TextView) inflater.inflate(R.layout.genre_chip, genresContainer, false);
            genre.setBackgroundResource(R.drawable.chip);
            genre.setText(genreString.getValue());

            genresContainer.addView(genre);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
        RequestManager.getInstance().getPicasso().cancelRequest(cover);
    }
}
