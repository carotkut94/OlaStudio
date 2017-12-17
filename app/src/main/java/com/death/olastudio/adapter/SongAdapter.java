package com.death.olastudio.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.death.olastudio.R;
import com.death.olastudio.model.Song;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.List;
import android.widget.Filter;

import java.util.ArrayList;


/**
 * Created by deathcode on 16/12/17.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> implements Filterable {

    private List<Song> songs;
    private List<Song> filteredSongs;
    private Context mContext;

    public SongAdapter(Context context, List<Song> songs) {
        mContext = context;
        this.songs = songs;
        this.filteredSongs = songs;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final Song song = filteredSongs.get(position);
        holder.song_title.setText(song.getSong());
        holder.song_author.setText(song.getArtists());
        Log.e("Song", song.getCoverImage());
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(new OkHttpDownloader(mContext));
        builder.build()
                .load(song.getCoverImage())
                .placeholder(R.drawable.app_icon)
                .into(holder.cover_image);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredSongs = songs;
                } else {
                    List<Song> filteredList = new ArrayList<>();
                    for (Song song : songs) {

                        if (song.getSong().toLowerCase().contains(charString.toLowerCase()) || song.getArtists().contains(charSequence)) {
                            filteredList.add(song);
                        }
                    }

                    filteredSongs = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredSongs;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredSongs = (ArrayList<Song>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public Song getItem(int position) {
        return filteredSongs.get(position);
    }

    @Override
    public int getItemCount() {
        return filteredSongs.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView song_title;
        TextView song_author;
        ImageView cover_image;

        MyViewHolder(View itemView) {
            super(itemView);
            song_title = itemView.findViewById(R.id.song_name);
            song_author  = itemView.findViewById(R.id.song_author);
            cover_image = itemView.findViewById(R.id.cover_image);
        }
    }

    public interface ClickListener{
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private SongAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final SongAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }


        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
