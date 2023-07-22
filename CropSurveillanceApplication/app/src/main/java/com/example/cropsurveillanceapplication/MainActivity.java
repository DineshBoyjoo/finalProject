package com.example.cropsurveillanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button logout,settings;
    private ProgressBar progressBar_loadFirebase;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fireUser = auth.getCurrentUser();
    private DatabaseReference sensorData_database;
    private  TextView emailHolder;
    private ArrayList<View> displayButtons = new ArrayList<>();
    private ArrayList<ParameterBounds> bounds = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(fireUser==null){
            Toast.makeText(getApplicationContext(),"Please login to continue",Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),login.class));
        }



        defineMainActivityViews();
        sensorData_database = FirebaseDatabase.getInstance().getReference().child("sensor_data");
        sensorData_database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                //Fetching sensor reading into arraylist
                try{
                    ArrayList<SensorElement> sensorCollection = getLatestSensorReadings(snapshot);
                    displaySensorReadingInButtons(sensorCollection);

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Cannot display sensor readings. Please try again later.",Toast.LENGTH_LONG).show();
                    progressBar_loadFirebase.setVisibility(View.INVISIBLE);
                    Log.d("SensorDataError","DisplayingSensorDataErr: "+e.getMessage());
                }

                //throw new RuntimeException("Test Crash"); // Force a crash to test Crashlytics


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FetchFirebaseDataWarning: ",error.getMessage());
            }

        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(),"Logged out successfully",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(),login.class);
                    startActivity(i);
                    finish();
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Something went wrong when trying to logout.",Toast.LENGTH_SHORT).show();
                    Log.d("LoginData","Logout Instance: "+e.getMessage());
                }

            }
        });

        settings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(getApplicationContext(), settings.class);
                startActivity(i);
            }
        });


    }

    private void getParameterBounds() {
        DatabaseReference parameterDatabase =  FirebaseDatabase.getInstance().getReference().child("plant_parameters");
        parameterDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Order is humidity (0) ,moisture (1) ,temp (2)
                bounds.clear(); //Clearing old data to prevent concatenations
                for(DataSnapshot shot : snapshot.getChildren()){
                    String entryName =  shot.getKey().toLowerCase();
                    Log.d("FBDN Records "," entry name "+entryName);
                    if(entryName.equals("sensorarchive") || entryName.equals("sensor_data") ){
                        //Preventing wrong entries from being accessed and created.
                        Log.d("FBDN Records "," Forbidden entry name registered and ignored.");
                    }else{

                        try{
                        Log.d("ERRORFIX",shot.getKey());
                            ParameterBounds bound = new ParameterBounds(
                                    shot.getKey(),
                                    Integer.parseInt(shot.child("lowerBound").getValue().toString()), //Getting the min field of the parameter
                                    Integer.parseInt(shot.child("upperBound").getValue().toString()) //Getting the max field of the parameter
                            );
                            bounds.add(bound);
                        }catch(Exception e){
                            Log.d("Database",e.getMessage());
                            Toast.makeText(getApplicationContext(),"Something went wrong with trying to access : "+shot.getKey().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bounds.add( new ParameterBounds("KEY",32,50));
            }
        });


    }

    private void defineMainActivityViews(){
        String emailInfo = "Logged in as:\n"+fireUser.getEmail().toString();
        emailHolder = (TextView) findViewById(R.id.tv_email);
        progressBar_loadFirebase = (ProgressBar) findViewById(R.id.progressBar_loadFirebase);
        settings = (Button) findViewById(R.id.btn_settings);
//        temp = (Button) findViewById(R.id.btn_temperature);
//        humidity = (Button) findViewById(R.id.btn_humidity);
//        moisture = (Button) findViewById(R.id.btn_soil_moisture);
//        intruder = (Button) findViewById(R.id.btn_intruder);

        try{
            emailHolder.setText(emailInfo);
        }catch(Exception e){ //User session is expired
            Log.d("SettingEmail",e.getMessage());
            emailHolder.setText(emailInfo);
        }

        logout = findViewById(R.id.btn_logout);

        getParameterBounds(); //Loading plant parameter information from database
        //Populating displayButtons ArrayList //Order for displayButtons is humidity,intrusion,moisture,temp
        displayButtons.add((Button) findViewById(R.id.btn_humidity));
        displayButtons.add((Button) findViewById(R.id.btn_intruder));
        displayButtons.add((Button) findViewById(R.id.btn_soil_moisture));
        displayButtons.add((Button) findViewById(R.id.btn_temperature));
    }

    private ArrayList<SensorElement> getLatestSensorReadings(DataSnapshot snapshot){
        Log.d("SNAPERROR",snapshot.toString());
        ArrayList<SensorElement> sensorCollections = new ArrayList<>();
        for(DataSnapshot shot : snapshot.getChildren()){

            SensorElement reading = new SensorElement(
                    shot.getKey(), //Getting the name of the parameter
                    shot.child("info").getValue().toString(), //Getting the info field of the parameter
                    shot.child("absolute_value").getValue().toString(), //Getting the absolute_value field of the parameter
                    shot.child("tip").getValue().toString(), //Getting the tip field of the parameter
                    shot.child("last_updated").getValue().toString() //Getting the last_updated field of the parameter
            );
            sensorCollections.add(reading);
        }

        return sensorCollections;
    }

    private void displaySensorReadingInButtons(ArrayList<SensorElement> sensorCollection) {
        //Order is humidity (0) ,intrusion (1) ,soil_moisture (2),temp (3)
        String recons_temp, recons_humidity, recons_intruder, recons_moisture;

        recons_humidity = String.format("Humidity: %s %%\n %s",sensorCollection.get(0).getAbsolute_value(),sensorCollection.get(0).getInfo());
        recons_intruder=String.format("Intruder: %s \n %s",sensorCollection.get(1).getAbsolute_value(),sensorCollection.get(1).getInfo());
        recons_moisture= String.format("Soil Moisture: %s \n %s",sensorCollection.get(2).getAbsolute_value(),sensorCollection.get(2).getInfo());
        recons_temp = String.format("Temperature: %s°C\n %s",sensorCollection.get(3).getAbsolute_value(),sensorCollection.get(3).getInfo());

        //Changing button colors based on absolute value. Order for bounds is humidity (0) ,intrusion (1) ,soil_moisture (2),temp (3)
        //Order for displayButtons is humidity,intrusion,moisture,temp
        for(int y=0;y<displayButtons.size();y++){
            ((Button) displayButtons.get(y)).setEnabled(true);
            ((Button) displayButtons.get(y)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            Log.d("SensorDataError","Bounds array size: "+bounds.size());
            float lowerbound = bounds.get(y).getLowerBound();
            float upperbound = bounds.get(y).getUpperBound();
            float currentSensorValue = Float.parseFloat(String.valueOf( (sensorCollection.get(y)).getAbsolute_value()));

            if(currentSensorValue>=lowerbound && currentSensorValue<=upperbound){
                //Sensor value is in limit
                ((Button) displayButtons.get(y)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.btn_inLimit));
            }else if(currentSensorValue>upperbound){
                //Sensor value is over limit
                ((Button) displayButtons.get(y)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.btn_overLimit));
            }else if(currentSensorValue<lowerbound){
                //Sensor value is under limit
                ((Button) displayButtons.get(y)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.btn_underLimit));
            }

        }
        ((Button) displayButtons.get(3)).setText(recons_temp); //temp.setText(recons_temp);
        ((Button) displayButtons.get(0)).setText(recons_humidity);//humidity.setText(recons_humidity);
        ((Button) displayButtons.get(2)).setText(recons_moisture);//moisture.setText(recons_moisture);
        ((Button) displayButtons.get(1)).setText(recons_intruder);//intruder.setText(recons_intruder);

        progressBar_loadFirebase.setVisibility(View.INVISIBLE);
    }

    public void generateGraph(View v){
        Intent i = new Intent(getApplicationContext(),parameter_graph.class);
        switch(v.getId()){
            case R.id.btn_temperature:
                i.putExtra("name","temp");
                break;
            case R.id.btn_humidity :
                i.putExtra("name","humidity");
                break;
            case R.id.btn_soil_moisture :
                i.putExtra("name","soil_moisture");
                break;
            case R.id.btn_intruder :
                i.putExtra("name","intruder");
                break;
            default:
                i.putExtra("name","");
                break;

        }
        startActivity(i);
    }






}