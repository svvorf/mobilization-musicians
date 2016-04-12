package com.svvorf.yandex.musicians.fragments;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.adapters.MusiciansAdapter;
import com.svvorf.yandex.musicians.misc.AnimationManager;
import com.svvorf.yandex.musicians.misc.ItemDecorations;
import com.svvorf.yandex.musicians.models.ApiResponse;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.network.RequestManager;
import com.svvorf.yandex.musicians.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A fragment for displaying list of musicians
 */
public class ListFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static final String MUSICIANS_LIST_INSTANCE_STATE = "musiciansListInstanceState";
    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.musicians_list)
    RecyclerView musiciansList;


    private Realm mRealm;

    private RealmResults<Musician> mMusicians;

    private MusiciansAdapter mListAdapter;
    private RequestManager mRequestManager;
    private LinearLayoutManager mMusiciansListLayoutManager;

    private OnMusicianSelectedListener mCallback;

    private boolean mIsTablet;
    private int mSelectedPosition;
    private SearchView mSearchView;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        mMusicians = mRealm.where(Musician.class).findAllSorted("id");
        mRequestManager = RequestManager.getInstance();
        setHasOptionsMenu(true);
        mIsTablet = getResources().getBoolean(R.bool.is_tablet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, null);
        ButterKnife.bind(this, rootView);
        setupRefreshLayout();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mMusicians.size() == 0 && Utils.isNetworkConnected(getActivity())) { //Make request to the API if there is no cache
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            loadMusicians();

        } else { //use cached data from database
            toggleListVisibility(true);
            if (mIsTablet) {
                //load first musician
                mCallback.onMusicianSelected(mMusicians.get(0).getId());
            }
            createAdapter();
        }

        mMusiciansListLayoutManager = new LinearLayoutManager(getActivity());
        musiciansList.setLayoutManager(mMusiciansListLayoutManager);

        musiciansList.addItemDecoration(new ItemDecorations.DividerItemDecoration(getActivity(), R.drawable.line_divider));
    }

    private void createAdapter() {
        mListAdapter = new MusiciansAdapter(getActivity(), mMusicians, mCallback, mSelectedPosition);
        musiciansList.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMusicians();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    }

    private Callback loadMusiciansCallback = new Callback() {

        @Override
        public void onFailure(Call call, IOException e) {
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final ApiResponse apiResponse = mRequestManager.getGson().fromJson(response.body().charStream(), ApiResponse.class);

            ListFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRealm.beginTransaction();
                    mRealm.copyToRealmOrUpdate(apiResponse.getMusicians());
                    mRealm.commitTransaction();

                    createAdapter();

                    toggleListVisibility(true);
                    mListAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                    if (mIsTablet) {
                        //load first musician
                        mCallback.onMusicianSelected(mMusicians.get(0).getId());
                    }
                }
            });


        }
    };

    private void toggleListVisibility(boolean show) {
        musiciansList.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void loadMusicians() {
        mRequestManager.loadMusicians(loadMusiciansCallback);

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //Restoring scrolling position after recreation (orientation change)
        if (savedInstanceState != null) {
            mMusiciansListLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(MUSICIANS_LIST_INSTANCE_STATE));
            mSelectedPosition = savedInstanceState.getInt("selectedPosition");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMusiciansListLayoutManager != null)
            outState.putParcelable(MUSICIANS_LIST_INSTANCE_STATE, mMusiciansListLayoutManager.onSaveInstanceState());
        outState.putInt("selectedPosition", mListAdapter.getSelectedPosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.action_search));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mIsTablet) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
           // Toolbar toolbar = ButterKnife.findById(getActivity(), R.id.toolbar);

            //AnimationManager.fadeDrawable(toolbar.getBackground(), AnimationManager.FadeDirection.OPAQUE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (OnMusicianSelectedListener) getActivity();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Musician> filteredModelList = filterMusicians(mMusicians, newText);
        mListAdapter.animateTo(filteredModelList);
        musiciansList.scrollToPosition(0);
        return true;
    }

    private List<Musician> filterMusicians(List<Musician> models, String query) {
        query = query.toLowerCase();

        final List<Musician> filteredModelList = new ArrayList<>();
        for (Musician model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    public interface OnMusicianSelectedListener {
        void onMusicianSelected(int id);
    }
}
