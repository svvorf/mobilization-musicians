package com.svvorf.yandex.musicians.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
public class ListFragment extends Fragment {

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
        }

        mMusiciansListLayoutManager = new LinearLayoutManager(getActivity());
        musiciansList.setLayoutManager(mMusiciansListLayoutManager);

        mListAdapter = new MusiciansAdapter(getActivity(), mMusicians, mCallback);
        musiciansList.setAdapter(mListAdapter);
        musiciansList.addItemDecoration(new ItemDecorations.DividerItemDecoration(getActivity(), R.drawable.line_divider));

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

                    toggleListVisibility(true);
                    mListAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
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
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMusiciansListLayoutManager != null) outState.putParcelable(MUSICIANS_LIST_INSTANCE_STATE, mMusiciansListLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        mCallback = (OnMusicianSelectedListener) getActivity();
    }

    public interface OnMusicianSelectedListener {
        public void onMusicianSelected(int id);
    }
}
