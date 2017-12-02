package pucminas.com.br.rotas.utils;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RouteTrackingUtils {
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Context mContext;

    public static List<LatLng> locations = new ArrayList<>();

    private GoogleMap mMap;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mIsTracking;

    public RouteTrackingUtils(Context context, boolean isTracking, GoogleMap map) {
        mMap = map;
        mContext = context;
        mIsTracking = isTracking;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void startLocationUpdates() {
        createLocationRequest();
        createLocationCallback();

        if (PermissionUtils.checkLocationPermission(mContext)) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                moveCamera(mCurrentLocation);

                if (mIsTracking) {
                    locations.add(new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()));
                }
            }
        };
    }

    /**
     * Set up map location.
     */
    public void moveCamera(Location location) {
        if (location != null) {
            // Get latitude of the current location
            double latitude = location.getLatitude();

            // Get longitude of the current location
            double longitude = location.getLongitude();

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            drawRoute();
        }
    }

    public void drawRoute() {
        if (mIsTracking) {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.parseColor("#FF0000"));
            options.width(10);

            for (LatLng coords : locations) {
                options.add(coords);
            }

            mMap.addPolyline(options);
        }
    }


    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    public void setmCurrentLocation(Location mCurrentLocation) {
        this.mCurrentLocation = mCurrentLocation;
    }

    public FusedLocationProviderClient getmFusedLocationClient() {
        return mFusedLocationClient;
    }

    public void setmFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    public boolean ismIsTracking() {
        return mIsTracking;
    }

    public void setmIsTracking(boolean mIsTracking) {
        this.mIsTracking = mIsTracking;
    }
}
