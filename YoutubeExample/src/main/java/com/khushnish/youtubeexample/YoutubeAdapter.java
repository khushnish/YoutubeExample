package com.khushnish.youtubeexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.youtube.model.SearchResult;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class YoutubeAdapter extends ArrayAdapter<SearchResult> {

    private LayoutInflater linflater;
    private List<SearchResult> searchResult;
    private DisplayImageOptions options;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    public YoutubeAdapter(Context context, int resource, int textViewResourceId, List<SearchResult> searchResult) {
        super(context, resource, textViewResourceId, searchResult);
        linflater = LayoutInflater.from(context);
        this.searchResult = searchResult;

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public int getCount() {
        return searchResult.size();
    }

    @Override
    public SearchResult getItem(int position) {
        return searchResult.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        View rowView = convertView;
        final int pos = position;

        if (rowView == null) {
            //inflating and finding views
            rowView = linflater.inflate(R.layout.row_youtube, null);
            holder = new ViewHolder();
            holder.txtDescription = (TextView) rowView.findViewById(R.id.row_youtube_txt_description);
            holder.imgThumb = (ImageView) rowView.findViewById(R.id.row_youtube_img_icon);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.txtDescription.setText(searchResult.get(position).getSnippet().getTitle());
        imageLoader.displayImage(searchResult.get(position).getSnippet().
                getThumbnails().getDefault().getUrl(), holder.imgThumb, options);

        return rowView;
    }

    private static class ViewHolder {
        private ImageView imgThumb;
        public TextView txtDescription;
    }
}