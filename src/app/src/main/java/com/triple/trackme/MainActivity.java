package com.triple.trackme;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.triple.trackme.CurrentTrack.CurrentTrackView;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnMapLoadedCallback, LocationListener {

    private GoogleMap map;
    private ProgressDialog progressDialog;
    private ImageView imgMyLocation;


    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        showMapLoadProgress();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        CurrentTrackView.initialize(this, (TextView) findViewById(R.id.timeVal),
                (TextView) findViewById(R.id.speedVal), (TextView) findViewById(R.id.spedVal),
                (ImageButton) findViewById(R.id.pauseTrackButton), (ImageButton) findViewById(R.id.startTrackButton));
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void showMapLoadProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Map Loading ...");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        GoogleMapService.settingMap(map, getSupportFragmentManager(), this);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onMapLoaded() {
        progressDialog.dismiss();

        int accessCoarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFinePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
            return;
        }
        showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                    Log.i("MapInfo", "Permission denied!");
                    this.showMyLocation();
                }
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                    Log.i("MapInfo", "Permission denied!");
                }
                break;
            }
        }
    }

    private void showMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider = GoogleMapService.getEnabledLocationProvider(locationManager, this);
        if (locationProvider == null) {
            return;
        }

        final long MIN_TIME_BW_UPDATES = 1000;
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
        Location myLocation = null;
        try {
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
            myLocation = locationManager.getLastKnownLocation(locationProvider);
        }
        catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.i("MapInfo", "Show My Location Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();
            Log.i("MapInfo", "Location not found");
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        CurrentTrackView.newLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }


    public void clickStartTrackButton(View view) {
        CurrentTrackView.start();
    }

    public void clickPauseTrackButton(View view) {
        CurrentTrackView.pause();
    }

    public void clickStopTrackButton(View view) {
        CurrentTrackView.stop();
    }

    public void clickProfileButton(View view) {
        Log.i("buttonClick", "Profile button click!!!");
    }

    public void setText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public void enableButton(final ImageButton button, final boolean enable){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

}
