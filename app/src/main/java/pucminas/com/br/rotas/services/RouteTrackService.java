package pucminas.com.br.rotas.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import pucminas.com.br.rotas.MainActivity;
import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.fragments.MyMapFragment;
import pucminas.com.br.rotas.utils.SharedPreferencesUtils;

public class RouteTrackService extends IntentService {
    public static ArrayList<LatLng> locations = new ArrayList<>();

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

        LocationResult result;
        if (intent != null && (result = LocationResult.extractResult(intent)) != null) {
            Location location = result.getLastLocation();

            sendBroadcast(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    private void sendBroadcast(LatLng latLng) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.DRAW_ACTION);
        locations.add(latLng);
        broadcastIntent.putParcelableArrayListExtra(MyMapFragment.KEY_LOCATIONS, locations);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
