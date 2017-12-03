package pucminas.com.br.rotas.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
public class RouteTrackingUtils {
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static FusedLocationProviderClient createFusedLocation(Context context) {
        return LocationServices.getFusedLocationProviderClient(context);
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    public static void startLocationUpdates(Context context, LocationRequest locationRequest,
                                            LocationCallback locationCallback,
                                            FusedLocationProviderClient fusedLocationProviderClient) {

        if (PermissionUtils.checkLocationPermission(context)) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        }
    }

    public static void startLocationUpdates(Context context, LocationRequest locationRequest,
                                            PendingIntent callbackIntent,
                                            FusedLocationProviderClient fusedLocationProviderClient) {
        if (PermissionUtils.checkLocationPermission(context)) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, callbackIntent);
        }
    }

    public static void removeLocationUpdates(FusedLocationProviderClient fusedLocationProviderClient,
                                             PendingIntent pendingIntent) {
        fusedLocationProviderClient.removeLocationUpdates(pendingIntent);
    }
}
