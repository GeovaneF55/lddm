package pucminas.com.br.rotas.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import pucminas.com.br.rotas.fragments.MyMapFragment;
import pucminas.com.br.rotas.utils.RouteTrackingUtils;

public class RouteTrackService extends IntentService {
    public static volatile boolean isTracking = true;
    private ArrayList<LatLng> mLocations;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RouteTrackService() {
        super(RouteTrackService.class.getName());

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RouteTrackService.isTracking = true;
        startTracking();

        while (RouteTrackService.isTracking) {
            try {
                Thread.sleep(5000);

                // Send broadcast to draw route
                sendBroadcast();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startTracking() {
        mLocations = new ArrayList<>();

        FusedLocationProviderClient fusedLocationProviderClient =
                RouteTrackingUtils.createFusedLocation(this);
        LocationRequest locationRequest = RouteTrackingUtils.createLocationRequest();
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (RouteTrackService.isTracking) {
                    Location location = locationResult.getLastLocation();
                    mLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        RouteTrackingUtils.startLocationUpdates(this, locationRequest, locationCallback,
                fusedLocationProviderClient);
    }

    private void sendBroadcast() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyMapFragment.DRAW_ACTION);
        broadcastIntent.putParcelableArrayListExtra(MyMapFragment.KEY_LOCATIONS, mLocations);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
