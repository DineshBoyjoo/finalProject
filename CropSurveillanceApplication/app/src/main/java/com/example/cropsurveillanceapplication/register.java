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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class register extends AppCompatActivity {
    Button register;
    EditText email,password,confirmPwd;
    FirebaseAuth auth;
    ProgressBar progressBar_register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Register New user");

        defineRegisterViews();
        progressBar_register.setVisibility(View.INVISIBLE);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar_register.setVisibility(View.VISIBLE);
                if(validateRegister()){
                    String email_txt = email.getText().toString();
                    String password_txt = password.getText().toString();
                    auth.createUserWithEmailAndPassword(email_txt,password_txt).addOnCompleteListener(register.this,new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"User registered",Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(),login.class);
                                startActivity(i);
                                finish();

                            }else {
                                try{
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException e) { //Usercollision will check whether a user exists (Using same email address)
                                    Toast.makeText(getApplicationContext(),"The email used is already registered.",Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),"Something went wrong with registering this user.",Toast.LENGTH_LONG).show();
                                    Log.d("registerActivity",e.getMessage());
                                }
                            }
                            progressBar_register.setVisibility(View.INVISIBLE);
                        }
                    });
                }else{
                    progressBar_register.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private Boolean validateRegister(){
        String email_txt = email.getText().toString();
        String password_txt = password.getText().toString();
        String confirm_password_txt = confirmPwd.getText().toString();

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
        if(!(password_txt.equals(confirm_password_txt))){
            confirmPwd.setError("Password does not match");
            confirmPwd.requestFocus();
            return false;
        }
        return true;
    }

    private void defineRegisterViews() {
        register = (Button) findViewById(R.id.btn_registerUser);
        email = (EditText) findViewById(R.id.edtEmailReg);
        password = (EditText) findViewById(R.id.edtpwdRegister);
        confirmPwd = (EditText) findViewById(R.id.edtConfPwd);
        progressBar_register = (ProgressBar) findViewById(R.id.progressBar_register);
        auth = FirebaseAuth.getInstance();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}