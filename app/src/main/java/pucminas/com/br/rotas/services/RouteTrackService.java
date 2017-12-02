package pucminas.com.br.rotas.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class RouteTrackService extends IntentService implements LocationListener {
    public static volatile boolean isTracking = true;
    public static GoogleMap map;

    private List<LatLng> mLocations;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RouteTrackService() {
        super(RouteTrackService.class.getName());
        mLocations = new ArrayList<>();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RouteTrackService.isTracking = true;

        while (RouteTrackService.isTracking) {
            Log.d("TESTE", "Map: " + ((map == null) ? "null" : "not null"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("TESTE", "Location Changed");

        // Get LatLng of new location
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mLocations.add(latLng);

        // Create options
        PolylineOptions options = new PolylineOptions();
        options.color(Color.parseColor("#FF0000"));
        options.width(10);
        options.visible(true);

        for (LatLng locationRecorded : mLocations) {
            options.add(locationRecorded);
            Log.d("TESTE", locationRecorded.toString());
        }

        map.addPolyline(options);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
