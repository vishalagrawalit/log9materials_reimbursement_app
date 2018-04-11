package com.log9materials.reimbursement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class FinalSubmitTaDa extends AppCompatActivity {

    ListView ta_da_list;
    TextView journeySummaryEmployeeName, journeySummaryEmployeeID, journeySummaryEmployeeAccountNumber,
            journeySummaryEmployeeIfscCode, journeySummaryReimbursementId,
            journeySummaryTotalAmount, journeySummaryAppliedOn;

    String user_employee_id, number_of_reimbursement_applied, type_of_reimbursement
            ,intercitySummary, localSummary, accommodationSummary, dailyAllowanceSummary
            , current_date= "", current_applied_amount, type, current_employee_type, count, current_employee_name;
    int total_amount=0, i;

    CustomAdapterSummary arrayAdapter;

    FirebaseDatabase mDatabase;
    DatabaseReference mEmployeeRef, mReimbursementRef;

    ArrayList<String> ta_da_summary = new ArrayList<>();
    ArrayList<String> ta_da_pushId = new ArrayList<>();
    ArrayList<String> ta_da_type = new ArrayList<>();
    ArrayList<String> ta_da_url = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_submit_ta_da);

        // Extra from previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        count = getIntent().getStringExtra("count");

        // Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mEmployeeRef = mDatabase.getReference("Employee").child(user_employee_id);
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id).child(number_of_reimbursement_applied);

        // Text VIew
        journeySummaryEmployeeName = (TextView) findViewById(R.id.journeySummaryEmployeeNameTextView);
        journeySummaryEmployeeID = (TextView) findViewById(R.id.journeySummaryEmployeeIDTextView);
        journeySummaryEmployeeAccountNumber = (TextView) findViewById(R.id.journeySummaryEmployeeAccountNumberTextView);
        journeySummaryEmployeeIfscCode = (TextView) findViewById(R.id.journeySummaryEmployeeIfscCodeTextView);
        journeySummaryReimbursementId = (TextView) findViewById(R.id.journeySummaryReimbursementIdTextView);
        journeySummaryTotalAmount = (TextView) findViewById(R.id.journeySummaryTotalAmountTextView);
        journeySummaryAppliedOn = (TextView) findViewById(R.id.journeySummaryAppliedOnTextView);

        ta_da_list = (ListView) findViewById(R.id.ta_da_applied_list);
        arrayAdapter = new CustomAdapterSummary(this, R.layout.custom_list_style_for_tada_summary,
                ta_da_summary, ta_da_pushId, ta_da_type, ta_da_url, user_employee_id, number_of_reimbursement_applied, count,
                "form");
        ta_da_list.setAdapter(arrayAdapter);

        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
                String date = getDate(number(estimatedServerTimeMs), "dd/MM/yyyy hh:mm:ss.SSS");
                for (int i=0; i<10; i++){
                    current_date += date.charAt(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

        mEmployeeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> employee_details = (Map<String, String>) dataSnapshot.getValue();
                journeySummaryEmployeeName.setText(employee_details.get("employee_name"));
                journeySummaryEmployeeID.setText(employee_details.get("employee_id"));
                journeySummaryEmployeeAccountNumber.setText(employee_details.get("account_number"));
                journeySummaryEmployeeIfscCode.setText(employee_details.get("ifsc_code"));
                journeySummaryReimbursementId.setText(user_employee_id + number_of_reimbursement_applied);
                journeySummaryTotalAmount.setText(String.valueOf(total_amount));
                journeySummaryAppliedOn.setText(current_date);

                current_employee_type = employee_details.get("type");
                current_applied_amount = employee_details.get("amount_applied");
                current_employee_name = employee_details.get("employee_name");
                current_applied_amount = String.valueOf(Integer.valueOf(current_applied_amount) + total_amount);
                mEmployeeRef.child("amount_applied").setValue(current_applied_amount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        for (i=1; i<=Integer.valueOf(count); i++){
            mReimbursementRef.child(String.valueOf(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    type_of_reimbursement = (String) dataSnapshot.child("type").getValue();
                    if (type_of_reimbursement.equals("intercity")) {
                        total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child("journey_amount").getValue()));
                        intercitySummary = "Intercity-\nFrom- " + String.valueOf(dataSnapshot.child("boarding_city").getValue())
                                + "\nTo- " + String.valueOf(dataSnapshot.child("destination_city").getValue())
                                + "\nDate- " + String.valueOf(dataSnapshot.child("journey_date").getValue())
                                + "\nMode- " + String.valueOf(dataSnapshot.child("journey_mode").getValue())
                                + "\nAmount- " + String.valueOf(dataSnapshot.child("journey_amount").getValue());

                        ta_da_pushId.add(String.valueOf(i));
                        ta_da_url.add(String.valueOf(dataSnapshot.child("img_url").getValue()));
                        ta_da_type.add(type_of_reimbursement);
                        ta_da_summary.add(intercitySummary);
                        arrayAdapter.notifyDataSetChanged();
                    }

                    else if (type_of_reimbursement.equals("local")){
                        total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child("journey_amount").getValue()));
                        localSummary = "Local-\nFrom- " + String.valueOf(dataSnapshot.child("boarding_city").getValue())
                                + "\nTo- " + String.valueOf(dataSnapshot.child("destination_city").getValue())
                                + "\nDate- " + String.valueOf(dataSnapshot.child("journey_date").getValue())
                                + "\nMode- " + String.valueOf(dataSnapshot.child("journey_mode").getValue())
                                + "\nAmount- " + String.valueOf(dataSnapshot.child("journey_amount").getValue());

                        ta_da_pushId.add(String.valueOf(i));
                        ta_da_type.add(type_of_reimbursement);
                        ta_da_url.add(String.valueOf(dataSnapshot.child("img_url").getValue()));
                        ta_da_summary.add(localSummary);
                        arrayAdapter.notifyDataSetChanged();
                    }
                    else if(type_of_reimbursement.equals("accommodation")){
                        total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child("journey_amount").getValue()));
                        accommodationSummary = "Accommodation-\nCity- " + String.valueOf(dataSnapshot.child("city").getValue())
                                + "\nClass of City- " + String.valueOf(dataSnapshot.child("city_class").getValue())
                                + "\nDate- " + String.valueOf(dataSnapshot.child("journey_date").getValue())
                                + "\nAmount- " + String.valueOf(dataSnapshot.child("journey_amount").getValue());

                        ta_da_pushId.add(String.valueOf(i));
                        ta_da_type.add(type_of_reimbursement);
                        ta_da_url.add(String.valueOf(dataSnapshot.child("img_url").getValue()));
                        ta_da_summary.add(accommodationSummary);
                        arrayAdapter.notifyDataSetChanged();
                    }
                    else{
                        total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child("journey_amount").getValue()));
                        dailyAllowanceSummary = "Daily Allowance-\nCity- " + String.valueOf(dataSnapshot.child("city").getValue())
                                + "\nClass of Employee- " + String.valueOf(dataSnapshot.child("employee_class").getValue())
                                + "\nFrom- " + String.valueOf(dataSnapshot.child("journey_from_date").getValue())
                                + "\nTo- " + String.valueOf(dataSnapshot.child("journey_to_date").getValue())
                                + "\nAmount- " + String.valueOf(dataSnapshot.child("journey_amount").getValue());

                        ta_da_pushId.add(String.valueOf(i));
                        ta_da_type.add(type_of_reimbursement);
                        ta_da_url.add("");
                        ta_da_summary.add(dailyAllowanceSummary);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    public long number(double time) {
        String strTime = String.valueOf(time);
        String strFinalTime = "";
        Log.d("Current time", strTime);
        for (int i=0; i<strTime.length(); i++) {
            if(strTime.charAt(i) == 'E')
                break;
            if(strTime.charAt(i) != '.')
                strFinalTime+=strTime.charAt(i);
        }
        Log.d("Current time", strFinalTime);
        return Long.valueOf(strFinalTime);
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public void finalSubmit(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mReimbursementRef.child("0").child("employee_name").setValue(current_employee_name);
        mReimbursementRef.child("0").child("da_or_reg").setValue("daily_allowance");
        mReimbursementRef.child("0").child("status").setValue("pending");
        mReimbursementRef.child("0").child("reimbursement_id").setValue(user_employee_id + number_of_reimbursement_applied);
        mReimbursementRef.child("0").child("applied_on").setValue(current_date);
        mReimbursementRef.child("0").child("total_amount").setValue(String.valueOf(total_amount));
        mReimbursementRef.child("0").child("count").setValue(count);
        mDatabase.getReference("Reimbursement").child(user_employee_id).child("count").setValue(number_of_reimbursement_applied);
        mEmployeeRef.child("reimbursement_count").setValue(number_of_reimbursement_applied);

        Toast.makeText(this, "Your TA/DA application has been submitted.", Toast.LENGTH_SHORT).show();
        switchActivity(current_employee_type);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id).child(number_of_reimbursement_applied);
        mReimbursementRef.removeValue();

        if (current_employee_type.equals("supervisor")) {
            Intent intent = new Intent(this, SupervisorProfile.class);
            intent.putExtra("User_ID", user_employee_id);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, EmployeeProfile.class);
            intent.putExtra("User_ID", user_employee_id);
            startActivity(intent);
        }
    }

    public void switchActivity (String current_employee_type) {
        if (current_employee_type.equals("supervisor")) {
            Intent intent = new Intent(FinalSubmitTaDa.this, SupervisorProfile.class);
            intent.putExtra("User_ID", user_employee_id);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(FinalSubmitTaDa.this, EmployeeProfile.class);
            intent.putExtra("User_ID", user_employee_id);
            startActivity(intent);
        }
    }
}
