package pucminas.com.br.rotas.fragments;


import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.services.RouteTrackService;
import pucminas.com.br.rotas.utils.PermissionUtils;
import pucminas.com.br.rotas.utils.RouteTrackingUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback,
                    GoogleMap.OnMyLocationButtonClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public static final String DRAW_ACTION = RouteTrackBroadcastReceiver.class.getPackage()
            .getName() + ".DRAW_REQUESTED";
    public static final String KEY_IS_TRACKING = "isTracking";
    public static final String KEY_LOCATIONS = "locations";
    public static final String TAG = MyMapFragment.class.getName();
    private static final int RC_LOCATIONS_UPDATE_CODE = 1;

    private boolean mIsTracking;
    private boolean mPermissionDenied;
    private Context mContext;
    private FloatingActionButton mStartEndBtn;

    private ArrayList<LatLng> mLocations;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private RouteTrackBroadcastReceiver mBroadcastReceiver;

    // BroadcastReceiver for route tracking
    public class RouteTrackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLocations.add(intent.getParcelableExtra(MyMapFragment.KEY_LOCATIONS));
            drawRoute();
        }
    }

    /**
     * Factory method used to create fragment.
     * @return A new instance of fragment MyMapFragment.
     */
    public static MyMapFragment newInstance() {
        return new MyMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mIsTracking = sharedPreferences.getBoolean(KEY_IS_TRACKING, false);

        mLocations = new ArrayList<>();
        mFusedLocationProviderClient = RouteTrackingUtils.createFusedLocation(mContext);
        mLocationRequest = RouteTrackingUtils.createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                moveCamera(locationResult.getLastLocation());
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mStartEndBtn = getActivity().findViewById(R.id.start_end_button);
        changeStartEndBtn();

        mStartEndBtn.setOnClickListener((view) -> {
            mIsTracking = !mIsTracking;

            changeStartEndBtn();

            SharedPreferences sharedPreferences = mContext
                    .getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_TRACKING, mIsTracking);
            editor.apply();

            if (mIsTracking) {
                Toast.makeText(mContext, getString(R.string.routing_started), Toast.LENGTH_SHORT)
                        .show();

                // Call intent service.
                startRouteTracking();

            } else {
                Toast.makeText(mContext, getString(R.string.routing_ended), Toast.LENGTH_SHORT)
                        .show();

                if (PermissionUtils.checkLocationPermission(mContext)) {
                    mFusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(location -> mCurrentLocation = location);
                }

                mLocations.add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                mMap.clear();
                drawRoute();
                stopRouteTracking();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }

        // Register broadcast receiver
        IntentFilter broadcastFilter = new IntentFilter(DRAW_ACTION);
        mBroadcastReceiver = new RouteTrackBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.registerReceiver(mBroadcastReceiver, broadcastFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister broadcast receiver
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_TRACKING, false);
        editor.apply();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);

        // Location updates for this fragment
        RouteTrackingUtils.startLocationUpdates(mContext, mLocationRequest, mLocationCallback,
                mFusedLocationProviderClient);

        if (! PermissionUtils.checkLocationPermission(getContext())) {
            // Permission to access the location is missing.
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            PermissionUtils.requestPermission(activity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            // Get current location at the first time map arrived.
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> mCurrentLocation = location);

            // Move camera to current location
            moveCamera(mCurrentLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            moveCamera(mCurrentLocation);
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
    }

    /**
     * Set up map location.
     */
    private void moveCamera(Location location) {
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

    private void drawRoute() {
        PolylineOptions options = new PolylineOptions();
        options.color(Color.parseColor("#FF0000"));
        options.width(10);

        for (LatLng coords : mLocations) {
            options.add(coords);
        }

        mMap.addPolyline(options);
    }

    private void changeStartEndBtn() {
        if (mIsTracking) {
            mStartEndBtn.setImageResource(R.drawable.ic_stop);
            mStartEndBtn.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.red)
            ));

        } else {
            mStartEndBtn.setImageResource(R.drawable.ic_navigation);
            mStartEndBtn.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.colorPrimary)
            ));
        }
    }

    private PendingIntent getPendingIntent() {
        Intent serviceIntent = new Intent(mContext, RouteTrackService.class);
        return PendingIntent.getService(mContext, RC_LOCATIONS_UPDATE_CODE,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private void startRouteTracking() {

        RouteTrackingUtils.startLocationUpdates(mContext, mLocationRequest, getPendingIntent(),
                mFusedLocationProviderClient);
    }
    private void stopRouteTracking() {
        RouteTrackingUtils.removeLocationUpdates(mFusedLocationProviderClient, getPendingIntent());
    }

}
