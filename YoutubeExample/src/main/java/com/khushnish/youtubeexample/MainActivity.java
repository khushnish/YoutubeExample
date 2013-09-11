package com.khushnish.youtubeexample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Iterator;

public class MainActivity extends Activity implements AdListener {

    /** Global instance of the HTTP transport. */
    private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global instance of the max number of videos we want returned (50 = upper limit per page). */
    private final long NUMBER_OF_VIDEOS_RETURNED = 50;

    /** Global instance of Youtube object to make all API requests. */
    private YouTube youtube;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        interstitial = new InterstitialAd(this, "");

        // Create ad request
        final AdRequest adRequest = new AdRequest();

        // Begin loading your interstitial
        interstitial.loadAd(adRequest);

        // Set Ad Listener to use the callbacks below
        interstitial.setAdListener(this);

        initImageLoader(this);

        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {}
        }).setApplicationName(getString(R.string.app_name)).build();

        new YoutubeTask().execute();
    }

    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    private void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Double checks the kind is video.
            if (rId.getKind().equals("youtube#video")) {
                final Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n--------------------------------------------------------\n");
            }
        }
    }

    @Override
    public void onReceiveAd(Ad ad) {
        if (ad == interstitial) {
            interstitial.show();
        }
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

    private class YoutubeTask extends AsyncTask<Void, Void, List<SearchResult>> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,
                    getString(R.string.app_name), "Please wait...");
        }

        @Override
        protected List<SearchResult> doInBackground(Void... params) {
            try {
                final YouTube.Search.List search = youtube.search().list("id,snippet");
                search.setKey(getString(R.string.api_key));
                search.setQ(getString(R.string.app_name));
                search.setType("video");
                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                SearchListResponse searchResponse = search.execute();

                final List<SearchResult> searchResultList = searchResponse.getItems();

//                if (searchResultList != null) {
//                    prettyPrint(searchResultList.iterator(), queryTerm);
//                }
                final ArrayList<SearchResult> results = new ArrayList<SearchResult>();
                for ( SearchResult searchResult : searchResultList ) {
                    if ( searchResult.getId().getKind().equals("youtube#video") ) {
                        results.add(searchResult);
                    }
                }
                results.trimToSize();
                return results;
            } catch (GoogleJsonResponseException e) {
                System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<SearchResult> searchResults) {
            super.onPostExecute(searchResults);

            if ( progressDialog != null && progressDialog.isShowing() ) {
                progressDialog.dismiss();
            }

            if ( searchResults != null ) {
                final ListView list = (ListView) findViewById(R.id.youtube_list);
                list.setAdapter(new YoutubeAdapter(MainActivity.this, R.layout.row_youtube,
                        R.id.row_youtube_txt_description, searchResults));
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final SearchResult result = (SearchResult) parent.getAdapter().getItem(position);

                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.app_name);
                        builder.setItems(R.array.pick_player, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (which == 0) {
                                    final Intent intent = new Intent(MainActivity.this, YoutubeActivity.class);
                                    Log.e("Youtube", "Video Id : " + result.getId().getVideoId());
                                    intent.putExtra("youtubeVideoId", result.getId().getVideoId());
                                    startActivity(intent);
                                } else {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                            "http://m.youtube.com/watch?v=" + result.getId().getVideoId())));

                                }
                            }
                        });

                        builder.create().show();
                    }
                });
            }
        }
    }
}