
package com.example.e_learning_restaurants;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

class Restaurant {
    String Name;
    String Langitude;
    String Longitude;
}

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    List<Marker> mMarkerArray = new ArrayList<>();
    ArrayList<Restaurant> restaurantList = new ArrayList<>();

    GoogleMap map;

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btn_AddRestaurant = findViewById(R.id.btn_AddRestaurant);

        final ListView lv_Restaurants = findViewById(R.id.lv_Restaurants);
        SharedPreferences preferences = getSharedPreferences("userPreferences", Activity.MODE_PRIVATE);
        String userId = preferences.getString("userId", "0");

        Map<Integer, Restaurant> mapa = new HashMap<>();

        final ArrayList<String> restaurantsList = new ArrayList<>();
        final ArrayAdapter<String> restaurantsAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, restaurantsList);
        lv_Restaurants.setAdapter(restaurantsAdapter);

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://dev.imagit.pl/mobilne/api/restaurants/" + userId;

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String JSON = new String(responseBody);
                try {
                    JSONArray jArray = new JSONArray(JSON);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jsonObject = jArray.getJSONObject(i);

                        String restaurantId = jsonObject.getString("RESTAURANT_ID");
                        String restaurantName = jsonObject.getString("RESTAURANT_NAME");
                        String restaurantPhone = jsonObject.getString("RESTAURANT_PHONE");

                        Restaurant restaurant = new Restaurant();
                        restaurant.Name = restaurantName;
                        restaurant.Langitude = jsonObject.getString("RESTAURANT_LAT");
                        restaurant.Longitude = jsonObject.getString("RESTAURANT_LONG");
                        mapa.put(i, restaurant);

                        if (isDouble(restaurant.Langitude) && isDouble(restaurant.Longitude)) {
                            restaurantList.add(restaurant);
                        }

                        restaurantsList.add(restaurantName + ", " + restaurantPhone);
                    }
                    onResume();

                    lv_Restaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            LatLng focus = new LatLng(Double.parseDouble(mapa.get(position).Langitude),
                                                        Double.parseDouble(mapa.get(position).Longitude));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(focus, Float.parseFloat("6.5")));

                            Marker marker = mMarkerArray.stream().filter(m->m.getPosition().latitude == Double.parseDouble(mapa.get(position).Langitude))
                                                                .findAny().orElse(null);
                            marker.showInfoWindow();
                        }
                    });

                    lv_Restaurants.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.confirmDelete)
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                    })
                                    .setPositiveButton("OK", (dialog, which) -> onClickElementOfList(mapa.get(position).Name));
                            alert.show();
                            return true;
                        }

                        public void onClickElementOfList(String restaurantId) {
                            String url = "http://dev.imagit.pl/mobilne/api/restaurant/delete/" + userId + "/" + restaurantId;

                            //AsyncHttpClient client = new AsyncHttpClient();
                            client.get(url, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    String response = new String(responseBody);
                                    if (response.equals("OK")) {
                                        Toast.makeText(MainActivity.this, R.string.infoDeletingSuccessful, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, R.string.errorDeleting, Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });
                        }
                    });
                    lv_Restaurants.setAdapter(restaurantsAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        btn_AddRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRestaurantActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        for (Restaurant element : restaurantList) {
            LatLng ele = new LatLng(Double.parseDouble(element.Langitude), Double.parseDouble(element.Longitude));
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ele)
                    .title(element.Name)
                    .icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360))));

            mMarkerArray.add(marker);
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng focus = new LatLng(52.069115917394974, 19.48057401749022);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(focus, Float.parseFloat("5.5")));
    }
}
