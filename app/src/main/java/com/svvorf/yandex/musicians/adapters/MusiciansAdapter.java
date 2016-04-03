package com.svvorf.yandex.musicians.adapters;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
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

import java.util.ArrayList;
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

    private int mSelectedPosition;
    private boolean shouldSelectItems;

    public MusiciansAdapter(Activity context, List<Musician> musicians, ListFragment.OnMusicianSelectedListener callback, int selectedPosition) {
        this.mContext = context;
        this.mMusicians = new ArrayList<>(musicians);
        mCallback = callback;
        mSelectedPosition = selectedPosition;

        shouldSelectItems = context.getResources().getBoolean(R.bool.is_tablet);
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

        if (shouldSelectItems) {
            if (mSelectedPosition == position) {
                holder.itemView.setBackgroundResource(R.color.selected);
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMusicians.size();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
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
                    int position = getAdapterPosition();
                    int id = mMusicians.get(position).getId();
                    mCallback.onMusicianSelected(id);

                    if (shouldSelectItems) {
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = position;
                        notifyItemChanged(position);

                    }
                }
            });
        }

    }



    public Musician removeItem(int position) {
        final Musician model = mMusicians.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Musician model) {
        mMusicians.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Musician model = mMusicians.remove(fromPosition);
        mMusicians.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<Musician> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Musician> newModels) {
        for (int i = mMusicians.size() - 1; i >= 0; i--) {
            final Musician model = mMusicians.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Musician> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Musician model = newModels.get(i);
            if (!mMusicians.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Musician> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Musician model = newModels.get(toPosition);
            final int fromPosition = mMusicians.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}
