package com.example.cropsurveillanceapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/*
To do: Fetch data from firebase and populate the fields to prevent user from entering info again.
 */
public class configure_crop_details extends AppCompatActivity {
    private EditText plantName, plantDescription,startIrrigation,endIrrigation,sunlightHours;
    private Spinner irrigationType,sunControl;
    private Button submitBtn;
    private TextView irrigation,sun;
    private DatabaseReference cropDetailDB;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fireUser = auth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_crop_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Configure crop details");

        if(fireUser==null){
            Toast.makeText(getApplicationContext(),"Please login to continue",Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(),login.class));
        }

        cropDetailDB = FirebaseDatabase.getInstance().getReference("plant_profile");
        defineCropDetailsViews();

        //Arrived here


        irrigationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                submitBtn.setEnabled(true);
                //Hiding fields that are not related
                startIrrigation.setVisibility(View.INVISIBLE);
                endIrrigation.setVisibility(View.INVISIBLE);
                switch(i){
                    case 0:
                        Toast.makeText(getApplicationContext(),"Please select a type of irrigation.",Toast.LENGTH_SHORT).show();
                        irrigation.setText("Please make a selection.");
                        submitBtn.setEnabled(false);
                        break;
                    case 1:
                        irrigation.setText(getResources().getString(R.string.automaticIrrigation));
                        break;
                    case 2:
                        irrigation.setText(getResources().getString(R.string.scheduledIrrigation));
                        startIrrigation.setVisibility(View.VISIBLE);
                        endIrrigation.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {            }
        });

        sunControl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                submitBtn.setEnabled(true);
                sunlightHours.setVisibility(View.INVISIBLE);
                switch(i){
                    case 0:
                        Toast.makeText(getApplicationContext(),"Please select a type sunlight control.",Toast.LENGTH_SHORT).show();
                        sun.setText("Please make a selection.");
                        submitBtn.setEnabled(false);
                        break;
                    case 1:
                        //Toast.makeText(getApplicationContext(),"Automatic Irrigation Selected",Toast.LENGTH_SHORT).show();
                        sun.setText(getResources().getString(R.string.sunControlYes));
                        sunlightHours.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        //Toast.makeText(getApplicationContext(),"Scheduled Irrigation Selected",Toast.LENGTH_SHORT).show();
                        sun.setText(getResources().getString(R.string.sunControlNo));

                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateForm()){
                    Map<String,String> cropInfo = new HashMap<>();
                    try {
                        //Uploading the data in Firebase
                        cropInfo.put("name",plantName.getText().toString());
                        cropInfo.put("description",plantDescription.getText().toString());
                        cropInfo.put("SunlightExposure",sunlightHours.getText().toString());
                        cropInfo.put("SunLightControlType",sunControl.getSelectedItem().toString());
                        cropInfo.put("IrrigationType",irrigationType.getSelectedItem().toString());
                        cropInfo.put("IrrigationStartTime",startIrrigation.getText().toString());
                        cropInfo.put("IrrigationEndTime",endIrrigation.getText().toString());

                        cropDetailDB.setValue(cropInfo);
                        Toast.makeText(getApplicationContext(),"Successfully uploaded the new details!",Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),"Something went wrong with uploading new details!",Toast.LENGTH_LONG).show();
                        Log.d("ErrorSendingData",e.getMessage());
                    }
                }
            }
        });


    }

    private void defineCropDetailsViews() {
        plantName = (EditText) findViewById(R.id.edt_plantName);
        plantDescription = (EditText) findViewById(R.id.edt_plantDescription);
        irrigationType = (Spinner) findViewById(R.id.spinner_irrigationType);
        sunControl = (Spinner) findViewById(R.id.spinner_sunControl);
        startIrrigation = (EditText) findViewById(R.id.edt_irr_start);
        endIrrigation = (EditText) findViewById(R.id.edt_irr_end);
        sunlightHours = (EditText) findViewById(R.id.edt_sun_hrs);
        submitBtn = (Button) findViewById(R.id.btn_submitCropDetails);
        irrigation = (TextView) findViewById(R.id.tv_IrrigateInfo);
        sun = (TextView) findViewById(R.id.tv_sunControlInfo);



    }

    private void ErrorToDisplayOnViews(EditText v, String error){
        v.requestFocus();
        v.setError(error);
    }

    private Boolean validateForm() {

        //validating plant name field
        String plantname = (plantName.getText().toString());
        if(plantname.equals("")){
            ErrorToDisplayOnViews(plantName,"Please enter a plant name");
            return false;
        }

        //validating irrigation choices and values
        String irrigationID = (String.valueOf(irrigationType.getSelectedItemId()));
        if(irrigationID.equals("0")){
            irrigationType.requestFocus();

        }else if(irrigationID.equals("1")){
            startIrrigation.setText("");
            endIrrigation.setText("");
        }
        else if(irrigationID.equals("2")){
            String startTime = startIrrigation.getText().toString();
            String endTime = endIrrigation.getText().toString();


            if(startTime.equals("")){
                ErrorToDisplayOnViews(startIrrigation,"Please enter a starting time.");
                return false;
            }
            if(endTime.equals("")){
                ErrorToDisplayOnViews(endIrrigation,"Please enter an ending time.");
                return false;
            }

            //validating Irrigation hours
            int startHr,endHr, startMin,endMin;

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");

            try{
                Date startDate = dateFormat.parse(startTime);
                Date endDate = dateFormat.parse(endTime);
                startHr = Integer.parseInt(String.valueOf(startDate.getHours()));
                startMin = Integer.parseInt(String.valueOf(startDate.getMinutes()));
                endHr = Integer.parseInt(String.valueOf(endDate.getHours()));
                endMin = Integer.parseInt(String.valueOf(endDate.getMinutes()));

                if(startHr>endHr){
                    ErrorToDisplayOnViews(startIrrigation,"Irrigation end hour should be greater than start hour");
                    return false;
                }else if(startHr==endHr && startMin>=endMin){
                    ErrorToDisplayOnViews(startIrrigation,"Irrigation end minutes should be greater than start minutes");
                    return false;
                }






            }catch(Exception e){
                Log.d("cropSetVal","EXP: "+e.getMessage());
                Toast.makeText(getApplicationContext(), "Irrigation Time format is invalid!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

            //new code here
            String sunlightControlID = (String.valueOf(sunControl.getSelectedItemId()));
            if(sunlightControlID.equals("0")){
                sunControl.requestFocus();
            }
            else if(sunlightControlID.equals("1")){
                String sunHrs = sunlightHours.getText().toString();
                Log.d("SUNLIGHTTEST","Sunlight test: "+sunHrs);
                if(sunHrs.equals("")){
                    ErrorToDisplayOnViews(sunlightHours,"Please enter a value for sunlight exposure time");
                    return false;
                }
            }





        //validating sun control choices and values
        String sunControlID = (String.valueOf(sunControl.getSelectedItemId()));
        if(sunControlID.equals("0")){
            sunControl.requestFocus();

        }else if(sunControlID.equals("1") && !(String.valueOf(sunlightHours.getText())).equals("")){
            //validating Sun control hours
            int sunlightHrs = Integer.valueOf(String.valueOf(sunlightHours.getText()));
            if(!(sunlightHrs>0 && sunlightHrs <24)){
                ErrorToDisplayOnViews(sunlightHours,"Please enter a valid exposure time between 1 and 23 hours.");
                return false;
            }
            if(sunlightHours.getText().toString().equals(null)){
                ErrorToDisplayOnViews(sunlightHours,"Please enter an exposure time in hours.");
                return false;
            }
        }
        else if(sunControlID.equals("2")){ //Clearing edittext when sun control is disabled
            sunlightHours.setText("0");
        }


        //Validating Spinner choices
        if(irrigationType.getSelectedItemId()==0){
            irrigation.setText("Please make a selection.");
            submitBtn.setEnabled(false);
            return false;
        }
        if(sunControl.getSelectedItemId()==0){
            sun.setText("Please make a selection.");
            submitBtn.setEnabled(false);
            return false;
        }


        return true;
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}