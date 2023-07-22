package com.example.cropsurveillanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;


public class login extends AppCompatActivity {
    private Button login,register;
    private EditText email,password;
    private ProgressBar loginBar;
    private FirebaseAuth loginAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        email = findViewById(R.id.edtEmailLogin);
        password = findViewById(R.id.edtPasswordLogin);
        login = findViewById(R.id.btn_login);
        loginBar = findViewById(R.id.progressBar_login);
        loginAuth = FirebaseAuth.getInstance();
        register = (Button) findViewById(R.id.btn_register);

        try{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){ //User already logged in
                launchHomepage();
            }
            //Log.d("LoginDebug",loginAuth.toString());
        }catch (Exception e){
            Log.d("LoginDebug",e.getMessage());
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),register.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(validateLoginForm()){

                    //Email and password have been validated
                    String email_txt = email.getText().toString();
                    String password_txt = password.getText().toString();
                    try{
                        loginBar.setVisibility(View.VISIBLE);
                    }catch(Exception e){
                        loginBar = findViewById(R.id.progressBar_login);
                        loginBar.setVisibility(View.VISIBLE);
                    }

                    userLogin(email_txt,password_txt);
                }

            }
        });
    }

//Login validating password length and email pattern
    private Boolean validateLoginForm() {
        String email_txt = email.getText().toString();
        String password_txt = password.getText().toString();

        if(email_txt.isEmpty() || (!Patterns.EMAIL_ADDRESS.matcher(email_txt).matches()) ){
            email.setError("Please enter a valid email");
            email.requestFocus();
            return false;
        }
        if(password_txt.isEmpty() || password_txt.length()<6){
            password.setError("Please enter a valid password");
            password.requestFocus();

            return false;
        }
        return true;
    }

    private void userLogin(String email, String pwd) {
        loginAuth.signInWithEmailAndPassword(email,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                loginBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),"Login successful! ",Toast.LENGTH_LONG).show();
                login.setText("");
                password.setText("");

                launchHomepage();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginBar.setVisibility(View.INVISIBLE);

                try {
                    throw e;
                }catch (FirebaseAuthInvalidCredentialsException credentialsException) {
                    Toast.makeText(getApplicationContext(),"Wrong credentials used.",Toast.LENGTH_LONG).show();
                } catch (FirebaseAuthInvalidUserException invalidUserException) {
                    Toast.makeText(getApplicationContext(),"This user does not exist.",Toast.LENGTH_LONG).show();
                }
                catch(FirebaseNetworkException firebaseNetworkException){
                    Toast.makeText(getApplicationContext(),"Please connect to a stable network and try again.",Toast.LENGTH_LONG).show();
                }
                catch (Exception exception) {
                    Toast.makeText(getApplicationContext(),"Something went wrong with the login process. Please try later.",Toast.LENGTH_LONG).show();
                    Log.d("LoginData", "Login Debug: "+exception.getMessage());
                }
            }
        });
    }

    private void launchHomepage(){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

}