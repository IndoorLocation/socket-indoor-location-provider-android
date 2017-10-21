package io.indoorlocation.socket.demoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import io.indoorlocation.socketlocationprovider.SocketIndoorLocationProvider;
import io.mapwize.mapwizeformapbox.MapOptions;
import io.mapwize.mapwizeformapbox.MapwizePlugin;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private MapwizePlugin mapwizePlugin;
    private SocketIndoorLocationProvider socketIndoorLocationProvider;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1IjoibWFwd2l6ZSIsImEiOiJjamNhYnN6MjAwNW5pMnZvMnYzYTFpcWVxIn0.veTCqUipGXCw8NwM2ep1Xg");// PASTE YOU MAPBOX API KEY HERE !!! This is a demo key. It is not allowed to use it for production. The key might change at any time without notice. Get your key by signing up at mapbox.com

        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapwizePlugin = new MapwizePlugin(mapView, mapboxMap, new MapOptions());
                setupLocationProvider();
            }
        });

    }

    private void setupLocationProvider() {
        socketIndoorLocationProvider = new SocketIndoorLocationProvider(this, "YOUR_SERVER_SOCKET_ADDRESS");
        mapwizePlugin.setLocationProvider(socketIndoorLocationProvider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setupLocationProvider();

                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
