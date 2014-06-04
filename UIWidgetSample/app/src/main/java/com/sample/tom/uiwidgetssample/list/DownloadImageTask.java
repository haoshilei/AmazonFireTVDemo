package com.sample.tom.uiwidgetssample.list;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
* Created by TomReinhart on 6/4/14.
*/
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    public interface ImageLoadedCallback {
        public void onImageLoaded(Bitmap image);
    }

    private ImageLoadedCallback imageLoadedCallback;

    public DownloadImageTask(ImageLoadedCallback imageLoadedCallback) {
        this.imageLoadedCallback = imageLoadedCallback;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        imageLoadedCallback.onImageLoaded(result);
    }
}
