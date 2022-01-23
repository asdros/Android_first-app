package com.example.e_learning_restaurants;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class AddRestaurantActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map2;

    String gps_lat ="";
    String gps_long ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);

        Button btn_AddRestaurantSubmit = findViewById(R.id.btn_AddRestaurantSubmit);
        EditText et_RestaurantName = findViewById(R.id.et_RestaurantName);
        EditText et_RestaurantPhone = findViewById(R.id.et_RestaurantPhone);

        SharedPreferences preferences = getSharedPreferences("userPreferences", Activity.MODE_PRIVATE);
        String userId = preferences.getString("userId", "0");
        TextView tv_Position = findViewById(R.id.tv_Position);

        String locationProvider = LocationManager.GPS_PROVIDER;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

            btn_AddRestaurantSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = et_RestaurantName.getText().toString();
                    String phone = et_RestaurantPhone.getText().toString();

                    String url = "http://dev.imagit.pl/mobilne/api/restaurant/add";

                    RequestParams requestParams = new RequestParams();
                    requestParams.put("user", userId);
                    requestParams.put("name", name);
                    requestParams.put("phone", phone);
                    requestParams.put("lat", gps_lat);
                    requestParams.put("long", gps_long);

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(url, requestParams, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);

                            if (response.equals("OK")) {
                                Toast.makeText(AddRestaurantActivity.this, R.string.restaurantAdded, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(AddRestaurantActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(AddRestaurantActivity.this, R.string.errorAPI, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                }
            });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map2 = googleMap;
        LatLng centerOfPoland = new LatLng(52.069115917394974, 19.48057401749022);
        map2.moveCamera(CameraUpdateFactory.newLatLngZoom(centerOfPoland,Float.parseFloat("5.5")));
        TextView tv_Position = findViewById(R.id.tv_Position);


        map2.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
               // markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                gps_lat=""+latLng.latitude;
                gps_long=""+latLng.longitude;

                tv_Position.setText(gps_lat + " " + gps_long);
                map2.clear();
                map2.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
                map2.addMarker(markerOptions);
            }
        });
    }
}