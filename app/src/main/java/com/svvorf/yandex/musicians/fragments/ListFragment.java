package com.svvorf.yandex.musicians.fragments;


import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.adapters.MusiciansAdapter;
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
 * A fragment for displaying the list of musicians.
 * The functionality includes:
 * - loading from the remote API on the first launch, then caching a response
 * - updating (fetching fresh response from the API) using swipe to refresh
 * - searching results
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
    private LinearLayoutManager mMusiciansListLayoutManager;

    private OnMusicianSelectedListener mCallback;

    private boolean mIsTablet;

    private int mSelectedPosition;

    private RequestManager mRequestManager;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getting database and stored data
        mRealm = Realm.getDefaultInstance();
        mMusicians = mRealm.where(Musician.class).findAllSorted("id");

        mRequestManager = RequestManager.getInstance();

        setHasOptionsMenu(true);
        mIsTablet = getResources().getBoolean(R.bool.is_tablet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);
        setupRefreshLayout();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // if there is no stored musicians in the database (e.g. a first launch),
        // then we start fetching data from the remote API
        if (mMusicians.size() == 0) {
            if (Utils.isNetworkConnected(getActivity())) {
                // the following is a workaround to make SwipeRefreshLayout show loading animation right away.
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                loadMusicians();
            } else {
                //TODO show no internet layout
            }

        } else { //use cached data from database
            finishLoadingData();
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

            //TODO show error layout
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final ApiResponse apiResponse = mRequestManager.getGson().fromJson(response.body().charStream(), ApiResponse.class);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //save the response to the database
                    mRealm.beginTransaction();
                    mRealm.copyToRealmOrUpdate(apiResponse.getMusicians());
                    mRealm.commitTransaction();

                    swipeRefreshLayout.setRefreshing(false);

                    finishLoadingData();
                }
            });


        }
    };

    /**
     * Creates adapter with data from the database and shows list.
     */
    private void finishLoadingData() {
        createAdapter();

        toggleListVisibility(true);
        //mListAdapter.notifyDataSetChanged();

        if (mIsTablet) {
            //load first musician
            mCallback.onMusicianSelected(mMusicians.get(0).getId());
        }
    }

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
        SearchView searc = (SearchView) MenuItemCompat.getActionView(searchItem);
        searc.setOnQueryTextListener(this);
        searc.setQueryHint(getString(R.string.action_search));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mIsTablet) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setHomeButtonEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
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
