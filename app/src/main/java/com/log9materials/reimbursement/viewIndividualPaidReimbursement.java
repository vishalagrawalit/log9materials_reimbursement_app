package com.log9materials.reimbursement;

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

import java.util.ArrayList;

public class viewIndividualPaidReimbursement extends AppCompatActivity {

    String user_employee_id, number_of_reimbursement_applied, type;
    int i;

    FirebaseDatabase mDatabase;
    DatabaseReference mEmployeeRef, mReimbursementRef;

    ListView getPendingReimbursementList;

    CustomAdapter TaDaarrayAdapter, RegulararrayAdapter;

    // List of all text want to show in activity
    ArrayList<String> pending_daily_allowance_list_employee_id = new ArrayList<>();
    ArrayList<String> pending_daily_allowance_list_employee_name = new ArrayList<>();
    ArrayList<String> pending_daily_allowance_list_amount = new ArrayList<>();
    ArrayList<String> pending_daily_allowance_list_date = new ArrayList<>();
    ArrayList<String> pending_daily_allowance_list_image_url = new ArrayList<>();
    ArrayList<String> pending_daily_allowance_list_reimbursement_id = new ArrayList<>();
    ArrayList<String> pending_regular_list_employee_id = new ArrayList<>();
    ArrayList<String> pending_regular_list_employee_name = new ArrayList<>();
    ArrayList<String> pending_regular_list_amount = new ArrayList<>();
    ArrayList<String> pending_regular_list_date = new ArrayList<>();
    ArrayList<String> pending_regular_list_image_url = new ArrayList<>();
    ArrayList<String> pending_regular_list_reimbursement_id = new ArrayList<>();
    ArrayList<String> pending_regular_list_count = new ArrayList<>();
    ArrayList<String> pending_daily_allowance_list_count = new ArrayList<>();
    ArrayList<String> pending_list_state1 = new ArrayList<>();
    ArrayList<String> pending_list_state2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_individual_paid_reimbursement);

        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        type = getIntent().getStringExtra("type");

        Log.v("number", number_of_reimbursement_applied);

        mDatabase = FirebaseDatabase.getInstance();
        // mEmployeeRef = mDatabase.getReference("Employee").child(user_employee_id);
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id);


        // References of the views used in this activity
        getPendingReimbursementList = (ListView) findViewById(R.id.paid_reimbursement_list);
        getPendingReimbursementList.setVisibility(View.INVISIBLE);

        TaDaarrayAdapter = new CustomAdapter(this, R.layout.custom_list_style,
                pending_daily_allowance_list_employee_name, pending_daily_allowance_list_employee_id, pending_daily_allowance_list_amount,
                pending_daily_allowance_list_date, pending_daily_allowance_list_image_url,
                pending_daily_allowance_list_reimbursement_id, pending_daily_allowance_list_count, pending_list_state1,
                "paid");
        getPendingReimbursementList.setAdapter(TaDaarrayAdapter);

        RegulararrayAdapter = new CustomAdapter(this, R.layout.custom_list_style,
                pending_regular_list_employee_name, pending_regular_list_employee_id, pending_regular_list_amount, pending_regular_list_date,
                pending_regular_list_image_url, pending_regular_list_reimbursement_id, pending_regular_list_count,
                pending_list_state2, "paid");
        getPendingReimbursementList.setAdapter(RegulararrayAdapter);

        getSummary(number_of_reimbursement_applied);
    }

    public void getSummary(String number_of_reimbursement_applied){
        for (i=1; i<=Integer.valueOf(number_of_reimbursement_applied); i++){
            mReimbursementRef.child(String.valueOf(i)).child("0").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot lastSnapshot) {
                    Log.v("mReim", String.valueOf(mReimbursementRef.child(String.valueOf(i)).child("0")));
                    Log.v("last", String.valueOf(lastSnapshot));
                    if (String.valueOf(lastSnapshot.child("status").getValue()).equals("paid")) {
                        // Regular Reimbursement goes here
                        if (String.valueOf(lastSnapshot.child("da_or_reg").getValue()).equals("regular")) {
                            Log.v("Paid", "regular");
                            pending_list_state2.add("regular");
                            pending_regular_list_employee_id.add(user_employee_id);
                            pending_regular_list_employee_name.add(String.valueOf(lastSnapshot.child("employee_name").getValue()));
                            pending_regular_list_amount.add(String.valueOf(lastSnapshot.child("amount").getValue()));
                            pending_regular_list_date.add(String.valueOf(lastSnapshot.child("date").getValue()));
                            pending_regular_list_image_url.add(String.valueOf(lastSnapshot.child("img_url").getValue()));
                            pending_regular_list_reimbursement_id.add(String.valueOf(lastSnapshot.child("reimbursement_id").getValue()));
                            pending_regular_list_count.add("0");

                        }
                        // Daily Allowance list will go here

                        else {
                            Log.v("Paid", "daily");
                            pending_list_state1.add("daily");
                            pending_daily_allowance_list_employee_id.add(user_employee_id);
                            pending_daily_allowance_list_employee_name.add(String.valueOf(lastSnapshot.child("employee_name").getValue()));
                            pending_daily_allowance_list_amount.add(String.valueOf(lastSnapshot.child("total_amount").getValue()));
                            pending_daily_allowance_list_date.add(String.valueOf(lastSnapshot.child("applied_on").getValue()));
                            pending_daily_allowance_list_reimbursement_id.add(String.valueOf(lastSnapshot.child("reimbursement_id").getValue()));
                            pending_daily_allowance_list_count.add(String.valueOf(lastSnapshot.child("count").getValue()));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void show_daily_allowance(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        getPendingReimbursementList.setVisibility(view.VISIBLE);
        getPendingReimbursementList.setAdapter(TaDaarrayAdapter);

        if (pending_daily_allowance_list_employee_id.size()==0){
            Toast.makeText(this, "No TA/DA Paid Reimbursement to Show.", Toast.LENGTH_SHORT).show();
        }
    }

    public void show_regular(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        getPendingReimbursementList.setVisibility(view.VISIBLE);
        getPendingReimbursementList.setAdapter(RegulararrayAdapter);

        if (pending_regular_list_employee_id.size()==0){
            Toast.makeText(this, "No Regular Paid Reimbursement to Show.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onBackPressed() {

        if (type.equals("supervisor")) {
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
}
