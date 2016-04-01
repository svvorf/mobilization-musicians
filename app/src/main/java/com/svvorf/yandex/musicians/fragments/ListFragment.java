package com.svvorf.yandex.musicians.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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

    private Realm realm;

    private RealmResults<Musician> musicians;

    private MusiciansAdapter listAdapter;
    private RequestManager requestManager;
    private LinearLayoutManager musiciansListLayoutManager;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        musicians = realm.where(Musician.class).findAllSorted("id");
        requestManager = RequestManager.getInstance();
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

        //Make request to the API if there is no cache
        if (musicians.size() == 0 && Utils.isNetworkConnected(getActivity())) {
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

        musiciansListLayoutManager = new LinearLayoutManager(getActivity());
        musiciansList.setLayoutManager(musiciansListLayoutManager);

        listAdapter = new MusiciansAdapter(getActivity(), musicians);
        musiciansList.setAdapter(listAdapter);
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
            final ApiResponse apiResponse = requestManager.getGson().fromJson(response.body().charStream(), ApiResponse.class);

            ListFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(apiResponse.getMusicians());
                    realm.commitTransaction();

                    toggleListVisibility(true);
                    listAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });


        }
    };

    private void toggleListVisibility(boolean show) {
        musiciansList.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void loadMusicians() {
        requestManager.loadMusicians(loadMusiciansCallback);

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("DbG", "restored");

        //Restoring scrolling position after recreation (orientation change)
        if (savedInstanceState != null) {
            musiciansListLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(MUSICIANS_LIST_INSTANCE_STATE));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MUSICIANS_LIST_INSTANCE_STATE, musiciansListLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
