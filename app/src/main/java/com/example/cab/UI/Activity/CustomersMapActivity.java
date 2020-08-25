package com.example.cab.UI.Activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cab.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity extends FragmentActivity {
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    private Button CustomerLogoutButton , CustomerSettingButton , CustomerCall ;
    private FirebaseAuth mAuth ;
    private FirebaseUser currentUser ;
    private String CustomerId ;
    private DatabaseReference CustomreDataBaseref ;
    Location LastLocation ;
    GoogleMap mMap ;
    private boolean driverFound = false , requestType =false;
    private String DriverFoundID ;
    private int radius =1;
     LatLng CustomerPickUpLocation  ;
     private DatabaseReference DriverAvailableref ;
     private DatabaseReference Driverref ;
     private DatabaseReference DriverLocationref ;
     private Marker DriverMarker ,pickUpMarker ;
     private ValueEventListener DriverLocationRefListener ;
     private GeoQuery geoQuery ;

//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);

        initatVariables();
        checkpermission();
        CustomerLogoutListener();
        CustomerSettingListener();
        CustomerCallListener();


    }

    private void CustomerSettingListener() {
        CustomerSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , SettingsActivity.class);
                intent.putExtra("type","Customer");
                startActivity(intent);
            }
        });
    }

    private void CustomerCallListener() {

        CustomerCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (requestType){
                    requestType=false ;
                    geoQuery.removeAllListeners();
                    DriverLocationref.removeEventListener(DriverLocationRefListener);

                    if (driverFound != false)                    {

                        Driverref = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(DriverFoundID).child("CustomerRideID");

                        Driverref.removeValue();

                        DriverFoundID=null;

                    }
                    driverFound=false;
                    radius = 1 ;
                    GeoFire geoFire = new GeoFire(CustomreDataBaseref);
                    geoFire.removeLocation(CustomerId);

                    if (pickUpMarker != null){
                        pickUpMarker.remove();
                    }
                    if (DriverMarker != null){
                        DriverMarker.remove();
                    }
                    CustomerCall.setText(getResources().getString(R.string.call));







                    }
                else
                {
                    requestType=true;
                    GeoFire geoFire = new GeoFire(CustomreDataBaseref);
                    geoFire.setLocation(CustomerId, new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                System.out.println("There was an error saving the location to GeoFire: " + error);
                            } else {
                                System.out.println("Location saved on server successfully!");
                            }
                        }
                    });
                    CustomerPickUpLocation=new LatLng(LastLocation.getLatitude(),LastLocation.getLongitude());
                   pickUpMarker =  mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location ").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    CustomerCall.setText(getResources().getString(R.string.gettingdriver));

                    getClosestDriver();
                }



            }
        });
    }

    private void getClosestDriver() {
        GeoFire geoFire = new GeoFire(DriverAvailableref);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude,CustomerPickUpLocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                if (!driverFound && requestType){
                    driverFound=true;
                    DriverFoundID=dataSnapshot.getKey();
                    Driverref =FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(DriverFoundID);
                    HashMap driverMap  = new HashMap();
                    driverMap.put("CustomerRideID",CustomerId);
                    Driverref.updateChildren(driverMap);

                    GettingDriverLocation();
                    CustomerCall.setText(getResources().getString(R.string.lookingFor));


                }




            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound){
                    radius  = radius + 1 ;
                    getClosestDriver();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation() {
        DriverLocationRefListener=DriverLocationref.child(DriverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && requestType){
                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double LocationLat=0 ;
                            double LocationLng=0;
                            CustomerCall.setText(getResources().getString(R.string.driverfound));

                            if (driverLocationMap.get(0) != null ){
                                LocationLat=Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if (driverLocationMap.get(1) != null ){
                                LocationLng=Double.parseDouble(driverLocationMap.get(1).toString());
                            }
                            LatLng DriverLatlng = new LatLng(LocationLat,LocationLng);
                            if (DriverMarker != null ){
                                DriverMarker.remove();
                            }
                            Location location1 = new Location("");
                            location1.setLatitude(CustomerPickUpLocation.latitude);
                            location1.setLongitude(CustomerPickUpLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatlng.latitude);
                            location2.setLongitude(DriverLatlng.longitude);

                            float Distance = location1.distanceTo(location2);
                            if (Distance < 90){
                                CustomerCall.setText(getResources().getString(R.string.reached));
                            }
                            else {
                                CustomerCall.setText("Driver Found " + String.valueOf(Distance));


                            }

                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatlng).title("Your Driver is Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));






                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void CustomerLogoutListener() {
        CustomerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),WelcomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initatVariables() {
        supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver);
        client= LocationServices.getFusedLocationProviderClient(this);
        CustomerLogoutButton = findViewById(R.id.customer_logout);
        CustomerCall=findViewById(R.id.customer_call);
        CustomerSettingButton=findViewById(R.id.customer_settings);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        CustomerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomreDataBaseref=FirebaseDatabase.getInstance().getReference().child("Customer Requests");
        DriverAvailableref=FirebaseDatabase.getInstance().getReference().child("Driver Requests");// the same name
        DriverLocationref=FirebaseDatabase.getInstance().getReference().child("Drivers Working");

    }



    private void checkpermission() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        }

    private void getCurrentLocation() {

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            MarkerOptions options = new MarkerOptions().position(latLng)
                                    .title("i am here");

                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                            googleMap.addMarker(options);

                            LastLocation = location ;
                            mMap = googleMap ;

                        }
                    });
                }

            }


        });


    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==44){
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromvector(Context context , int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight()
        ,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



}
