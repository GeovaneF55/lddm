package pucminas.com.br.rotas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import pucminas.com.br.rotas.fragments.RoutesFragment;
import pucminas.com.br.rotas.fragments.MyMapFragment;
import pucminas.com.br.rotas.route.RouteItem;
import pucminas.com.br.rotas.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RoutesFragment.OnListFragmentInteractionListener {

    // Sign in request code
    private static final int RC_SIGN_IN = 1;

    // Firebase components
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private MyMapFragment mMapFragment = null;

    // BroadcastReceiver for route tracking
    public static final String DRAW_ACTION = RouteTrackBroadcastReceiver.class.getPackage()
            .getName() + ".DRAW_REQUESTED";

    private RouteTrackBroadcastReceiver mBroadcastReceiver;
    private ActionBar mActionBar;

    public class RouteTrackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyMapFragment myMapFragment = (MyMapFragment) getSupportFragmentManager()
                    .findFragmentByTag(MyMapFragment.TAG);

            android.util.Log.d("TESTE", "teste1");
            if (myMapFragment!= null && myMapFragment.getLocations() != null) {
                android.util.Log.d("TESTE", "teste");
                myMapFragment.getLocations().clear();
                myMapFragment.getLocations().addAll(intent.getParcelableArrayListExtra(MyMapFragment.KEY_LOCATIONS));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setTitle(getResources().getString(R.string.home));

        // Initialize firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = (firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // already signed in
                mMapFragment = MyMapFragment.newInstance();
                switchFragment(mMapFragment, MyMapFragment.TAG);
            } else {
                // not signed in
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(true)
                        .setAvailableProviders(
                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(),
                    RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        // Register broadcast receiver
        IntentFilter broadcastFilter = new IntentFilter(DRAW_ACTION);
        mBroadcastReceiver = new MainActivity.RouteTrackBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mBroadcastReceiver, broadcastFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister broadcast receiver
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RC_SIGN_IN:
                // Finish activity if user press back button.
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            AuthUI.getInstance().signOut(this);
            SharedPreferencesUtils.clear(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(RouteItem item) {
        //Do nothing
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            assert mActionBar != null;
            mActionBar.setTitle(getResources().getString(R.string.home));
            switchFragment(mMapFragment, MyMapFragment.TAG);
        } else if (id == R.id.nav_routes) {
            assert mActionBar != null;
            mActionBar.setTitle(getResources().getString(R.string.rotas));
            switchFragment(RoutesFragment.newInstance(), RoutesFragment.TAG);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }
}
