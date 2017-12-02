package pucminas.com.br.rotas.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.services.RouteTrackService;
import pucminas.com.br.rotas.utils.PermissionUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback,
                    GoogleMap.OnMyLocationButtonClickListener,
                    GoogleMap.OnMyLocationClickListener,
                    LocationListener {

    public static final String TAG = MyMapFragment.class.getName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mPermissionDenied;
    private boolean mIsStarted;
    private Context mContext;

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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mIsStarted = sharedPreferences.getBoolean("isStarted", false);
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

        FloatingActionButton startEndButton = getActivity().findViewById(R.id.start_end_button);
        startEndButton.setOnClickListener((view) -> {
            mIsStarted = !mIsStarted;
            SharedPreferences sharedPreferences = mContext
                    .getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isStarted", mIsStarted);
            editor.apply();

            /*
             * TODO: Handle stop service based on minutes stopped at the same location??
             * TODO: Maybe a separate file for Route Tracking task?
             */
            if (mIsStarted) {
                Toast.makeText(mContext, getString(R.string.routing_started), Toast.LENGTH_SHORT)
                        .show();

                startEndButton.setImageResource(R.drawable.ic_stop);
                startEndButton.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.red)
                ));

                // Set DrawMapRouteTask's map object.
                RouteTrackService.map = mMap;

                // Call intent service.
                Intent serviceIntent = new Intent(mContext, RouteTrackService.class);
                mContext.startService(serviceIntent);

            } else {
                Toast.makeText(mContext, getString(R.string.routing_ended), Toast.LENGTH_SHORT)
                        .show();

                startEndButton.setImageResource(R.drawable.ic_navigation);
                startEndButton.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.colorPrimary)
                ));
            }

            RouteTrackService.isTracking = false;
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
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
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
        mMap.setOnMyLocationClickListener(this);

        if (! PermissionUtils.checkLocationPermission(getContext())) {
            // Permission to access the location is missing.
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            PermissionUtils.requestPermission(activity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            // Get last location
            getCurrentLocation();
        }
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
            getCurrentLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Get last location
        getCurrentLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }

    private void getCurrentLocation() {
        if (PermissionUtils.checkLocationPermission(getContext())) {
            // Add listener to get last location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener((location) -> {
                        // Sometimes location could be null
                        if (location != null) {

                            // If location is defined, move camera to there.
                            moveCamera(location);
                        }
                    });
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
    }
}
