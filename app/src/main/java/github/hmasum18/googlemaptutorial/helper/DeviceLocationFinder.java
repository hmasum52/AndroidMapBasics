package github.hmasum18.googlemaptutorial.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class DeviceLocationFinder {
    //to get the device location
    public final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    public static final String TAG = "DeviceLocationFinder->";
    private Activity activity;
    private boolean isPermissionGranted = false;
    private LatLng deviceLatLng = null;
    private OnDeviceLatLngFoundListener onDeviceLatLngFoundListener;

    public DeviceLocationFinder(Activity activity) {
        this.activity = activity;
        this.getLocationPermission();
    }

    public interface OnDeviceLatLngFoundListener{
        void onDeviceLatLngFound(LatLng latLng);
    }

    public void setOnDeviceLatLngFoundListener(OnDeviceLatLngFoundListener onDeviceLatLngFoundListener) {
        this.onDeviceLatLngFoundListener = onDeviceLatLngFoundListener;
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
    }

    public boolean isPermissionGranted() {
        return isPermissionGranted;
    }

    public void requestDeviceLocation() {
        // Construct a FusedLocationProviderClient.
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        /**
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (isPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getDeviceLocation onComplete: location found");
                            Location location = task.getResult();
                            if (location == null) {
                                Toast.makeText(activity.getApplicationContext(), "location is null", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(activity.getApplicationContext(), "location found\"", Toast.LENGTH_SHORT).show();
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            deviceLatLng = currentLocation;
                            onDeviceLatLngFoundListener.onDeviceLatLngFound(currentLocation);
                        } else {
                            Log.d(TAG, "getDeviceLocation onComplete: location not found");
                        }
                    }
                });
            }else {
                this.getLocationPermission();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public LatLng getDeviceLatLng() {
        return deviceLatLng;
    }

    /**
     * get the user permission to access device location
     */
    public void getLocationPermission() {
        boolean isGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (isGranted){
            isPermissionGranted = true;
        }else {
            //get the location permission from user
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

}
