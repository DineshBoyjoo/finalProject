package com.example.cropsurveillanceapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class parameter_graph extends AppCompatActivity {

    private TextView InfoBannergraph;
    private DatabaseReference firebaseDatabase, sensorArchive;
    private ProgressBar parameterGraph_spinner;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fireUser = auth.getCurrentUser();

    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter_graph);

        if(fireUser==null){
            Toast.makeText(getApplicationContext(),"Please login to continue",Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),login.class));
        }

        InfoBannergraph = findViewById(R.id.InfoBannergraph);
        parameterGraph_spinner = findViewById(R.id.parameterGraph_spinner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle infos = getIntent().getExtras();
        try{
            String graph = infos.getString("name").toString();

            configureGraph(graph);
            switch(graph){
                case "temp":
                    getSupportActionBar().setTitle("Graph for Temperature");
                    break;
                case "humidity":
                    getSupportActionBar().setTitle("Graph for Humidity");
                    break;
                case "soil_moisture":
                    getSupportActionBar().setTitle("Graph for Soil moisture");
                    break;
                case "intruder":
                    getSupportActionBar().setTitle("Intruder System details");
                    break;
                default:
                    getSupportActionBar().setTitle("Cannot display graph");
                    break;
            }

            firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("sensor_data/"+graph);
            firebaseDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String tip = snapshot.child("tip").getValue().toString();

                    InfoBannergraph.setText(tip);
                    parameterGraph_spinner.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });


        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Something went wrong with displaying the graph.",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }


    }

    private void configureGraph(String graph){

        sensorArchive = FirebaseDatabase.getInstance().getReference().child("sensorArchive/"+graph);
        sensorArchive.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Float> sensorValues = new ArrayList<Float>();
                try{
                    for(DataSnapshot shot : snapshot.getChildren()){
                        float extractedSensorReading = (Float.parseFloat(String.valueOf(shot.child("reading").getValue())));
                        sensorValues.add(extractedSensorReading); //Populating arraylist with sensor values

                    }

                }catch (Exception e){
                    Log.d("Exception",e.toString());
                }

                int highest = 0;
                for(int x=0;x<sensorValues.size();x++ ){
                    int val = Math.round((sensorValues.get(x)));
                    if(highest<val){
                        highest=val;
                    }
                }
                highest=highest+10; //Adding offset for graph



                Log.d("HIGH",""+highest);
                //Initialising the GraphView Object
                GraphView graphImg = findViewById(R.id.graph);
                graphImg.getViewport().setMinY(0);
                graphImg.getViewport().setMaxY(highest);
                graphImg.getViewport().setYAxisBoundsManual(true);
                series = new LineGraphSeries<>();


                for(int i=0;i<sensorValues.size();i++){
                    series.appendData(new DataPoint(i,sensorValues.get(i)),true, 100);
                }

                graphImg.addSeries(series);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}