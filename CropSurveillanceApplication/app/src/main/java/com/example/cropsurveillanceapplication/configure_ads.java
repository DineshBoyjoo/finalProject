package com.example.cropsurveillanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class configure_ads extends AppCompatActivity {
    private Spinner animalChoice;
    private CheckBox scarecrow,noise;
    private Button uploadBtn;
    private TextView info;
    private DatabaseReference adsConfigDB;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fireUser = auth.getCurrentUser();
    private HashMap<String, Map<Boolean,Boolean>> adsData = new HashMap<>(); //First boolean stores status for noise and second boolean stores status for scarecrow
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_ads);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if(fireUser==null){
            Toast.makeText(getApplicationContext(),"Please login to continue",Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),login.class));
        }

        getSupportActionBar().setTitle("Configure Animal Deterring System");
        adsConfigDB = FirebaseDatabase.getInstance().getReference("intruder_system");
        adsConfigDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adsData.clear();
                for(DataSnapshot shot : snapshot.getChildren()){
                    String animalName = shot.getKey();
                    Boolean noise = Boolean.parseBoolean(String.valueOf(shot.child("Noise").getValue()));
                    Boolean scarecrow = Boolean.parseBoolean(String.valueOf(shot.child("Scarecrow").getValue()));
                    Map<Boolean,Boolean> configADS = new HashMap<>();
                    configADS.put(noise,scarecrow);
                    adsData.put(animalName,configADS);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        defineADSViews();
        animalChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){ //No option is selected
                    scarecrow.setVisibility(View.INVISIBLE);
                    noise.setVisibility(View.INVISIBLE);
                    uploadBtn.setVisibility(View.INVISIBLE);
                    info.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Please select an intruder option",Toast.LENGTH_SHORT).show();
                }
                else{
                    scarecrow.setVisibility(View.VISIBLE);
                    noise.setVisibility(View.VISIBLE);
                    uploadBtn.setVisibility(View.VISIBLE);
                    info.setVisibility(View.VISIBLE);
                    info.setText("Please select a deterring action for "+animalChoice.getSelectedItem().toString());

                    //Clearing Checkboxes for next animal
                    scarecrow.setChecked(false);
                    noise.setChecked(false);
                }
                Map<Boolean,Boolean> dataInstance;
               switch(i){
                   case 1:
                        //Dog option section
                       dataInstance=adsData.get("Dog");
                       displayinCheckboxes(dataInstance);
                       break;
                   case 2:
                       //Monkey option section
                       dataInstance=adsData.get("Monkey");
                       displayinCheckboxes(dataInstance);
                       break;
                   case 3:
                       //Pig option section
                       dataInstance=adsData.get("Pig");
                       displayinCheckboxes(dataInstance);
                       break;
               }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Boolean> deteringConfig = new HashMap<>();
                deteringConfig.clear();
                if(scarecrow.isChecked()){
                    deteringConfig.put("Scarecrow" , true);
                }else{
                    deteringConfig.put("Scarecrow" , false);
                }

                if(noise.isChecked()){
                    deteringConfig.put("Noise" , true);
                }else{
                    deteringConfig.put("Noise" , false);
                }
                try{
                    adsConfigDB.child(animalChoice.getSelectedItem().toString()).setValue(deteringConfig);
                    Toast.makeText(getApplicationContext(),"Successfully updated the Animal deterring system",Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Cannot update the Animal deterring system at the moment. Please try later",Toast.LENGTH_LONG).show();
                    Log.d("Database",e.getMessage());
                }

            }
        });

    }

    private void displayinCheckboxes(Map<Boolean,Boolean> dataInstance){
        Boolean noiseBool = (Boolean) dataInstance.keySet().toArray()[0];
        Boolean scarecrowBool = (Boolean) dataInstance.get(noiseBool);

        if(noiseBool){
            noise.setChecked(true);
        }
        if(scarecrowBool){
            scarecrow.setChecked(true);
        }
    }
    private void defineADSViews(){
        animalChoice = (Spinner) findViewById(R.id.animalOptions);
        scarecrow = (CheckBox) findViewById(R.id.cb_scarecrow);;
        noise = (CheckBox) findViewById(R.id.cb_noise);;
        uploadBtn = (Button) findViewById(R.id.btn_updateADS);
        info = (TextView) findViewById(R.id.tv_adsInfo);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}