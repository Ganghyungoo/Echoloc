package com.example.echoloc.Directions;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class FindElapsedTimeTask extends AsyncTask<String, Void, String> {

    Context context;
    String[] arrParametersName = new String[8];
    String[] arrJsonKeys = new String[3];
    String[] arrJsonKeys2 = new String[3];

    TMapPolyLine tMapPolyLine;
    TMapView tMapView;

    public FindElapsedTimeTask(Context context, TMapView tMapView) {
        super();
        this.context = context;
        this.tMapView = tMapView;
    }

    private String MinuteToSecond(int nSecond) {
        String strText = null;
        try {
            if (nSecond >= 3600) {
                int hour = (nSecond / 3600);
                int minute = (nSecond % 3600 / 60);

                strText = String.format(("%d시간 %d분"), hour, minute);
            } else {
                int minute = (nSecond / 60);
                strText = String.format(("%d분"), minute);
            }

            return strText;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPreExecute();
        String[] array = str.split(",");
        String strTime = MinuteToSecond(Integer.parseInt(array[0]));

        Toast.makeText(context, "시간 : " + strTime + "\n거리 : " + array[1] + "m", Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(String[] args) {

        setArrays();

        TMapWebService tMapWebService = new TMapWebService("https://apis.openapi.sk.com/tmap/routes/pedestrian");
        WebService webService = new WebService("https://apis.openapi.sk.com/tmap/routes/pedestrian");
        tMapWebService.setParameters(arrParametersName, args, 8);
        String totalTime = tMapWebService.connectWebService(arrJsonKeys);
        String totalDistance = tMapWebService.connectWebService(arrJsonKeys2);

        webService.setParameters(arrParametersName, args, 8);
        ArrayList<TMapPoint> coordinates = webService.connectWebService("features");

        tMapPolyLine = new TMapPolyLine();
        tMapPolyLine.setLineColor(Color.BLUE);
        tMapPolyLine.setLineWidth(2);

        if (coordinates == null) {
            coordinates = webService.connectWebService("features");
        }
        System.out.println(coordinates);
        if (coordinates != null) {
            for (int i = 0; i < coordinates.size(); i++) {
                tMapPolyLine.addLinePoint(coordinates.get(i));
            }
            tMapView.addTMapPolyLine("Line",tMapPolyLine);
        }

        return totalTime + "," + totalDistance;
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
