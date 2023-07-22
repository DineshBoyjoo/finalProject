package com.example.cropsurveillanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class settings extends AppCompatActivity {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fireUser = auth.getCurrentUser();
    private TextView emailPlaceholder;
    private EditText pwd,confirmpwd;
    private Button changePwd,processChangePwd,configure_crop,configure_ads,hidepwd,plantparam;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(fireUser==null){
            Toast.makeText(getApplicationContext(),"Please login to continue",Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),login.class));
        }

        initialiseSettingViews();

        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pwd.setVisibility(View.VISIBLE);
                confirmpwd.setVisibility(View.VISIBLE);
                processChangePwd.setVisibility(View.VISIBLE);
                hidepwd.setVisibility(View.VISIBLE);

                configure_crop.setVisibility(View.INVISIBLE);
                configure_ads.setVisibility(View.INVISIBLE);
                changePwd.setVisibility(View.INVISIBLE);
                plantparam.setVisibility(View.INVISIBLE);

            }
        });

        hidepwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pwd.setVisibility(View.INVISIBLE);
                confirmpwd.setVisibility(View.INVISIBLE);
                processChangePwd.setVisibility(View.INVISIBLE);
                hidepwd.setVisibility(View.INVISIBLE);

                configure_crop.setVisibility(View.VISIBLE);
                configure_ads.setVisibility(View.VISIBLE);
                changePwd.setVisibility(View.VISIBLE);
                plantparam.setVisibility(View.VISIBLE);

            }
        });

        processChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validatePasswords()){
                    //logic to update password
                    try{
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        currentUser.updatePassword(confirmpwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"Password changed sucessfully!",Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(getApplicationContext(),"Please sign in again",Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(getApplicationContext(),login.class);
                                    startActivity(i);
                                    finish();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Password could not be changed! Please perform a recent login to change password",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }catch(Exception e){
                        Log.d("PWDCHANGE",e.getMessage());
                        Toast.makeText(getApplicationContext(),"Something went wrong when trying to change your password. Please try again later.",Toast.LENGTH_SHORT).show();
                    }



                }
            }
        });

        configure_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),configure_crop_details.class));
            }
        });

        configure_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),configure_ads.class));
            }
        });

        plantparam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),configure_plant_parameters.class));
            }
        });
    }

    private Boolean validatePasswords() {
        String passkey = pwd.getText().toString();
        String confirm_pwd = confirmpwd.getText().toString();

        if(pwd.length()<6){
            pwd.setError("Password length should be greater than 6");
            pwd.requestFocus();
            return false;
        }else if(!(passkey.equals(confirm_pwd))){
            //Both password does not match
            confirmpwd.setError("Password does not match");
            confirmpwd.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initialiseSettingViews(){
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        hidepwd = findViewById(R.id.btn_hidepwd);
        plantparam = (Button) findViewById(R.id.btn_configure_plantparam);
        changePwd = (Button) findViewById(R.id.btn_changepwd);
        processChangePwd = (Button) findViewById(R.id.btn_confirm_changepwd);
        configure_crop = (Button) findViewById(R.id.btn_configure_crops);
        configure_ads = (Button) findViewById(R.id.btn_configure_ads);


        emailPlaceholder = (TextView) findViewById(R.id.tv_settings_email);
        String emailInfo = "Logged in as:\n"+fireUser.getEmail().toString();
        emailPlaceholder.setText(emailInfo);

        pwd = (EditText) findViewById(R.id.edt_password);
        confirmpwd = (EditText) findViewById(R.id.edt_confirmpwd);
    }
}