package com.example.cab.UI.Activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cab.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.util.List;
import java.util.Set;

public class DriversMapsActivity extends FragmentActivity {
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
     private  Button DriverLogoutButton , DriverSettingButton ;
     private FirebaseAuth mAuth ;
     private FirebaseUser currentUser ;
    private String DriverId  , CustomerID ="";
    private DatabaseReference DriverDataBaseref ;
    private DatabaseReference AssignCutomerref  , AssignCustomerPickUpref ;
    private GoogleMap mMap;
    private Marker PickUpMarker ;
    private ValueEventListener AssignCustomerPickUprefListener ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_maps);

        initatVariables();
        checkpermission();
        driverLogoutListener();
        driverSeetingListener();

    }

    private void driverSeetingListener() {
        DriverSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() ,SettingsActivity.class);
                intent.putExtra("type" , "Drivers");
                startActivity(intent);
            }
        });
    }

    private void driverLogoutListener() {
        DriverLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),WelcomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        getAssignCustomerRequest();
    }

    private void getAssignCustomerRequest() {

        AssignCutomerref=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(DriverId).child("CustomerRideID");
        AssignCutomerref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    CustomerID=dataSnapshot.getValue().toString();
                    getAssignCustomerPickupLocation();
                }
                else {
                    CustomerID = " ";
                    if (PickUpMarker != null){
                        PickUpMarker.remove();
                    }
                    if (AssignCustomerPickUprefListener != null){
                        AssignCustomerPickUpref.removeEventListener(AssignCustomerPickUprefListener);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getAssignCustomerPickupLocation() {
        AssignCustomerPickUpref = FirebaseDatabase.getInstance().getReference().child("Customer Request")
        .child(CustomerID).child("1");

        AssignCustomerPickUprefListener=AssignCustomerPickUpref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> CustomerLocationMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat=0 ;
                    double LocationLng=0;

                    if (CustomerLocationMap.get(0) != null ){
                        LocationLat=Double.parseDouble(CustomerLocationMap.get(0).toString());
                    }
                    if (CustomerLocationMap.get(1) != null ){
                        LocationLng=Double.parseDouble(CustomerLocationMap.get(1).toString());
                    }
                    LatLng DriverLatlng = new LatLng(LocationLat,LocationLng);
                   PickUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatlng).title(" Customer PickUp Location ").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    private void initatVariables() {
        supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        client=LocationServices.getFusedLocationProviderClient(this);
        DriverLogoutButton = findViewById(R.id.Driver_logout_btn);
        DriverSettingButton=findViewById(R.id.Driver_setting_btn);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        DriverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DriverDataBaseref=FirebaseDatabase.getInstance().getReference().child("Driver Requests");
    }

    private void checkpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void getCurrentLocation() {
        if (getApplicationContext() != null){
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

                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                                googleMap.addMarker(options);
                                mMap = googleMap;

                                GeoFire geoFire = new GeoFire(DriverDataBaseref);


                                DatabaseReference DriverWorkingref = FirebaseDatabase.getInstance().getReference().child("Driver Working");
                                GeoFire geoFireWorking = new GeoFire(DriverWorkingref);

                                switch (CustomerID){
                                    case "":
                                        geoFireWorking.removeLocation(DriverId);
                                        geoFire.setLocation(DriverId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                        break;
                                        default:
                                            geoFire.removeLocation(DriverId);
                                            geoFireWorking.setLocation(DriverId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                            break;




                                }






                            }
                        });
                    }

                }
            });
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==44){
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }




}
