package com.svvorf.yandex.musicians.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * The fragment for displaying musician info
 */
public class MusicianFragment extends Fragment {

    private Realm mRealm;


    private Musician mMusician;

    @Bind(R.id.description)
    TextView description;
    @Bind(R.id.statistics)
    TextView statistics;
    @Bind(R.id.genres_container)
    LinearLayout genresContainer;
    @Bind(R.id.link)
    TextView link;
    @Bind(R.id.musician_name_and_cover)
    ViewGroup nameAndCoverLayout;

    ImageView cover;
    ProgressBar coverProgress;
    private ViewGroup rootView;

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

        int musicianId = 0;
        if (getArguments() != null)
            musicianId = getArguments().getInt("id");

        mRealm = Realm.getDefaultInstance();
        mMusician = mRealm.where(Musician.class).equalTo("id", musicianId).findFirst();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_musician, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup viewGroup;
        // The layout for handsets and for tablets are different: on handsets the name and the cover
        // views are in the toolbar. On the tablets they are in the main layout
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            //if the device is a handset, then we should seek views in the CollapsingToolbarLayout
            viewGroup = ButterKnife.findById(getActivity(), R.id.collapsing_toolbar);
            // also remove those views from the fragment where they defined via xml by default
            nameAndCoverLayout.removeAllViews();
        } else {
            //on tablets, the views are in the fragment
            viewGroup = rootView;
        }

        cover = ButterKnife.findById(viewGroup, R.id.big_cover);
        coverProgress = ButterKnife.findById(viewGroup, R.id.cover_progress);

        if (mMusician != null)
            fillViews();
    }

    /**
     * The method fills views with musician info and start loading cover image
     */
    private void fillViews() {
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            ((CollapsingToolbarLayout) ButterKnife.findById(getActivity(), R.id.collapsing_toolbar)).setTitle(mMusician.getName());
        } else {
            ((TextView) ButterKnife.findById(rootView, R.id.name)).setText(mMusician.getName());
        }
        description.setText(mMusician.getDescription());
        statistics.setText(getResources().getString(R.string.statistics, mMusician.getAlbums(), mMusician.getTracks()));
        link.setText(mMusician.getLink());

        createGenresChips();

        coverProgress.setVisibility(View.VISIBLE);
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

    /**
     * Genres are respresented in chips, the method creates them.
     */
    private void createGenresChips() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        genresContainer.removeAllViews();
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
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
        RequestManager.getInstance().getPicasso().cancelRequest(cover);
    }

    /**
     * The method in fact is used in the tablet two-pane layout: a user selects new musician,
     * then the views are updated in the existing MusicianFragment instance
     *
     * @param musicianId the id of the musician
     */
    public void setMusician(int musicianId) {
        mMusician = mRealm.where(Musician.class).equalTo("id", musicianId).findFirst();
        if (mMusician != null)
            fillViews();
    }
}
