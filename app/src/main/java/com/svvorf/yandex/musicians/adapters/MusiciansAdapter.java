package com.svvorf.yandex.musicians.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.svvorf.yandex.musicians.R;
import com.svvorf.yandex.musicians.fragments.ListFragment;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.models.RealmString;
import com.svvorf.yandex.musicians.network.RequestManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An adapter for the list of musicians in the {@link ListFragment}
 */
public class MusiciansAdapter extends RecyclerView.Adapter<MusiciansAdapter.ViewHolder> {

    private final Context context;
    private final List<Musician> musicians;

    public MusiciansAdapter(Context context, List<Musician> musicians) {
        this.context = context;
        this.musicians = musicians;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_musicians, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Musician musician = musicians.get(position);
        holder.name.setText(musician.getName());
        holder.statistics.setText(context.getResources().getString(R.string.statistics, musician.getAlbums(), musician.getTracks()));

        StringBuilder genresStringBuilder = new StringBuilder();
        int genresCount = musician.getGenres().size();
        for (int i = 0; i < genresCount; i++) {
            genresStringBuilder.append(musician.getGenres().get(i).getValue());
            if (i != genresCount - 1) {
                genresStringBuilder.append(", ");
            }
        }
        holder.genres.setText(genresStringBuilder.toString());

        RequestManager.getInstance().getPicasso().load(musician.getSmallCover()).placeholder(R.drawable.placeholder).fit().into(holder.smallCover);
    }

    @Override
    public int getItemCount() {
        return musicians.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.small_cover)
        ImageView smallCover;
        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.statistics)
        TextView statistics;
        @Bind(R.id.genres)
        TextView genres;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
