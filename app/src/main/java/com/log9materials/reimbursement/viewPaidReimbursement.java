package com.log9materials.reimbursement;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class viewPaidReimbursement extends AppCompatActivity {

    // Firebase Database Variables
    FirebaseDatabase mDatabase;
    DatabaseReference mEmployeeRef, mSupervisorRef, mReimbursementRef, mLastReimbursementRef;

    String user_employee_id, subordinate_id, reimbursement_number, count;
    int i;

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
        setContentView(R.layout.activity_view_paid_reimbursement);

        // Firebase Database References
        mDatabase = FirebaseDatabase.getInstance();
        mEmployeeRef = mDatabase.getReference("Employee");
        mSupervisorRef = mDatabase.getReference("Supervisor");
        mReimbursementRef = mDatabase.getReference("Reimbursement");

        // Employee ID from Previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");

        // References of the views used in this activity
        getPendingReimbursementList = (ListView) findViewById(R.id.paid_reimbursement_list);
        getPendingReimbursementList.setVisibility(View.INVISIBLE);

        TaDaarrayAdapter = new CustomAdapter(this, R.layout.custom_list_style,
                pending_daily_allowance_list_employee_name, pending_daily_allowance_list_employee_id, pending_daily_allowance_list_amount,
                pending_daily_allowance_list_date, pending_daily_allowance_list_image_url,
                pending_regular_list_reimbursement_id, pending_regular_list_count, pending_list_state1,
                "paid");

        RegulararrayAdapter = new CustomAdapter(this, R.layout.custom_list_style,
                pending_regular_list_employee_name, pending_regular_list_employee_id, pending_regular_list_amount, pending_regular_list_date,
                pending_regular_list_image_url, pending_regular_list_reimbursement_id, pending_regular_list_count,
                pending_list_state2, "paid");
        getPendingReimbursementList.setAdapter(RegulararrayAdapter);

        getSummary(user_employee_id);
    }

    public void getSummary(final String user_employee_id){
        mSupervisorRef.child(user_employee_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    mReimbursementRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot reimburseSnapshot) {
                            for (i = 1; i <= Integer.valueOf((String.valueOf(dataSnapshot.child("count").getValue()))); i++) {
                                for (int j = 1; j <= 20; j++) {
                                    mReimbursementRef.child(String.valueOf(dataSnapshot.child(String.valueOf(i)).getValue()))
                                            .child(String.valueOf(j)).child("0")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot lastSnapshot) {
                                                    if (lastSnapshot.getValue() != null) {
                                                        if (String.valueOf(lastSnapshot.child("status").getValue()).equals("paid")) {
                                                            // Regular Reimbursement goes here

                                                            if (String.valueOf(lastSnapshot.child("da_or_reg").getValue()).equals("regular")) {
                                                                pending_regular_list_employee_id.add("");
                                                                pending_regular_list_employee_name.add(String.valueOf(lastSnapshot.child("employee_name").getValue()));
                                                                pending_regular_list_amount.add(String.valueOf(lastSnapshot.child("amount").getValue()));
                                                                pending_regular_list_date.add(String.valueOf(lastSnapshot.child("date").getValue()));
                                                                pending_regular_list_image_url.add(String.valueOf(lastSnapshot.child("img_url").getValue()));
                                                                pending_regular_list_reimbursement_id.add(String.valueOf(lastSnapshot.child("reimbursement_id").getValue()));
                                                                pending_regular_list_count.add("0");
                                                                pending_list_state2.add("regular");
                                                                RegulararrayAdapter.notifyDataSetChanged();
                                                            }
                                                            // Daily Allowance list will go here

                                                            else {
                                                                pending_daily_allowance_list_employee_id.add("");
                                                                pending_daily_allowance_list_employee_name.add(String.valueOf(lastSnapshot.child("employee_name").getValue()));
                                                                pending_daily_allowance_list_amount.add(String.valueOf(lastSnapshot.child("total_amount").getValue()));
                                                                pending_daily_allowance_list_date.add(String.valueOf(lastSnapshot.child("applied_on").getValue()));
                                                                pending_daily_allowance_list_reimbursement_id.add(String.valueOf(lastSnapshot.child("reimbursement_id").getValue()));
                                                                pending_daily_allowance_list_count.add(String.valueOf(lastSnapshot.child("count").getValue()));
                                                                pending_list_state1.add("daily_allowance");
                                                                TaDaarrayAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void show_daily_allowance(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        getPendingReimbursementList.setAdapter(TaDaarrayAdapter);
        getPendingReimbursementList.setVisibility(view.VISIBLE);
        if (pending_daily_allowance_list_employee_name.size()==0){
            Toast.makeText(this, "No TA/DA Paid Reimbursement to Show.", Toast.LENGTH_SHORT).show();
        }
    }

    public void show_regular(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        getPendingReimbursementList.setAdapter(RegulararrayAdapter);
        getPendingReimbursementList.setVisibility(view.VISIBLE);
        if (pending_regular_list_employee_name.size()==0){
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
        Intent intent = new Intent(this, ActAsAdmin.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }
}