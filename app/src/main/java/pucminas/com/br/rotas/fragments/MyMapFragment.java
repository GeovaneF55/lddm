package pucminas.com.br.rotas.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.services.RouteTrackService;
import pucminas.com.br.rotas.utils.PermissionUtils;
import pucminas.com.br.rotas.utils.RouteTrackingUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback,
                    GoogleMap.OnMyLocationButtonClickListener {

    public static final String TAG = MyMapFragment.class.getName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FloatingActionButton mStartEndBtn;
    private boolean mPermissionDenied;
    private boolean mIsTracking;
    private Context mContext;
    private RouteTrackingUtils mRouteTrackingUtils;

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
        mIsTracking = sharedPreferences.getBoolean("isTracking", false);
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
            mRouteTrackingUtils.setmIsTracking(mIsTracking);

            changeStartEndBtn();

            SharedPreferences sharedPreferences = mContext
                    .getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isTracking", mIsTracking);
            editor.apply();

            if (mIsTracking) {
                Toast.makeText(mContext, getString(R.string.routing_started), Toast.LENGTH_SHORT)
                        .show();

                // Call intent service.
                Intent serviceIntent = new Intent(mContext, RouteTrackService.class);
                mContext.startService(serviceIntent);

            } else {
                Toast.makeText(mContext, getString(R.string.routing_ended), Toast.LENGTH_SHORT)
                        .show();
                RouteTrackService.isTracking = false;
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
        googleMap.setOnMyLocationButtonClickListener(this);
        mRouteTrackingUtils = new RouteTrackingUtils(mContext, mIsTracking, googleMap);
        mRouteTrackingUtils.startLocationUpdates();

        if (! PermissionUtils.checkLocationPermission(getContext())) {
            // Permission to access the location is missing.
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            PermissionUtils.requestPermission(activity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);

            // Get current location at the first time map arrived.
            mRouteTrackingUtils.getmFusedLocationClient().getLastLocation()
                    .addOnSuccessListener(location ->
                            mRouteTrackingUtils.setmCurrentLocation(location));

            // Move camera to current location
            mRouteTrackingUtils.moveCamera(mRouteTrackingUtils.getmCurrentLocation());
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
            mRouteTrackingUtils.moveCamera(mRouteTrackingUtils.getmCurrentLocation());
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
}
