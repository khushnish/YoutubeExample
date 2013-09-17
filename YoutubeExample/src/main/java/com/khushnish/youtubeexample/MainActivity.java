package com.khushnish.youtubeexample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private final long NUMBER_OF_VIDEOS_RETURNED = 50;
    private YouTube youtube;
    private AdView adView;
    private String pageToken = "";
    private List<SearchResult> searchResults = null;
    private YoutubeAdapter youTubeAdapter;
    private String sorting = "relevance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();

        final JsonFactory JSON_FACTORY = new JacksonFactory();
        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName(getString(R.string.app_name)).build();
    }

    private void initializeComponents() {

        final SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sortby,
                android.R.layout.simple_expandable_list_item_1);

        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int pos, long l) {
                pageToken = "";
                searchResults.clear();
                youTubeAdapter.notifyDataSetChanged();

                if (pos == 0) {
                    sorting = "relevance";
                } else if (pos == 1) {
                    sorting = "date";
                } else if (pos == 2) {
                    sorting = "viewCount";
                } else if (pos == 3) {
                    sorting = "rating";
                }

                if (Utils.checkInternetConnection(MainActivity.this)) {
                    new YoutubeTask().execute(sorting);
                } else {
                    Utils.displayDialog(getString(R.string.app_name),
                            getString(R.string.check_internet_connection),
                            MainActivity.this, getString(android.R.string.ok));
                }
                return true;
            }
        });

        initImageLoader(this);

        adView = (AdView) findViewById(R.id.activity_main_adView);
        final ListView list = (ListView) findViewById(R.id.youtube_list);
        searchResults = new ArrayList<SearchResult>();
        youTubeAdapter = new YoutubeAdapter(MainActivity.this, R.layout.row_youtube,
                R.id.row_youtube_txt_description, searchResults);
        list.setAdapter(youTubeAdapter);

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

        list.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean loadMore = /* maybe add a padding */
                        firstVisibleItem + visibleItemCount >= totalItemCount;

                if (loadMore) {
                    new YoutubeTask().execute(sorting);
                }
            }
        });
    }

    private void initImageLoader(Context context) {
        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final AdView adView = (AdView) findViewById(R.id.activity_main_adView);
        final RelativeLayout parent = (RelativeLayout) findViewById(R.id.activity_main_container);
        final ViewGroup.LayoutParams params = adView.getLayoutParams();

        parent.removeView(adView);

        final AdView newAdView = new AdView(this, AdSize.SMART_BANNER, getString(R.string.ads_key));
        newAdView.setId(R.id.activity_main_adView);

        parent.addView(newAdView, params);
        newAdView.loadAd(new AdRequest());
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public FragmentManager getSupportFragmentManager() {
        return null;
    }

    private class YoutubeTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,
                    getString(R.string.app_name), "Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                final YouTube.Search.List search = youtube.search().list("id,snippet");
                search.setKey(getString(R.string.api_key));
                search.setQ(getString(R.string.app_name));
                search.setType("video");
                search.setOrder(params[0]);
                search.setFields("nextPageToken,pageInfo(totalResults),items(id/kind,id/videoId," +
                        "snippet/title,snippet/thumbnails/default/url)");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                search.setPart("id,snippet");
                search.setPageToken(pageToken);
                SearchListResponse searchResponse = search.execute();

                final List<SearchResult> searchResultList = searchResponse.getItems();

                for (SearchResult searchResult : searchResultList) {
                    if (searchResult.getId().getKind().equals("youtube#video")) {
                        MainActivity.this.searchResults.add(searchResult);
                    }
                }
                pageToken = searchResponse.getNextPageToken();
                return null;
            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            youTubeAdapter.notifyDataSetChanged();
        }
    }
}