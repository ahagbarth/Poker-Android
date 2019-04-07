package com.example.poker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.READ_CONTACTS;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    EditText editTextEmail, editTextPassword;
    Button loginButton;
   // ProgressBar progressBar;


    // UI references.
   // private View mProgressView;
   // private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);


        //THIS WILL BE MOVED AROUND
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);


        loginButton = findViewById(R.id.signInButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError("enter a valid email address");
                } else if (password.isEmpty() || password.length() < 6 || password.length() > 14) {
                    editTextPassword.setError("Password should be between 6 and 14 characters");
                } else {
                    signIn(email, password);
                }
            }
        });


        mAuth = FirebaseAuth.getInstance();

    }

    protected void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                            loggedIn();

                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!= null){
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
            loggedIn();

        }

    }

    public void loggedIn() {
        Intent loggedIn = new Intent(LoginActivity.this, MainPageActivity.class);
        startActivity(loggedIn);

    }


}

