package com.example.echoloc.Directions;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class FindWalkingPath extends AsyncTask<String, Void, ArrayList<LatLng>> {

    Context context;
    GoogleMap googleMap;
    Polyline polyline;

    String[] arrParametersName = new String[8];
    String[] arrJsonKeys = new String[3];
    String[] arrJsonKeys2 = new String[3];

    public FindWalkingPath(Context context, GoogleMap googleMap) {
        super();
        this.context = context;
        this.googleMap = googleMap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> points) {
        super.onPostExecute(points);

        PolylineOptions options = new PolylineOptions();
        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                options.add(points.get(i));
            }
        }

        polyline = googleMap.addPolyline(options.width(15).color(Color.BLUE));
    }

    @Override
    protected ArrayList<LatLng> doInBackground(String[] args) {

        setArrays();

        TMapWebService tMapWebService = new TMapWebService("https://apis.openapi.sk.com/tmap/routes/pedestrian");
        tMapWebService.setParameters(arrParametersName, args, 8);

        GMapWebService gMapWebService = new GMapWebService("https://apis.openapi.sk.com/tmap/routes/pedestrian");
        gMapWebService.setParameters(arrParametersName, args, 8);
        ArrayList<LatLng> coordinates = gMapWebService.connectWebService("features");

        if (coordinates == null) {
            gMapWebService.setParameters(arrParametersName, args, 8);
            coordinates = gMapWebService.connectWebService("features");
        }
        if (coordinates != null) {
            System.out.println(coordinates);
        }

        return coordinates;
    }


    private void setArrays() {
        arrParametersName[0] = "version";
        arrParametersName[1] = "appKey";
        arrParametersName[2] = "startX";
        arrParametersName[3] = "startY";
        arrParametersName[4] = "endX";
        arrParametersName[5] = "endY";
        arrParametersName[6] = "startName";
        arrParametersName[7] = "endName";

        arrJsonKeys[0] = "features";
        arrJsonKeys[1] = "properties";
        arrJsonKeys[2] = "totalTime";

        arrJsonKeys2[0] = "features";
        arrJsonKeys2[1] = "properties";
        arrJsonKeys2[2] = "totalTime";
    }
}
