package pucminas.com.br.rotas.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import pucminas.com.br.rotas.Adapters.MyRoutesRecyclerViewAdapter;
import pucminas.com.br.rotas.R;
import pucminas.com.br.rotas.RecyclerClickListener;
import pucminas.com.br.rotas.route.RouteContent;
import pucminas.com.br.rotas.route.RouteItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RoutesFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private DatabaseReference mRouteDatabaseReference;

    public static final String TAG = RoutesFragment.class.getName();
    private int mColumnCount;
    private MyRoutesRecyclerViewAdapter mAdapter;

    private GestureDetector mGestureDetector;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RoutesFragment() {
    }

    public static RoutesFragment newInstance() {
        RoutesFragment routesFragment = new RoutesFragment();
        routesFragment.mColumnCount = 0;
        return routesFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get current user
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Get firebase reference to route.
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        RouteContent.clearItems();

        assert mFirebaseUser != null;
        mRouteDatabaseReference = firebaseDatabase.getReference().child(mFirebaseUser.getUid()).child("route");
        ChildEventListener routeChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Timestamp timestamp = new Timestamp(Long.valueOf(dataSnapshot.getKey()));
                String id = timestamp.toString();
                ArrayList<LatLng> route = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    HashMap hashMap = (HashMap) data.getValue();
                    double latitude = (double) hashMap.get("latitude");
                    double longitude = (double) hashMap.get("longitude");
                    LatLng latLng = new LatLng(latitude, longitude);
                    route.add(latLng);
                }

                RouteItem routeItem = RouteContent.createRouteItem(id, route);
                RouteContent.addItem(routeItem);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        mRouteDatabaseReference.addChildEventListener(routeChildEventListener);

        // RecyclerView
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.addOnItemTouchListener(new RecyclerClickListener(getActivity(),
                    (view1, position) -> {
                        ArrayList<LatLng> latLngs = mAdapter.getValues().get(position).content;
                        DetailRouteFragment detailRouteFragment = DetailRouteFragment.newInstance(latLngs);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, detailRouteFragment,
                                DetailRouteFragment.TAG);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }) {
            });
            mAdapter = new MyRoutesRecyclerViewAdapter(RouteContent.ITEMS, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mGestureDetector = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(RouteItem item);
    }
}
