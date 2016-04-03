package com.svvorf.yandex.musicians.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.ListFragment;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.network.RequestManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An adapter for the list of musicians in the {@link ListFragment}
 */
public class MusiciansAdapter extends RecyclerView.Adapter<MusiciansAdapter.ViewHolder> {

    private final Activity mContext;
    private final List<Musician> mMusicians;
    private ListFragment.OnMusicianSelectedListener mCallback;

    public MusiciansAdapter(Activity context, List<Musician> musicians, ListFragment.OnMusicianSelectedListener callback) {
        this.mContext = context;
        this.mMusicians = musicians;
        mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_musicians, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Musician musician = mMusicians.get(position);
        holder.name.setText(musician.getName());
        holder.statistics.setText(mContext.getResources().getString(R.string.statistics, musician.getAlbums(), musician.getTracks()));

        StringBuilder genresStringBuilder = new StringBuilder();
        int genresCount = musician.getGenres().size();
        for (int i = 0; i < genresCount; i++) {
            genresStringBuilder.append(musician.getGenres().get(i).getValue());
            if (i != genresCount - 1) {
                genresStringBuilder.append(", ");
            }
        }
        holder.genres.setText(genresStringBuilder.toString());

        holder.coverProgress.setVisibility(View.VISIBLE);
        RequestManager.getInstance().getPicasso().load(musician.getSmallCover()).into(holder.smallCover, new Callback() {
            @Override
            public void onSuccess() {
                holder.coverProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMusicians.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.small_cover)
        ImageView smallCover;
        @Bind(R.id.cover_progress)
        ProgressBar coverProgress;

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.statistics)
        TextView statistics;
        @Bind(R.id.genres)
        TextView genres;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = mMusicians.get(getAdapterPosition()).getId();
                    mCallback.onMusicianSelected(id);
                }
            });
        }

    }
}
