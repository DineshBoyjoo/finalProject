package com.example.cropsurveillanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class configure_plant_parameters extends AppCompatActivity {
    private EditText minVal,maxVal;
    private Button upload;
    private Spinner params;
    private ProgressBar progressBar;
    private DatabaseReference boundsDatabase;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fireUser = auth.getCurrentUser();
    private HashMap<String,ParameterBounds> boundsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_plant_parameters);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Configure Plant Parameters");

        if(fireUser==null){
            Toast.makeText(getApplicationContext(),"Please login to continue",Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),login.class));
        }

        defineParameterViews();

        boundsDatabase = FirebaseDatabase.getInstance().getReference().child("plant_parameters");
        boundsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.INVISIBLE);
                boundsMap.clear();
                for(DataSnapshot shot : snapshot.getChildren()){
                    String entryName =  shot.getKey().toLowerCase();
                    if(entryName.equals("sensorarchive") || entryName.equals("sensor_data") ){ //Preventing wrong entries from being accessed.
                        Log.d("FBDN Records "," Forbidden entry name registered and ignored.");
                    }else {
                        try {
                            boundsMap.put(shot.getKey(),
                                    new ParameterBounds(
                                            shot.getKey(),
                                            (Integer.parseInt(String.valueOf(shot.child("lowerBound").getValue()))),
                                            (Integer.parseInt(String.valueOf(shot.child("upperBound").getValue())))
                                    )
                            );
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Something went wrong with trying to access entry : " + shot.getKey().toString(), Toast.LENGTH_LONG).show();
                            break;
                        }
                    }

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        params.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                ParameterBounds bound;

                switch(i){
                    case 0: //Humidity Selected
                        bound = boundsMap.get("humidity");
                        break;
                    case 1://Intruder Selected
                        bound = boundsMap.get("intruder");
                        break;
                    case 2://Moisture Selected
                        bound = boundsMap.get("moisture");
                        break;
                    case 3://Temperature Selected
                        bound = boundsMap.get("temp");
                        break;
                    default:
                        bound = new ParameterBounds("",0,0);
                        break;
                }
                try{
                    minVal.setText( String.valueOf(bound.getLowerBound()) );
                    maxVal.setText(String.valueOf(bound.getUpperBound()) );
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Something went wrong with displaying parameter threshold.",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(),settings.class));
                }


            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int fieldID = Integer.parseInt(String.valueOf(params.getSelectedItemId()));
                String field="";
                switch(fieldID){
                    case 0:
                        field="humidity";
                        break;
                    case 1:
                        field="intruder";
                        break;
                    case 2:
                        field="moisture";
                        break;
                    case 3:
                        field="temp";
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),"Cannot upload new sensor limits. Try again later",Toast.LENGTH_LONG).show();
                        break;
                }
                if((!field.equals("")) && (validateParameterForm())){
                    try{
                        boundsDatabase.child(field).setValue(
                                new ParameterBounds(
                                        String.valueOf(params.getSelectedItem()),
                                        Integer.parseInt(String.valueOf(minVal.getText())),
                                        Integer.parseInt(String.valueOf(maxVal.getText()))
                                )
                        );
                        Toast.makeText(getApplicationContext(),"Successfullys updated parameter limit for "+String.valueOf(params.getSelectedItem()),Toast.LENGTH_LONG).show();
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(),"Something went wrong with uploading sensor limit data.",Toast.LENGTH_LONG).show();
                        Log.d("Database",e.getMessage());
                    }
                }
            }
        });
    }
    private void ErrorToDisplayOnViews(EditText v, String error){
        v.requestFocus();
        v.setError(error);
    }
    private boolean validateParameterForm() {
        String min = String.valueOf(minVal.getText());
        String max = String.valueOf(maxVal.getText());
        if(min.equals("")){
            ErrorToDisplayOnViews(minVal,"Please enter a minimum value");
            return false;
        }
        if(max.equals("")){
            ErrorToDisplayOnViews(maxVal,"Please enter a maximum value");
            return false;
        }
        if(Float.parseFloat(min)>Float.parseFloat(max)){
            ErrorToDisplayOnViews(minVal,"Minimum value entered is larger than maximum value set");
            return false;
        }

        return true;

    }


    private void defineParameterViews(){
        minVal = (EditText) findViewById(R.id.edtMin);
        maxVal = (EditText) findViewById(R.id.edtMax);
        upload = (Button) findViewById(R.id.btn_submitBounds);
        params = (Spinner) findViewById(R.id.spinner_parameter);
        progressBar = (ProgressBar) findViewById(R.id.Progbar_bounds);

        //Populating field to prevent null exception if firebase could not fetch data.
        boundsMap.put("humidity",new ParameterBounds("",0,0));
        boundsMap.put("intruder",new ParameterBounds("",0,0));
        boundsMap.put("moisture",new ParameterBounds("",0,0));
        boundsMap.put("temp",new ParameterBounds("",0,0));


    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}