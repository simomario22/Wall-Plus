package ytstudios.wall.bucket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Yugansh Tyagi on 29-09-2017.
 */

public class DownloadFragmentAdapter extends RecyclerView.Adapter {

    private ArrayList<String> paths;
    Context context;
    private DisplayMetrics displayMetrics;
    public static Uri uri;

    public DownloadFragmentAdapter(Context context, ArrayList<String> paths) {
        this.paths = paths;
        this.context = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        displayMetrics = new DisplayMetrics();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.downloaded_item, parent, false);
        viewHolder = new DownloadsHolder(v, context, paths);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        int width = displayMetrics.widthPixels / 2;
        int height = 220;

        uri = Uri.fromFile(new File(paths.get(position)));
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .override(width, height);
        Glide.with(context).load(uri).apply(myOptions).transition(withCrossFade()).into(((DownloadsHolder) holder).downloadedImage);


        ((DownloadsHolder) holder).deleteDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animation = AnimationUtils.loadAnimation(context, R.anim.zoom_out);
                Animation animation1 = AnimationUtils.loadAnimation(context, R.anim.fadeout);
                animation.setDuration(200);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        File fdelete = new File(paths.get(position));
                        if (fdelete.exists()) {
                            fdelete.delete();
                        }
                        Intent mediaScanIntent = new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(fdelete); //out is your file you saved/deleted/moved/copied
                        mediaScanIntent.setData(contentUri);
                        context.sendBroadcast(mediaScanIntent);
                        Intent intent = new Intent("Refresh");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                ((DownloadsHolder) holder).downloadedImage.startAnimation(animation);
                ((DownloadsHolder) holder).deleteDownload.startAnimation(animation1);
                ((DownloadsHolder) holder).view.startAnimation(animation1);
            }
        });
    }

    @Override
    public int getItemCount() {
        try {
            return paths.size();
        } catch (Exception e) {
        }
        return 0;
    }

    public static class DownloadsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        SimpleDraweeView downloadedImage;
        ImageView deleteDownload;
        View view;
        Context context;
        ArrayList<String> paths;

        public DownloadsHolder(View itemView, Context context, ArrayList<String> paths) {
            super(itemView);
            this.context = context;
            downloadedImage = itemView.findViewById(R.id.downloadImage);
            this.paths = paths;
            itemView.setOnClickListener(this);
            this.deleteDownload = itemView.findViewById(R.id.deleteDownload);
            this.view = itemView.findViewById(R.id.viewBar);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Intent intent = new Intent(context, FullWallpaperViewActivity.class);
            intent.putExtra("paths", paths);
            intent.putExtra("position", position);
            intent.putExtra("caller", "Downloads");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.context.startActivity(intent);

        }
    }
}
