package github.hmasum18.googlemaptutorial;

import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import github.hmasum18.googlemaptutorial.helper.DeviceLocationFinder;
import github.hmasum18.googlemaptutorial.helper.MapCustomizer;
import github.hmasum18.googlemaptutorial.util.AnimationUtils;
import github.hmasum18.googlemaptutorial.util.MapUtils;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MapsActivity->";
    //  private EditText searchLocationEDT;

    private GoogleMap mMap;
    private DeviceLocationFinder deviceLocationFinder;
    private MapCustomizer mapCustomizer;

    //bound the camera in this latLng
    private LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
    private boolean isCircleDrawn = false;

    //different map styles
    int[] rawMapStyles = {R.raw.dark_map, R.raw.night_map, R.raw.aubergine_map, R.raw.assassins_creed_map};

    //to show satellite
    private LatLng satPrevLatLng;
    private LatLng satCurLatLng;
    private Marker movingSatelliteMarker;
    private int idx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //getLocationPermission();
        deviceLocationFinder = new DeviceLocationFinder(this);
        initMap();

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

    public void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
        mMap = googleMap;
        mapCustomizer = new MapCustomizer(googleMap);

        //customize map style
        //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, rawMapStyles[3]));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Turn on the My Location layer and the related control on the map.
        // updateLocationUI();


        deviceLocationFinder.requestDeviceLocation();
        deviceLocationFinder.setOnDeviceLatLngFoundListener(new DeviceLocationFinder.OnDeviceLatLngFoundListener() {
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
        });
        //addCircle(deviceLatLng,1000);

        final ArrayList<LatLng> locations = getArrayOfDummyLocations();

        /*//after delaying 3 sec implements run
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // showPath(locations);
                showMovingSatellite(locations);
            }
        },2000);*/
    }

    public void animateCamera(GoogleMap map, LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15.5f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void animateCameraWithBounds(LatLngBounds bounds) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
       /* mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.animateCamera(cameraUpdate);
            }
        });*/
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
            animateCameraWithBounds(latLngBuilder.build());
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


    public Polyline addLineBetweenTwoPoints(GoogleMap map, final LatLng from, final LatLng to) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(from);
        polylineOptions.add(to);
        polylineOptions.width(8f);
        polylineOptions.color(Color.RED);
        return map.addPolyline(polylineOptions);
    }

    public void animatePolyLineBetweenTwoPoints(final Polyline polyline, final LatLng from, final LatLng to) {
        ValueAnimator polylineAnimator = ValueAnimator.ofFloat((float) from.latitude, (float) to.latitude);
        polylineAnimator.setInterpolator(new LinearInterpolator());
        polylineAnimator.setDuration(8000);
        // polylineAnimator.setInterpolator(new LinearInterpolator());
        polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentLat = (float) animation.getAnimatedValue();
                double currentLng = ((currentLat - from.latitude) * (from.longitude - to.longitude)) / (from.latitude - to.latitude) + from.longitude;
                LatLng latLng = new LatLng(currentLat, currentLng);
                List<LatLng> list = new ArrayList<>();
                list.add(from);
                list.add(latLng);
                polyline.setPoints(list);
            }
        });
        polylineAnimator.start();
    }

    public void addCircle(LatLng latLng, int radiusInMeter) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radiusInMeter);
        circleOptions.fillColor(Color.parseColor("#72FFCCCB"));
        circleOptions.strokeColor(Color.RED);
        circleOptions.strokeWidth(2f);
        Circle circle = mMap.addCircle(circleOptions);
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

    private Marker addSatelliteAndGet(LatLng latLng) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getSatelliteBitmap(this));
        return mMap.addMarker(new MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor));
    }

    public Marker addOriginDestinationMarkerAndGet(LatLng latLng) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap());
        return mMap.addMarker(new MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor));
    }


    public void showPath(ArrayList<LatLng> latLngs) {
        //part 1
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();

        for (LatLng latLng : latLngs) {
            latLngBuilder.include(latLng);
        }

        // this is used to set the bound of the Map
        LatLngBounds latLngBounds = latLngBuilder.build();
        animateCameraWithBounds(latLngBounds);

        //now draw the polyLines
        PolylineOptions polylineOptions = new PolylineOptions();
        // polylineOptions.color(Color.GRAY);
        polylineOptions.color(Color.RED);
        polylineOptions.width(5f);
        polylineOptions.addAll(latLngs);
        final Polyline grayPloyLine = mMap.addPolyline(polylineOptions);

        PolylineOptions blackPolylineOptions = new PolylineOptions();
        blackPolylineOptions.color(Color.BLACK);
        blackPolylineOptions.width(5f);
        final Polyline blackPolyLine = mMap.addPolyline(blackPolylineOptions);

        //part 2
        //add markers in  origin  and destination
        Marker originMarker = addOriginDestinationMarkerAndGet(latLngs.get(0));
        originMarker.setAnchor(0.5f, 0.5f);

        Marker destinationMarker = addOriginDestinationMarkerAndGet(latLngs.get(latLngs.size() - 1));
        destinationMarker.setAnchor(0.5f, 0.5f);

        //part 3
        ValueAnimator polyLineAnimator = AnimationUtils.polyLineAnimator();
        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int percentValue = (int) animation.getAnimatedValue();
                int index = grayPloyLine.getPoints().size() * (int) (percentValue / 100.0f);
                Log.d(TAG, " grayPolyLine Size " + grayPloyLine.getPoints().size());
                Log.d(TAG, " point index " + index);
                blackPolyLine.setPoints(grayPloyLine.getPoints().subList(0, index));
            }
        });
        polyLineAnimator.start();
    }

    private void updateSatelliteLocation(LatLng latLng) {
        if (movingSatelliteMarker == null) {
            movingSatelliteMarker = addSatelliteAndGet(latLng);
        }

        if (satPrevLatLng == null) {
            satCurLatLng = latLng;
            satPrevLatLng = satCurLatLng;
            movingSatelliteMarker.setPosition(satCurLatLng);
            movingSatelliteMarker.setAnchor(0.5f, 0.5f);

            //animateCamera(latLng);
        } else {
            satPrevLatLng = satCurLatLng;
            satCurLatLng = latLng;
            movingSatelliteMarker.setPosition(satCurLatLng);
            movingSatelliteMarker.setAnchor(0.5f, 0.5f);

            ValueAnimator valueAnimator = AnimationUtils.satelliteAnimation();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (satCurLatLng != null && satPrevLatLng != null) {
                        double multiplier = animation.getAnimatedFraction();

                        //ease in the value
                        LatLng nextLocation = new LatLng(
                                multiplier * satCurLatLng.latitude + (1 - multiplier) * satPrevLatLng.latitude,
                                multiplier * satCurLatLng.longitude + (1 - multiplier) * satPrevLatLng.longitude
                        );

                        movingSatelliteMarker.setPosition(nextLocation);
                        movingSatelliteMarker.setAnchor(0.5f, 0.5f);
                        //animateCamera(nextLocation);
                        mapCustomizer.moveCamera(nextLocation, 0f);
                        addLineBetweenTwoPoints(mMap, satPrevLatLng, nextLocation);
                    }
                }
            });
            valueAnimator.start();
        }
    }

    public void showMovingSatellite(final ArrayList<LatLng> latLngs) {
        final Handler handler = new Handler();

        for (idx = 0; idx < latLngs.size(); idx++) {
            updateSatelliteLocation(latLngs.get(idx));
            /*handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateSatelliteLocation(latLngs.get(idx));
                }
            }, 1)*/
            ;
        }

        /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Welcome to BUET. Aibar mara khao",Toast.LENGTH_LONG).show();
            }
        },15000);*/
    }

    private ArrayList<LatLng> getArrayOfDummyLocations() {
        ArrayList<LatLng> locationList = new ArrayList<>();
        //locationList.add(deviceLatLng);
     /*   locationList.add(new LatLng(22.487241899999997,92.08613299999999)); // Rangunia
        locationList.add(new LatLng(23.7265081,90.39263989999999)); // BUET*/
        locationList.add(new LatLng(22.574578, -10.189307)); //Yeman
        locationList.add(new LatLng(17.614275, 121.984322)); //Luzon, Philippines
        return locationList;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        deviceLocationFinder.setPermissionGranted(false);
        switch (requestCode) {
            case DeviceLocationFinder.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deviceLocationFinder.setPermissionGranted(true);
                }
        }
    }
}