package pucminas.com.br.rotas.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.fragments.MyMapFragment;
import pucminas.com.br.rotas.utils.SharedPreferencesUtils;

public class RouteTrackService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RouteTrackService() {
        super(RouteTrackService.class.getName());

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean isTracking = SharedPreferencesUtils.readBoolean(getApplicationContext(), MyMapFragment.KEY_IS_TRACKING);

        if (! isTracking) {
            return;
        }

        if (intent != null) {
            LocationResult result = LocationResult.extractResult(intent);

            if (result != null) {
                Log.d("TESTE", result.getLastLocation().toString());
                Location location = result.getLastLocation();
                sendBroadcast(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }
    }
    private void sendBroadcast(LatLng latLng) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyMapFragment.DRAW_ACTION);
        broadcastIntent.putExtra(MyMapFragment.KEY_LOCATIONS, latLng);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
