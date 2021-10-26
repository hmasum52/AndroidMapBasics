package github.hmasum18.googlemaptutorial.helper;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

import github.hmasum18.googlemaptutorial.util.MapUtils;

public class MapCustomizer {
    private static final String TAG = "MapCustomizer";
    private final GoogleMap map;

    public MapCustomizer(GoogleMap map) {
        this.map = map;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to: lat: " + latLng.latitude + " , lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void animateCamera(GoogleMap map, LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15.5f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void animateCameraWithBounds(List<LatLng> latLngList){
        if(latLngList == null || latLngList.size() == 0)
            return;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng :
                latLngList) {
            builder.include(latLng);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
    }

    public void animateCameraWithBounds(LatLngBounds bounds) {
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    public Polyline addLine(LatLng from, LatLng to) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(from);
        polylineOptions.add(to);
        polylineOptions.width(6f);
        polylineOptions.color(Color.RED);
        return map.addPolyline(polylineOptions);
    }

    public Polyline addLine(MapLine mapLine) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(mapLine.getFrom());
        polylineOptions.add(mapLine.getTo());
        polylineOptions.width(mapLine.getWidth());
        polylineOptions.color(mapLine.getColor());
        return map.addPolyline(polylineOptions);
    }

    public Polyline addLine(PolylineOptions polylineOptions) {
        return map.addPolyline(polylineOptions);
    }

    public void animatePolyLine(final Polyline polyline, final LatLng from, final LatLng to, long duration) {
        ValueAnimator polylineAnimator = ValueAnimator.ofFloat((float) from.latitude, (float) to.latitude);
        polylineAnimator.setInterpolator(new LinearInterpolator());
        polylineAnimator.setDuration(duration);
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

    public Circle addCircle(LatLng latLng, int radiusInMeter) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radiusInMeter);
        circleOptions.fillColor(Color.parseColor("#72FFCCCB"));
        circleOptions.strokeColor(Color.RED);
        circleOptions.strokeWidth(2f);
        return map.addCircle(circleOptions);
    }

    public Circle addCircle(MapCircle mapCircle) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(mapCircle.getCenter());
        circleOptions.radius(mapCircle.getRadius());
        circleOptions.fillColor(mapCircle.getFillColor());
        circleOptions.strokeColor(mapCircle.getFillColor());
        circleOptions.strokeWidth(mapCircle.getStrokeWidth());
        return map.addCircle(circleOptions);
    }

    public Circle addCircle(CircleOptions circleOptions) {
        return map.addCircle(circleOptions);
    }

    public Marker addMarker(LatLng latLng,String title){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);

        return map.addMarker(markerOptions);
    }

    public Marker addImageMarker(LatLng latLng, Bitmap bitmap){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.flat(true);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        return map.addMarker(markerOptions);
    }

    public Marker addImageMarker(MarkerOptions markerOptions){
         return map.addMarker(markerOptions);
    }

    public void showPath(ArrayList<LatLng> latLngList) {
        //part 1
        //camera will be bound to these lat lng
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngList) {
            latLngBuilder.include(latLng);
        }
        LatLngBounds latLngBounds = latLngBuilder.build();
        this.animateCameraWithBounds(latLngBounds);

        //now draw the polyLines
        PolylineOptions polylineOptions = new PolylineOptions();
        // polylineOptions.color(Color.GRAY);
        polylineOptions.color(Color.RED);
        polylineOptions.width(5f);
        polylineOptions.addAll(latLngList);
        final Polyline grayPloyLine = map.addPolyline(polylineOptions);

        PolylineOptions blackPolylineOptions = new PolylineOptions();
        blackPolylineOptions.color(Color.BLACK);
        blackPolylineOptions.width(5f);
        blackPolylineOptions.addAll(latLngList);
        final Polyline blackPolyLine = map.addPolyline(blackPolylineOptions);

        //part 2
        //add markers in  origin  and destination
        Marker originMarker = addOriginDestinationMarkerAndGet(latLngList.get(0));
        originMarker.setAnchor(0.5f, 0.5f);

        Marker destinationMarker = addOriginDestinationMarkerAndGet(latLngList.get(latLngList.size() - 1));
        destinationMarker.setAnchor(0.5f, 0.5f);

        //part 3
        //animate point
        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
        polyLineAnimator.setInterpolator(new LinearInterpolator());
        polyLineAnimator.setDuration(5000);
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

    private Marker addOriginDestinationMarkerAndGet(LatLng latLng) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap());
        return map.addMarker(new MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor));
    }

    static class MapLine {
        private LatLng from;
        private LatLng to;
        private float width;
        private int color;

        public MapLine(LatLng from, LatLng to, float width, int color) {
            this.from = from;
            this.to = to;
            this.width = width;
            this.color = color;
        }

        public LatLng getFrom() {
            return from;
        }

        public LatLng getTo() {
            return to;
        }

        public float getWidth() {
            return width;
        }

        public int getColor() {
            return color;
        }
    }

    static class MapCircle {
        private LatLng center;
        private double radius;
        private int fillColor; //hexa color code
        private int strokeColor;
        private float strokeWidth;

        public MapCircle(LatLng center, double radius, int fillColor, int strokeColor, float strokeWidth) {
            this.center = center;
            this.radius = radius;
            this.fillColor = fillColor;
            this.strokeColor = strokeColor;
            this.strokeWidth = strokeWidth;
        }

        public MapCircle(LatLng center, int radius) {
            this.center = center;
            this.radius = radius;
        }

        public LatLng getCenter() {
            return center;
        }

        public double getRadius() {
            return radius;
        }

        public int getFillColor() {
            return fillColor;
        }

        public int getStrokeColor() {
            return strokeColor;
        }

        public float getStrokeWidth() {
            return strokeWidth;
        }
    }
}
