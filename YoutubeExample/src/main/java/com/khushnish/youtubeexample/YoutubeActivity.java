package com.khushnish.youtubeexample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.ads.InterstitialAd;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubeActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener, AdListener {

    private String videoId = "";
    private YouTubePlayerView youTubeView;
    private boolean isInitialized = false;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        final AdRequest adRequest = new AdRequest();

        interstitial = new InterstitialAd(this, getString(R.string.ads_interstitial_key));
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(this);

        videoId = getIntent().getStringExtra("youtubeVideoId");
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( !isInitialized ) {
            youTubeView.initialize(getString(R.string.api_key), this);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            isInitialized = true;
            youTubePlayer.cueVideo(videoId);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult youTubeInitializationResult) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.google.android.youtube"));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setTitle(getString(R.string.app_name));
        dialog.setMessage(getString(R.string.update_youtube_player));
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onReceiveAd(Ad ad) {
        if (ad == interstitial) {
            interstitial.show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final AdView adView = (AdView)findViewById(R.id.activity_youtube_adView);
        final RelativeLayout parent = (RelativeLayout) findViewById(R.id.activity_youtube_container);
        final ViewGroup.LayoutParams params = adView.getLayoutParams();

        parent.removeView(adView);

        final AdView newAdView = new AdView(this, AdSize.SMART_BANNER, getString(R.string.ads_key));
        newAdView.setId(R.id.activity_youtube_adView);

        parent.addView(newAdView, params);
        newAdView.loadAd(new AdRequest());
    }

    @Override
    public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {

    }

    @Override
    public void onPresentScreen(Ad ad) {

    }

    @Override
    public void onDismissScreen(Ad ad) {

    }

    @Override
    public void onLeaveApplication(Ad ad) {

    }
}