package github.hmasum18.googlemaptutorial.satellite;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import github.hmasum18.googlemaptutorial.helper.MapCustomizer;
import github.hmasum18.googlemaptutorial.util.AnimationUtils;
import github.hmasum18.googlemaptutorial.util.MapUtils;

public class SatelliteViewer {
    private Context context;
    private MapCustomizer mapCustomizer;
    //to show satellite
    private LatLng satPrevLatLng;
    private LatLng satCurLatLng;
    private Marker movingSatelliteMarker;
    private int idx = 0;

    public SatelliteViewer(Context context,MapCustomizer mapCustomizer) {
        this.mapCustomizer = mapCustomizer;
        this.context = context;
    }

    private Marker addSatelliteAndGet(LatLng latLng) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getSatelliteBitmap(context));
        return mapCustomizer.addImageMarker(new MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor));
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
                        mapCustomizer.addLine(satPrevLatLng, nextLocation);
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
}
