package com.appycamp.mapmanager;

import android.content.Intent;
import android.os.Bundle;

import com.appycamp.mapmanager.markers.IpSearchDialog;
import com.appycamp.mapmanager.markers.MarkerClusterItem;
import com.appycamp.mapmanager.markers.MarkerClusterRenderer;
import com.appycamp.mapmanager.markers.MarkerRequestService;
import com.appycamp.mapmanager.markers.MyMarkerManager;
import com.appycamp.mapmanager.network.models.MarkerModel;
import com.appycamp.mapmanager.polylines.PolylineManager;
import com.appycamp.mapmanager.tiles.CustomTileProvider;
import com.appycamp.mapmanager.tiles.TileOverlayManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapsActivity extends AppCompatActivity{

    private static final String DRAW_TYPE_KEY = KeyGenerator.generateKey("DRAW_TYPE");

    public static final String MY_HOME_IP = "66.57.4.181";
    public static final String DUMMY_TEST_IP = "66.57.10.200";

    public static final double HOME_LOC_LAT = 39.78373;
    public static final double HOME_LOC_LNG = -100.445882;
    public static final LatLng OFFICE_COORDS = new LatLng(HOME_LOC_LAT, HOME_LOC_LNG);
    private static final int MARKER_ZOOM = 3;
    private static final int MARKER_CLUSTER_FIT_PADDING = 50;
    private static final int POLYLINE_ZOOM = (int) ((CustomTileProvider.MAX_ZOOM_THRESHOLD + CustomTileProvider.MIN_ZOOM_THRESHOLD) / 2);
    public static final String API_KEY = "AIzaSyCD2VzaeYtBzIrMFpNrR9WAkjYz-tBBKDI";


    private enum DrawType{
        MARKER, POLYLINE_OVERLAY, POLYLINE_MAP, POLYLINE_HYBRID
    }
    private DrawType mDrawType;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MyMarkerManager mMarkerManager;
    private LatLngBounds.Builder mMarkerBoundsBuilder;
    private ClusterManager<MarkerClusterItem> mClusterManager;
    private ProgressBar mNetworkProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDrawType = DrawType.values()[getIntent().getExtras() == null? 0: getIntent().getExtras().getInt(DRAW_TYPE_KEY, 0)];
        mMarkerManager = MyMarkerManager.getInstance();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        switch(mDrawType){
            case POLYLINE_OVERLAY:
                menu.removeItem(R.id.show_polyline_overlay);
                menu.removeItem(R.id.search);
                break;
            case POLYLINE_MAP:
                menu.removeItem(R.id.show_polyline_map);
                menu.removeItem(R.id.search);
                break;
            case POLYLINE_HYBRID:
                menu.removeItem(R.id.show_polyline_hybrid);
                menu.removeItem(R.id.search);
                break;
            default:
                menu.removeItem(R.id.show_markers);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.search){
            RelativeLayout searchDialog = (RelativeLayout) getLayoutInflater().inflate(R.layout.ip_search_dialog, null);
            new IpSearchDialog(searchDialog, new IpSearchDialog.OnIpSearchListener(){
                @Override
                public void onSearchRequested(String startIp, String endIp) {
                    setUpClusterer();
                    requestIpScan(startIp, endIp);
                }
            });
            return true;
        }

        Intent showMap = new Intent(this, MapsActivity.class);
        showMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.show_markers:
                if(mDrawType == DrawType.MARKER)
                    break;
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.MARKER.ordinal());
                startActivity(showMap);
                break;
            case R.id.show_polyline_overlay:
                if(mDrawType == DrawType.POLYLINE_OVERLAY)
                    break;
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.POLYLINE_OVERLAY.ordinal());
                startActivity(showMap);
                break;
            case R.id.show_polyline_map:
                if(mDrawType == DrawType.POLYLINE_MAP)
                    break;
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.POLYLINE_MAP.ordinal());
                startActivity(showMap);
                break;
            case R.id.show_polyline_hybrid:
                if(mDrawType == DrawType.POLYLINE_HYBRID)
                    break;

                Toast.makeText(MapsActivity.this, "Need to complete hybrid setup", Toast.LENGTH_LONG).show();
                break;
                /*TODO when complete uncomment this
                showMap.putExtra(DRAW_TYPE_KEY, DrawType.POLYLINE_HYBRID.ordinal());
                startActivity(showMap);
                break;
                */
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
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if(mDrawType == DrawType.MARKER) {
            mNetworkProgressBar = (ProgressBar) findViewById(R.id.progress_network);

            setUpClusterer();
            finalizeMap();
        }

        else if(PolylineManager.getInstance() == null)
            PolylineManager.init(new PolylineManager.OnPolylineReadyListener() {

                @Override
                public void onPolylineReady(int polylinePosition, PolylineOptions polylineOptions) {
                    switch(mDrawType){
                        case POLYLINE_OVERLAY:
                            if(polylinePosition == PolylineManager.NUM_ROUTES -1){
                                drawRenderedOverlays();
                                finalizeMap();
                            }
                            break;
                        case POLYLINE_MAP:
                            drawPolylines(polylinePosition, polylineOptions);
                            break;
                    }
                }
            });
        else{
            PolylineOptions[] polylineOptions = PolylineManager.getInstance().getAllPolylineOptions();
            switch(mDrawType){
                case POLYLINE_OVERLAY:
                    drawRenderedOverlays();
                    finalizeMap();
                    break;
                case POLYLINE_MAP:
                    for(int i = 0; i < polylineOptions.length; i++)
                        drawPolylines(i, polylineOptions[i]);
                    break;
                case POLYLINE_HYBRID:
                    //TODO complete
                    break;
            }
        }
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new MarkerClusterRenderer(this, mMap, mClusterManager));

        // Point the map's listeners at the listeners implemented by the cluster manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void addMarkerToCluster(MarkerModel marker){
        if(marker != null) {
            mClusterManager.addItem(new MarkerClusterItem(marker.getIpAddress(), marker.getLatitude(), marker.getLongitude()));
            mMarkerBoundsBuilder.include(new LatLng(marker.getLatitude(), marker.getLongitude()));
        }
    }

    private void requestIpScan(String startIp, String endIp){

        mMarkerManager = MyMarkerManager.getInstance(new MyMarkerManager.MarkerRequestCompleteListener() {
            @Override
            public void onMarkerRequestComplete(boolean success) {
                if (success)
                    addMarkerToCluster(mMarkerManager.getMostRecentMarker());

                mNetworkProgressBar.setProgress(MarkerRequestService.getCurrProgressStatus());
            }

            @Override
            public void onAllMarkersRequested() {
                String[] markerRequest = getResources().getStringArray(R.array.toast_ip_success_result);
                Toast.makeText(MapsActivity.this, markerRequest[0]+ mMarkerManager.getMarkerModelCount()
                        + markerRequest[1] + MarkerRequestService.IP_TOTAL_COUNT + markerRequest[2], Toast.LENGTH_LONG).show();

                // Attempt to move the map to show all of our markers within our marker bounds (cluster and office)
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMarkerBoundsBuilder.build(), MARKER_CLUSTER_FIT_PADDING));

                LatLngBounds projection = mMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng mostRecentMarkerLocation = new LatLng(mMarkerManager.getMostRecentMarker().getLatitude()
                        , mMarkerManager.getMostRecentMarker().getLongitude());
                if(!projection.contains(OFFICE_COORDS) && !projection.contains(mostRecentMarkerLocation))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mostRecentMarkerLocation, MARKER_ZOOM));

                mNetworkProgressBar.setVisibility((View.GONE));
            }
        });

        mNetworkProgressBar.setVisibility(View.VISIBLE);

        Intent markerService = new Intent(MapsActivity.this, MarkerRequestService.class);
        markerService.putExtra(MarkerRequestService.START_IP_KEY, startIp);
        markerService.putExtra(MarkerRequestService.END_IP_KEY, endIp);
        startService(markerService);
    }

    private void drawRenderedOverlays(){
        if (TileOverlayManager.getInstance().getPolylineOverlayOptions() == null)
            TileOverlayManager.getInstance().initializePolylineOverlayOptions();

        if (TileOverlayManager.getInstance().getPolylineOverlayOptions() == null)
            Log.e(getClass().getSimpleName(), "Loading in our polyline options fails, null load result");
        else
            mMap.addTileOverlay(TileOverlayManager.getInstance().getPolylineOverlayOptions());
    }

    private void drawPolylines(int polylinePosition, PolylineOptions polylineOptions){
        if (mDrawType == DrawType.POLYLINE_OVERLAY && polylinePosition == PolylineManager.NUM_ROUTES - 1){
            if (TileOverlayManager.getInstance().getPolylineOverlayOptions() == null)
                TileOverlayManager.getInstance().initializePolylineOverlayOptions();

            if (TileOverlayManager.getInstance().getPolylineOverlayOptions() == null)
                Log.e(getClass().getSimpleName(), "Loading in our polyline options fails, null load result");
            else
                mMap.addTileOverlay(TileOverlayManager.getInstance().getPolylineOverlayOptions());

            finalizeMap();
        } else if (mDrawType == DrawType.POLYLINE_MAP) {
            if (polylineOptions != null && polylineOptions.isVisible())
                mMap.addPolyline(polylineOptions);
            if (polylinePosition == PolylineManager.NUM_ROUTES - 1)
                finalizeMap();
        } else {
            //TODO finish setting this up
            finalizeMap();
        }
    }

    private void finalizeMap() {
        MarkerOptions officeMarker = new MarkerOptions()
                .title(getString(R.string.marker_title_trans_loc))
                .snippet(getString(R.string.marker_label_trans_loc))
                .position(new LatLng(HOME_LOC_LAT, HOME_LOC_LNG))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(officeMarker);

        mMarkerBoundsBuilder = new LatLngBounds.Builder();
        mMarkerBoundsBuilder.include(officeMarker.getPosition());
        if(mDrawType == DrawType.MARKER)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OFFICE_COORDS, MARKER_ZOOM));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OFFICE_COORDS, POLYLINE_ZOOM));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        findViewById(R.id.progress_map).setVisibility(View.GONE);
    }
}
