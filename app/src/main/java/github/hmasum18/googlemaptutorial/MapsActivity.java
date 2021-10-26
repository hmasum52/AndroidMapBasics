package github.hmasum18.googlemaptutorial;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;

import github.hmasum18.googlemaptutorial.api.GoogleMapsApi;
import github.hmasum18.googlemaptutorial.databinding.ActivityMapsBinding;
import github.hmasum18.googlemaptutorial.helper.DeviceLocationFinder;
import github.hmasum18.googlemaptutorial.helper.MapCustomizer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MapsActivity->";

    private ActivityMapsBinding mVB;

    private GoogleMap mMap;
    private LatLng deviceLatLng;
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
        mVB = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(mVB.getRoot());

        //getLocationPermission();
        deviceLocationFinder = new DeviceLocationFinder(this, this);
        initMap();

        mVB.locationInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || event.getAction() == KeyEvent.ACTION_DOWN
                || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    findAndLocateByLocationName();
                }
                return false;
            }
        });

    }

    public void initMap() {
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
        mMap = googleMap;
        deviceLocationFinder.requestDeviceLocation(new DeviceLocationFinder.OnDeviceLocationFoundListener() {
            @Override
            public void onDeviceLocationFound(LatLng latLng) {
                Log.d(TAG, "onDeviceLocationFound: latlng: "+latLng);
                updateLocationUI();
            }
        });

        mapCustomizer = new MapCustomizer(googleMap);

        //customize map style
        //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, rawMapStyles[3]));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        initListeners();
    }

    private void initListeners() {
        mVB.hospital.setOnClickListener(v -> {
            Toast.makeText(this, "Showing nearby hospitals...", Toast.LENGTH_SHORT).show();
            showNearByHospitals();
        });

        mVB.schools.setOnClickListener(v -> {
            Toast.makeText(this, "Showing nearby schools...", Toast.LENGTH_SHORT).show();
            showNearBySchools();
        });
    }


    private void showNearByHospitals(){
        deviceLocationFinder.requestDeviceLocation(latLng -> {
            makePlacesCall("hospital", latLng);
        });
    }

    private void showNearBySchools(){
        deviceLocationFinder.requestDeviceLocation(latLng -> {
            makePlacesCall("school", latLng);
        });
    }

    private void showNearByPoliceStations(){
        deviceLocationFinder.requestDeviceLocation(latLng -> {
            makePlacesCall("police station", latLng);
        });
    }

    private void makePlacesCall(String type, LatLng latLng){
        String location = latLng.latitude+","+latLng.longitude;
        Call<ResponseBody> call =  GoogleMapsApi.instance.placesService
                .fetchNearByPlaces(location,1500, type, BuildConfig.GOOGLE_MAP_WEB_API_KEY);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()&&response.body()!=null){
                    Log.d(TAG, "onResponse: "+response.body());
                }else{
                    Log.e(TAG, "onResponse: "+type+" fetching is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                Log.e(TAG, "onFailure: error fetching "+type, t);
            }
        });
    }


    /**
     * geo locate the search string that will be given in the edit text field
     */
    public void findAndLocateByLocationName() {
        Log.d(TAG, "findAndLocate");
        String locationString = mVB.locationInput.getText().toString();

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
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                // mMap.getUiSettings().setCompassEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                deviceLocationFinder.getLocationPermission();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
}