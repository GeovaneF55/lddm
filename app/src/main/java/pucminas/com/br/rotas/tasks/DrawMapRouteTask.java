package pucminas.com.br.rotas.tasks;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class DrawMapRouteTask implements LocationListener {
    public static GoogleMap map;
    public static List<LatLng> locations = new ArrayList<>();

    @Override
    public void onLocationChanged(Location location) {
        // Get LatLng of new location
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        locations.add(latLng);

        // Create options
        PolylineOptions options = new PolylineOptions();
        options.color(Color.parseColor("#FF0000"));
        options.width(10);
        options.visible(true);

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
