package github.hmasum18.googlemaptutorial;

import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import github.hmasum18.googlemaptutorial.helper.DeviceLocationFinder;
import github.hmasum18.googlemaptutorial.helper.MapCustomizer;
import github.hmasum18.googlemaptutorial.satellite.SatelliteViewer;
import github.hmasum18.googlemaptutorial.util.AnimationUtils;
import github.hmasum18.googlemaptutorial.util.MapUtils;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MapsActivity->";
    //  private EditText searchLocationEDT;

    private GoogleMap mMap;
    private DeviceLocationFinder deviceLocationFinder;
    private MapCustomizer mapCustomizer;

    //bound the camera in this latLng
    private final LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
    private boolean isCircleDrawn = false;

    //different map styles
    int[] rawMapStyles = {R.raw.dark_map, R.raw.night_map, R.raw.aubergine_map, R.raw.assassins_creed_map};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //getLocationPermission();
        deviceLocationFinder = new DeviceLocationFinder(this);
        initStartMap();

        //searchLocationEDT = findViewById(R.id.location_input);
        /*searchLocationEDT.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || event.getAction() == KeyEvent.ACTION_DOWN
                || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    findAndLocate();
                }
                return false;
            }
        });*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        deviceLocationFinder.setPermissionGranted(false);
        switch (requestCode) {
            case DeviceLocationFinder.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deviceLocationFinder.setPermissionGranted(true);
                }
        }
    }

    public void initStartMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // load map in the fragment in an async task as it may pass UI thread
        mapFragment.getMapAsync(this);
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
        updateLocationUI();

        mMap = googleMap;
        mapCustomizer = new MapCustomizer(googleMap);

        //customize map style
        //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, rawMapStyles[3]));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        deviceLocationFinder.requestDeviceLocation();
       /* deviceLocationFinder.setOnDeviceLatLngFoundListener(new DeviceLocationFinder.OnDeviceLatLngFoundListener() {
            @Override
            public void onDeviceLatLngFound(LatLng latLng) {

                latLngBuilder.include(latLng);

                //move camera tha to device location
                mapCustomizer.moveCamera(latLng, 15);

                //add a marker to device location
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("myLocation");
                mMap.addMarker(markerOptions);

                //add a circle to device location
                mapCustomizer.addCircle(latLng,1000);
            }
        });*/


        //after delaying 3 sec implements run
        final ArrayList<LatLng> locationList = new ArrayList<>();
        //locationList.add(deviceLatLng);
     /*   locationList.add(new LatLng(22.487241899999997,92.08613299999999)); // Rangunia
        locationList.add(new LatLng(23.7265081,90.39263989999999)); // BUET*/
        locationList.add(new LatLng(22.574578, -10.189307)); //Yeman
        locationList.add(new LatLng(17.614275, 121.984322)); //Luzon, Philippines
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //mapCustomizer.showPath(locationList);
                //show a satellite
                SatelliteViewer satelliteViewer = new SatelliteViewer(MapsActivity.this,mapCustomizer);
                satelliteViewer.showMovingSatellite(locationList);
            }
        },2000);
    }


    /**
     * geo locate the search string that will be given in the edit text field
     */
    public void findAndLocateByLocationName() {
        Log.d(TAG, "findAndLocate");
        String locationString = "";//searchLocationEDT.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(locationString, 1);
        } catch (Exception e) {
            Log.w(TAG, "findAndLocate" + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.w(TAG, "findAndLocate:" + address.toString());

            LatLng destination = new LatLng(address.getLatitude(), address.getLongitude());

            //location is found now move the camera
            //moveCamera(deviceLatLng,15,address.getAddressLine(0));
            //add a polyLine from device location to the searched location
            latLngBuilder.include(destination);
            mapCustomizer.animateCameraWithBounds(latLngBuilder.build());
            //addLineBetweenTwoPoints(mMap, deviceLatLng, destination);
            if (!isCircleDrawn) {
                //addCircle(deviceLatLng, 25000);
                isCircleDrawn = true;
            }
            MarkerOptions markerOptions = new MarkerOptions().position(destination).title(address.getAddressLine(0));
            mMap.addMarker(markerOptions);
        } else {
            Log.w(TAG, "findAndLocate: no location found");
        }
    }


    public void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (deviceLocationFinder.isPermissionGranted()) {
                mMap.setMyLocationEnabled(true);
                // mMap.getUiSettings().setMyLocationButtonEnabled(true);
                // mMap.getUiSettings().setCompassEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                // mMap.getUiSettings().setMyLocationButtonEnabled(false);
                deviceLocationFinder.getLocationPermission();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
}