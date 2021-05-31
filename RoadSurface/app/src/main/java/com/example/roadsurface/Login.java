package com.example.roadsurface;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    TextView lmail;
    TextView lname;
    Button acs;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lmail = (TextView) findViewById(R.id.lmail);

        acs = (Button) findViewById(R.id.acs);
        mAuth = FirebaseAuth.getInstance();



        acs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AccelerometrMenu.class));
            }
        });
    }
        public void getUserProfile() {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                
                lmail.setText("Почта " + user.getEmail());
                // Check if user's email is verified
                boolean emailVerified = user.isEmailVerified();


            }

        }

    }

