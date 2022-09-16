package com.example.echoloc.Directions;

import com.google.android.gms.maps.model.LatLng;
import com.skt.Tmap.TMapPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WebService {

    private final String ROUTES = "https://apis.openapi.sk.com/tmap/routes/pedestrian";

    private String mStrFullURI = "";
    private String mStrURI = "";

    private int totalDistance = 0;
    private int totalTime = 0;
    private ArrayList<TMapPoint> mapPoints;

    public WebService(String uri) { this.mStrURI = uri;}

    public void setParameters(String[] parameterName, String[] parameterData, int size) {
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                mStrFullURI += mStrURI + "?" + parameterName[i] + "=" + parameterData[i];
                continue;
            }

            mStrFullURI += "&" + parameterName[i] + "=" + parameterData[i];
        }
    }

    public ArrayList<TMapPoint> connectWebService(String jsonKey) {
        try {
            URL url = new URL(mStrFullURI);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);

            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(is));

            ArrayList<TMapPoint> points = parseJSON(json, jsonKey);
            return points;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getStringFromInputStream(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String line = "";

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return stringBuilder.toString();
    }

    private ArrayList<TMapPoint> parseJSON(JSONObject jsonObject, String arrKey) throws JSONException {
        if (ROUTES.equals(mStrURI)) {

//            JSONArray jsonArray = jsonObject.getJSONArray(arrKey[0]);
            JSONArray features = jsonObject.getJSONArray(arrKey);
            mapPoints = new ArrayList<>();

            for (int i = 0; i < features.length(); i++) {
                JSONObject test2 = features.getJSONObject(i);
                if (i == 0) {
                    JSONObject properties = test2.getJSONObject("properties");
                    totalDistance += properties.getInt("totalDistance");
                    totalTime += properties.getInt("totalTime");
                }
                JSONObject geometry = test2.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                String geoType = geometry.getString("type");
                if (geoType.equals("Point")) {
                    double lonJson = coordinates.getDouble(0);
                    double latJson = coordinates.getDouble(1);
                    TMapPoint point = new TMapPoint(latJson, lonJson);
                    mapPoints.add(point);
                }
                if (geometry.equals("LineString")) {
                    for (int j = 0; j < coordinates.length(); j++) {
                        JSONArray JLinePoint = coordinates.getJSONArray(j);
                        double lonJson = JLinePoint.getDouble(0);
                        double latJson = JLinePoint.getDouble(1);
                        TMapPoint point = new TMapPoint(latJson, lonJson);
                        mapPoints.add(point);
                    }
                }
            }

            return mapPoints;
//            JSONObject jsonFeatures = jsonArray.getJSONObject(0);
//            JSONObject jsonProperties = jsonFeatures.getJSONObject(arrKey[1]);
//
//            String strTime = jsonProperties.getString(arrKey[2]);
//
//            return strTime;
        }

        return null;
    }
}
