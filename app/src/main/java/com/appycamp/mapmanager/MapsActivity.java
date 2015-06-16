package com.appycamp.mapmanager;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.appycamp.mapmanager.polylines.PolylineManager;
import com.appycamp.mapmanager.tiles.CustomTileProvider;
import com.appycamp.mapmanager.tiles.TileOverlayManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapsActivity extends AppCompatActivity{

    private static final String DRAW_TYPE_KEY = KeyGenerator.generateKey("DRAW_TYPE");;

    public static final double TRANS_LOC_LAT = 35.876189;
    public static final double TRANS_LOC_LNG = -78.843486;
    public static final LatLng officeCoords = new LatLng(TRANS_LOC_LAT, TRANS_LOC_LNG);
    private static final int DEFAULT_ZOOM = (int) ((CustomTileProvider.MAX_ZOOM_THRESHOLD + CustomTileProvider.MIN_ZOOM_THRESHOLD) / 2);
    public static final String API_KEY = "AIzaSyCD2VzaeYtBzIrMFpNrR9WAkjYz-tBBKDI";

    private enum DrawType{
        OVERLAY, POLYLINE, HYBRID
    }
    private DrawType mDrawType;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDrawType = DrawType.values()[getIntent().getExtras() == null? 0: getIntent().getExtras().getInt(DRAW_TYPE_KEY, 0)];
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean working = false;
        if(!working){
            Toast.makeText(this, "Switching between types is currently buggy", Toast.LENGTH_LONG).show();
            return true;
        }

        Intent showMap = new Intent(this, MapsActivity.class);
        showMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.use_overlay:
                if(mDrawType == DrawType.OVERLAY)
                    break;
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.OVERLAY.ordinal());
                startActivity(showMap);
                break;
            case R.id.use_polyline:
                if(mDrawType == DrawType.POLYLINE)
                    break;
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.POLYLINE.ordinal());
                startActivity(showMap);
                break;
            case R.id.user_hybrid:
                if(mDrawType == DrawType.HYBRID)
                    break;
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.HYBRID.ordinal());
                startActivity(showMap);
                break;
        }
        showMap = null;
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Obtain the map from the fragment
            final SupportMapFragment mapFragment =  (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    setUpMap();
                }
            });
        }
        /* TODO this doesnt work, need to trace down why (if map == null we need to figure out why setupMap() isn't getting called or doing
         * TODO(cont) what we would expect in the above code.
        else{
            switch(mDrawType){
                case OVERLAY:
                    drawPolylines(PolylineManager.NUM_ROUTES-1, PolylineManager.getInstance().getPolylineOptions(PolylineManager.NUM_ROUTES-1));
                    break;
                case POLYLINE:
                    for(int i = 0; i < PolylineManager.getInstance().getAllPolylineOptions().length; i++)
                        drawPolylines(i, PolylineManager.getInstance().getPolylineOptions(i));
                    finalizeMap();
                    break;
                case HYBRID:
                    Toast.makeText(MapsActivity.this, "Need to complete hybrid setup", Toast.LENGTH_LONG).show();
                    finalizeMap();
                    break;
            }
        }
        */
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (PolylineManager.getInstance() == null)
            PolylineManager.init(new PolylineManager.OnPolylineReadyListener() {

                @Override
                public void onPolylineReady(int polylinePosition, PolylineOptions polylineOptions) {
                    drawPolylines(polylinePosition, polylineOptions);
                }
            });

    }

    private void drawPolylines(int polylinePosition, PolylineOptions polylineOptions){
        if (mDrawType == DrawType.OVERLAY) {
            if (polylinePosition == PolylineManager.NUM_ROUTES - 1) {
                if (TileOverlayManager.getInstance().getPolylineOverlayOptions() == null)
                    TileOverlayManager.getInstance().initializePolylineOverlayOptions();

                if (TileOverlayManager.getInstance().getPolylineOverlayOptions() == null)
                    Log.e(getClass().getSimpleName(), "Loading in our polyline options fails, null load result");
                else
                    mMap.addTileOverlay(TileOverlayManager.getInstance().getPolylineOverlayOptions());

                finalizeMap();
            }
        } else if (mDrawType == DrawType.POLYLINE) {
            if (polylineOptions != null && polylineOptions.isVisible())
                mMap.addPolyline(polylineOptions);
            if (polylinePosition == PolylineManager.NUM_ROUTES - 1)
                finalizeMap();
        } else {
            Toast.makeText(MapsActivity.this, "Need to complete hybrid setup", Toast.LENGTH_LONG).show();
            finalizeMap();
        }
    }

    private void finalizeMap(){
        mMap.addMarker(new MarkerOptions().position(officeCoords).title("TransLoc Office"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(officeCoords, DEFAULT_ZOOM));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        findViewById(R.id.progress_map).setVisibility(View.GONE);
    }
}
