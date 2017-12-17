package com.death.olastudio;
import com.airbnb.lottie.LottieAnimationView;
import com.death.olastudio.adapter.SongAdapter;
import com.death.olastudio.model.Song;
import com.death.olastudio.network.*;
import com.death.olastudio.utils.StItemDecorator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ohoussein.playpause.PlayPauseView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.Logger;
import dm.audiostreamer.MediaMetaData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicActivity extends AppCompatActivity implements CurrentSessionCallback, SeekBar.OnSeekBarChangeListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener, InternetConnectionDetector.ConnectivityReceiverListener {

    StarlordApi apiService;
    RecyclerView recyclerView_music_list;
    SongAdapter songAdapter;
    List<Song> songs;
    MediaMetaData mediaMetaData;
    private SlidingUpPanelLayout mSlidingLayout;
    private RelativeLayout slideBottomView;
    private boolean isExpand = false;
    private Context context;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private AudioStreamingManager streamingManager;
    private MediaMetaData currentSong;
    private PlayPauseView playPauseButton;
    private TextView songName;
    private TextView stripSongName;
    LottieAnimationView lottieAnimationView;
    private ProgressBar isBuffering;
    private TextView songAuthor;
    private TextView stripSongAuthor;
    private ImageView coverImageView;
    private ImageView backgroundCoverImage;
    private ImageView stripCoverImage;
    private DisplayImageOptions options;
    private RelativeLayout bottomStripView;
    Call<List<Song>> call;

    public static List<MediaMetaData> musicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        this.context = MusicActivity.this;
        Toolbar toolbar = findViewById(R.id.toolbar_container);
        setSupportActionBar(toolbar);

        configAudioStreamer();
        uiInitialization();
        recyclerView_music_list = findViewById(R.id.music_container);
        recyclerView_music_list.addItemDecoration(new StItemDecorator(20));
        recyclerView_music_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        apiService = ApiClient.getClient().create(StarlordApi.class);
        checkAlreadyPlaying();
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseEvent(v);
            }
        });
        if(isOnline())
        {
            loadMusic();
        }else{
            noInternet();
        }
        recyclerView_music_list.addOnItemTouchListener(new SongAdapter.RecyclerTouchListener(context, recyclerView_music_list, new SongAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                showMediaInfo(musicList.get(position));
                playSong(musicList.get(position));
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    public void onBackPressed() {
        if (isExpand) {
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
            overridePendingTransition(0, 0);
            finish();
        }
    }

    private void checkAlreadyPlaying() {
        if (streamingManager.isPlaying()) {
            currentSong = streamingManager.getCurrentAudio();
            if (currentSong != null) {
                currentSong.setPlayState(streamingManager.mLastPlaybackState);
                showMediaInfo(currentSong);
                playPauseButton.toggle();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (streamingManager != null) {
                streamingManager.subscribesCallBack(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance(context);
        streamingManager.setPlayMultiple(false);
        streamingManager.setShowPlayerNotification(true);
    }

    private void uiInitialization() {
        mSlidingLayout = findViewById(R.id.sliding_layout);
        backgroundCoverImage = findViewById(R.id.cover_view);
        stripCoverImage = findViewById(R.id.img_bottom_albArt);
        songName = findViewById(R.id.song_main_screen_name);
        stripSongName = findViewById(R.id.txt_bottom_SongName);
        songAuthor = findViewById(R.id.author_main_screen_name);
        stripSongAuthor = findViewById(R.id.txt_bottom_author);
        coverImageView = findViewById(R.id.cover_image);
        playPauseButton = findViewById(R.id.play_pause_layout);
        lottieAnimationView = findViewById(R.id.animation_view);
        ImageView nextButton = findViewById(R.id.btn_forward);
        ImageView prevButton = findViewById(R.id.btn_backward);
        bottomStripView = findViewById(R.id.slide_strip);
        isBuffering = findViewById(R.id.isLoading);
        bottomStripView.setVisibility(View.VISIBLE);
        ImageButton portfolioButton = findViewById(R.id.portfolio);
        bottomStripView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        playPauseButton.setOnClickListener(this);

        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.app_icon)
                .showImageForEmptyUri(R.drawable.app_icon)
                .showImageOnFail(R.drawable.app_icon).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mSlidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    bottomStripView.setVisibility(View.VISIBLE);
                } else {
                    isExpand = true;
                    bottomStripView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                switch (newState){
                    case EXPANDED:
                        isExpand = true;
                        Log.e("Expanded", "True");
                        break;
                    case HIDDEN:
                        isExpand = false;
                        break;
                }
            }
        });

        portfolioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupPortfolio(v);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void updatePlaybackState(int i) {
        Logger.e("updatePlaybackState: ", "" + i);
        switch (i) {
            case PlaybackStateCompat.STATE_PLAYING:
                isBuffering.setVisibility(View.INVISIBLE);
                if(playPauseButton.isPlay())
                {
                    playPauseButton.toggle();
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                break;
            case PlaybackStateCompat.STATE_NONE:

                break;
            case PlaybackStateCompat.STATE_STOPPED:
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                isBuffering.setVisibility(View.VISIBLE);
                playPauseButton.toggle();
                break;
        }
    }

    private void playPauseEvent(View v) {

        if (streamingManager.isPlaying()) {
            streamingManager.onPause();
            ((PlayPauseView) v).toggle();
        } else {
            streamingManager.onPlay(currentSong);
            ((PlayPauseView) v).toggle();
        }
    }

    @Override
    public void playSongComplete() {

    }

    private void playSong(MediaMetaData media) {
        if (streamingManager != null) {
            streamingManager.onPlay(media);
        }
    }

    @Override
    public void currentSeekBarPosition(int i) {
        Log.e("Position", ""+i);
    }

    @Override
    public void playCurrent(int i, MediaMetaData mediaMetaData) {

    }

    @Override
    public void playNext(int i, MediaMetaData mediaMetaData) {

    }

    @Override
    public void playPrevious(int i, MediaMetaData mediaMetaData) {

    }

    private void showMediaInfo(MediaMetaData media) {
        currentSong = media;
        loadSongDetails(media);
    }

    private void loadSongDetails(MediaMetaData metaData) {
        songName.setText(metaData.getMediaTitle());
        stripSongName.setText(metaData.getMediaTitle());
        songAuthor.setText(metaData.getMediaArtist());
        stripSongAuthor.setText(metaData.getMediaArtist());
        Log.e("IMAGE URL", metaData.getMediaArt());

        imageLoader.displayImage(metaData.getMediaArt(), backgroundCoverImage, options);
        imageLoader.displayImage(metaData.getMediaArt(), coverImageView, options);
        imageLoader.displayImage(metaData.getMediaArt(), stripCoverImage, options);
    }

    public void showPopupPortfolio(View v){
        PopupMenu popup = new PopupMenu(this,v);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.menu_portfolio, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.github:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/carotkut94"));
                startActivity(browserIntent);
                return true;
            case R.id.play_store:
                Intent play = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Vishal+Bothra"));
                startActivity(play);
                startActivity(play);
                return  true;
            case R.id.website:
                Intent website = new Intent(Intent.ACTION_VIEW, Uri.parse("https://carotkut94.github.io/deathcode"));
                startActivity(website);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        View searchplate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);

        searchplate.setBackgroundColor(Color.WHITE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                songAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                songAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }


    protected boolean isOnline() {
        return InternetConnectionDetector.isConnected();
    }

    public void loadMusic(){
        if(isOnline()) {
            lottieAnimationView.setVisibility(View.INVISIBLE);
            recyclerView_music_list.setVisibility(View.VISIBLE);
            call = apiService.getSongs();
            call.enqueue(new Callback<List<Song>>() {
                @Override
                public void onResponse(@NonNull Call<List<Song>> call, @NonNull Response<List<Song>> response) {
                    if (response != null && response.body().size() > 0) {
                        songs = response.body();
                        for (Song song : songs) {
                            mediaMetaData = new MediaMetaData();
                            Log.e("name", song.getSong());
                            Log.e("streaming_url", song.getUrl());
                            mediaMetaData.setMediaTitle(song.getSong());
                            mediaMetaData.setMediaArtist(song.getArtists());
                            mediaMetaData.setMediaArt(song.getCoverImage());
                            mediaMetaData.setMediaUrl(song.getUrl());
                            mediaMetaData.setMediaId(song.getUrl());
                            musicList.add(mediaMetaData);
                        }
                        songAdapter = new SongAdapter(MusicActivity.this, songs);
                        recyclerView_music_list.setAdapter(songAdapter);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<Song>> call, @NonNull Throwable t) {
                    Log.e(MusicActivity.this.getClass().getName(), "Something went wrong");
                    Toast.makeText(context, "Broken Internet "+t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppController.getInstance().setConnectivityListener(this);
    }

    public void noInternet(){
        recyclerView_music_list.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected){
            loadMusic();
        }else{
            noInternet();
        }
    }
}
