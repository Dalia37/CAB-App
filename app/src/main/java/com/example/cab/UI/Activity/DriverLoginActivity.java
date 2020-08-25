package com.example.cab.UI.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cab.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {
    Button driver_login , driver_register ;
    TextView driverstate , driver_NoAccount ;
    EditText driver_Email , driver_password ;
    FirebaseAuth mAuth ;
    ProgressDialog loading ;
    private String onLineDriverID ;
    private DatabaseReference DriverDataBaseref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        intiatvariable();
        showRegister();
        driverButtonListener();
        diverSignInRegister();

    }



    private void diverSignInRegister() {
        driver_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emial = driver_Email.getText().toString();
                String password = driver_password.getText().toString();
                loginDriver(emial,password);


            }
        });
    }

    private void loginDriver(String emial, String password) {
        if (TextUtils.isEmpty(emial)){
            driver_Email.setError(getResources().getString(R.string.email));
        }
        if (TextUtils.isEmpty(password)){
            driver_password.setError(getResources().getString(R.string.password));
        }
        else {
            loading.setTitle(getResources().getString(R.string.driverlogin));
            loading.setMessage(getResources().getString(R.string.driverWait));
            loading.show();
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(emial, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(),DriversMapsActivity.class);
                                startActivity(intent);
                                Toast.makeText(DriverLoginActivity.this,getResources().getString(R.string.logindone), Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            } else
                                Toast.makeText(DriverLoginActivity.this,getResources().getString(R.string.loginfield), Toast.LENGTH_SHORT).show();
                            loading.dismiss();

                        }
                    });
        }



    }




    private void driverButtonListener() {
        driver_register.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String emial = driver_Email.getText().toString();
            String password = driver_password.getText().toString();
            registerDrivers(emial,password);
        }
    });
    }

    private void registerDrivers(String emial, String password) {
        if (TextUtils.isEmpty(emial)){
            driver_Email.setError(getResources().getString(R.string.email));
        }
        if (TextUtils.isEmpty(password)){
            driver_password.setError(getResources().getString(R.string.password));
        }
        else {
            loading.setTitle(getResources().getString(R.string.driverregister));
            loading.setMessage(getResources().getString(R.string.driverChecking));
            loading.show();
            mAuth.createUserWithEmailAndPassword(emial,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                onLineDriverID = mAuth.getCurrentUser().getUid();
                                DriverDataBaseref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                                        .child(onLineDriverID);
                                DriverDataBaseref.setValue(true);
                                Intent DriverIntent = new Intent(DriverLoginActivity.this,DriversMapsActivity.class);
                                startActivity(DriverIntent);
                                Toast.makeText(DriverLoginActivity.this, getResources().getString(R.string.registerdone), Toast.LENGTH_SHORT).show();
                                loading.dismiss();

                                Intent intent = new Intent(getApplicationContext(),DriversMapsActivity.class);
                                startActivity(intent);
                            }
                            else
                                Toast.makeText(DriverLoginActivity.this, getResources().getString(R.string.registerfield), Toast.LENGTH_SHORT).show();
                            loading.dismiss();

                        }
                    });

        }
    }


    private void showRegister() { driver_register.setVisibility(View.INVISIBLE);
        driver_NoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverstate.setText(getResources().getString(R.string.registration));
                driver_login.setVisibility(View.INVISIBLE);
                driver_NoAccount.setVisibility(View.INVISIBLE);
                driver_register.setVisibility(View.VISIBLE);
            }
        });

    }

    private void intiatvariable() {
        driver_login = findViewById(R.id.button);
        driver_register = findViewById(R.id.driver_register_btn);
        driverstate = findViewById(R.id.driver_tv);
        driver_NoAccount = findViewById(R.id.NoAccount_tv);
        driver_Email = findViewById(R.id.driver_email);
        driver_password=findViewById(R.id.driver_password);
        mAuth=FirebaseAuth.getInstance();
        loading=new ProgressDialog(this);

    }



}
