package com.hackerearth.mrsun.ipickup;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    int PICKUP_REQUEST_CODE = 0;
    int DROP_REQUEST_CODE = 1;
    static String TAG = "MainActivity";

    boolean pickupbool = false, dropbool = false;
    LatLng pickupLatLng, dropLatLng;

    public GoogleApiClient mGoogleApiClient;

    private GoogleMap map;

    Button pickup, drop;

    double distance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();


        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        pickup = (Button) findViewById(R.id.pickup);
        drop = (Button) findViewById(R.id.drop);

        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PICKUP_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, DROP_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            Log.i(TAG, "Place: " + place.getName());
            if (requestCode == PICKUP_REQUEST_CODE) {
                if(pickupbool){
                    map.clear();
                    distance = 0;
                }
                pickupbool = true;
                pickupLatLng = place.getLatLng();

                map.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup").anchor(0.5f, 0.5f));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15));
            } else if (requestCode == DROP_REQUEST_CODE) {
                dropbool = true;
                dropLatLng = place.getLatLng();
                map.addMarker(new MarkerOptions().position(dropLatLng).title("Drop").anchor(0.5f, 0.5f));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(dropLatLng, 15));
            }
            if(pickupbool && dropbool){
                drawDirection();
            }
        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(this, data);
            // TODO: Handle the error.
            Log.i(TAG, status.getStatusMessage());

        } else if (resultCode == RESULT_CANCELED) {

        }

    }

    private void drawDirection() {
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(constructDirectionURL());
    }

    private String constructDirectionURL(){

        String str_origin = "origin="+pickupLatLng.latitude+","+pickupLatLng.longitude;

        String str_dest = "destination="+dropLatLng.latitude+","+dropLatLng.longitude;

        String parameters = str_origin+"&"+str_dest+"&sensor=false";

        String url = "https://maps.googleapis.com/maps/api/directions/json?"+parameters;
        Log.d(TAG, url);
        return url;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class DownloadTask extends AsyncTask<String, Void, List<List<HashMap<String,String>>>> {
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... urls) {

            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            JSONObject jsonObject = null;

            List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try{
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while( ( line = br.readLine()) != null){
                    sb.append(line);
                }

                data = sb.toString();
                br.close();

            }catch(Exception e){
                Log.d("Exception:", e.toString());
            }finally{
                try {
                    iStream.close();
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            routes.clear();
            //Parse JSON
            try {
                jsonObject = new JSONObject(data);

                jRoutes = jsonObject.getJSONArray("routes");
                for(int i=0;i<jRoutes.length();i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();

                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            int dist = (Integer)((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("value");
                            distance += dist;
                            List<LatLng> list = decodePoly(polyline);

                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
            @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {
            super.onPostExecute(result);

                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                for(int i=0;i<result.size();i++){
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = result.get(i);

                             for(int j=0;j<path.size();j++){
                        HashMap<String,String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.width(5);
                    lineOptions.color(Color.BLUE);
                    Log.d(TAG, "Distance=" + distance);
                    Toast.makeText(MainActivity.this,"Distance: "+distance/1000+" KM", Toast.LENGTH_LONG).show();
                }

                map.addPolyline(lineOptions);
        }
    }
}
