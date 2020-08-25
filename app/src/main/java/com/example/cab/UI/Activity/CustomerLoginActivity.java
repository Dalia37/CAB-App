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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cab.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity {

    Button driver_login , driver_register ;
    TextView driverstate , driver_NoAccount ;
    EditText customer_email , customer_password ;
    FirebaseAuth mAuth ;
    ProgressDialog loading ;
    private DatabaseReference CustomerDataBaseref ;
    private String onLineCustomerID ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        initiateVariable();
        cutomerRegisterShow();
        customerRegisterListener();
        customerLogin();




    }

    private void customerLogin() {
        driver_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = customer_email.getText().toString();
                String password = customer_password.getText().toString();
                customerLogin(email,password);

            }
        });
    }

    private void customerLogin(String email, String password) {
        if (TextUtils.isEmpty(email)){
            customer_email.setError(getResources().getString(R.string.email));
        }
        if (TextUtils.isEmpty(password)){
            customer_password.setError(getResources().getString(R.string.password));
        }
        else {
            loading.setTitle(getResources().getString(R.string.customerlogin));
            loading.setMessage(getResources().getString(R.string.CoustomerWait));
            loading.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(),CustomersMapActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.logindone), Toast.LENGTH_SHORT).show();
                                loading.dismiss();

                            } else
                                Toast.makeText(CustomerLoginActivity.this, getResources().getString(R.string.logincustomerfield), Toast.LENGTH_SHORT).show();
                            loading.dismiss();


                        }
                    });
        }

    }

    private void customerRegisterListener() {
        driver_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = customer_email.getText().toString();
                String password = customer_password.getText().toString();
                registerCustomer(email,password);
            }
        });
    }

    private void registerCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)){
            customer_email.setError("enter email");
        }
        if (TextUtils.isEmpty(password)){
            customer_password.setError("enter Password");

        }
        else {
            loading.setTitle("Driver Registration");
            loading.setMessage("please Wait , while we are register your data");
            loading.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                onLineCustomerID = mAuth.getCurrentUser().getUid();
                                CustomerDataBaseref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer")
                                        .child(onLineCustomerID);

                                CustomerDataBaseref.setValue(true);
                                Intent driverIntenet = new Intent(CustomerLoginActivity.this,CustomersMapActivity.class);
                                startActivity(driverIntenet);

                                Toast.makeText(CustomerLoginActivity.this, "Customer Register Successfuly", Toast.LENGTH_SHORT).show();
                                loading.dismiss();

                            }
                            else
                                Toast.makeText(CustomerLoginActivity.this, "Registration Unsuccessfuly", Toast.LENGTH_SHORT).show();
                            loading.dismiss();

                        }
                    });
        }

    }

    private void cutomerRegisterShow() {

        driver_register.setVisibility(View.INVISIBLE);

        driver_NoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverstate.setText(getResources().getString(R.string.customer_regisration));
                driver_login.setVisibility(View.INVISIBLE);
                driver_NoAccount.setVisibility(View.INVISIBLE);
                driver_register.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initiateVariable() {
        driver_login=findViewById(R.id.button);
        driver_register=findViewById(R.id.customer_register);
        driverstate=findViewById(R.id.driver_tv);
        driver_NoAccount=findViewById(R.id.textView2);
        customer_email=findViewById(R.id.customer_email);
        customer_password=findViewById(R.id.customer_password);
        mAuth=FirebaseAuth.getInstance();
        loading=new ProgressDialog(this);


    }
}
