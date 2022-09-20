package com.example.echoloc.Directions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.echoloc.TMapsActivity;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FetchBitmap extends AsyncTask<Void, Void, Bitmap> {

    String imageURL;
    TMapPoint tMapPoint;
    TMapView tMapView;

    public FetchBitmap(String imageURL, TMapView tMapView, TMapPoint tMapPoint) {
        imageURL = imageURL;
        tMapPoint = tMapPoint;
        tMapView = tMapView;
    }

    @Override
    protected void onPostExecute(Bitmap result) {

    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        return getBitmapFromURL(imageURL);
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
