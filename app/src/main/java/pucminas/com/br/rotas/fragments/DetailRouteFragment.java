package pucminas.com.br.rotas.fragments;


import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.utils.PermissionUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailRouteFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {
    public static final String TAG = DetailRouteFragment.class.getPackage().getName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied;
    private GoogleMap mMap;

    private ArrayList<LatLng> mLatLngs;
    private FloatingActionButton mWhatsapp;


    public DetailRouteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static DetailRouteFragment newInstance(ArrayList<LatLng> latLngs) {
        DetailRouteFragment detailRouteFragment = new DetailRouteFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("latLngs", latLngs);
        detailRouteFragment.setArguments(args);
        return detailRouteFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_route, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (this.getArguments() != null) {
            mLatLngs = this.getArguments().getParcelableArrayList("latLngs");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.detailRoute);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWhatsapp = getActivity().findViewById(R.id.btn_whatsapp);

        mWhatsapp.setOnClickListener((view) -> {
            if (this.getArguments() != null) {
                mLatLngs = this.getArguments().getParcelableArrayList("latLngs");
                StringBuilder mensagem = new StringBuilder();
                for(Object value: mLatLngs.toArray()){
                    mensagem.append(value.toString()).append("\n");
                }

                sendWhatsappRoute(mensagem.toString());
            }
        });
    }

    private void sendWhatsappRoute(String mensagem) {
        Intent wppIntent = new Intent(Intent.ACTION_SEND);
        wppIntent.setType("text/plain");
        wppIntent.putExtra(Intent.EXTRA_TEXT, mensagem);
        wppIntent.setPackage("com.whatsapp");
        startActivityForResult(wppIntent, 1);
    }

    private void drawRoute() {
        PolylineOptions options = new PolylineOptions();
        options.color(Color.parseColor("#FF0000"));
        options.width(10);

        for (LatLng coords : mLatLngs) {
            options.add(coords);
        }

        mMap.addPolyline(options);
    }
    /**
     * Set up map location.
     */
    private void moveCamera(LatLng latLng) {
        if (latLng != null) {
            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
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

        if (! PermissionUtils.checkLocationPermission(getContext())) {
            // Permission to access the location is missing.
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            PermissionUtils.requestPermission(activity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            moveCamera(mLatLngs.get(0));

            try {
                Thread.sleep(2000);
                drawRoute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (! PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
    }

}
