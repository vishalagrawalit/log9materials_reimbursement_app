package com.log9materials.reimbursement;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewTaDaDetails extends AppCompatActivity {

    FirebaseDatabase mDatabase;
    DatabaseReference mReimbursementRef;

    ListView ta_da_list;

    String user_employee_id, number_of_reimbursement_applied, type_of_reimbursement
            ,intercitySummary, localSummary, accommodationSummary, dailyAllowanceSummary
            , count, reimburse_id;

    CustomAdapterSummary arrayAdapter;

    int i, total_amount=0;

    ArrayList<String> ta_da_summary = new ArrayList<>();
    ArrayList<String> ta_da_pushId = new ArrayList<>();
    ArrayList<String> ta_da_type = new ArrayList<>();
    ArrayList<String> ta_da_url = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ta_da_details);

        // Employee ID from Previous Activity
        user_employee_id = getIntent().getStringExtra("id");
        reimburse_id = getIntent().getStringExtra("reid");

        ta_da_list = (ListView) findViewById(R.id.ta_da_applied_list);
        arrayAdapter = new CustomAdapterSummary(this, R.layout.custom_list_style_for_tada_summary,
                ta_da_summary, ta_da_pushId, ta_da_type, ta_da_url, user_employee_id, number_of_reimbursement_applied, "0", "details");
        ta_da_list.setAdapter(arrayAdapter);

        mDatabase = FirebaseDatabase.getInstance();
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id);

        mReimbursementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id = "";
                for (int i=5; i<reimburse_id.length(); i++){
                    id += reimburse_id.charAt(i);
                }

                Log.v("Reim", String.valueOf(mReimbursementRef.child(id).child("0").child("count")));
                count = (String.valueOf(dataSnapshot.child(id).child("0").child("count").getValue()));
                Log.v("Count", count);
                getSummary(count, id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void getSummary(final String count, final String id){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Log.v("mReim", String.valueOf(mReimbursementRef));
        mReimbursementRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (i = 1; i <= Integer.valueOf(count); i++) {
                        Log.v("Path", String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("type")));
                        type_of_reimbursement = (String) dataSnapshot.child(id).child(String.valueOf(i)).child("type").getValue();
                        if (type_of_reimbursement.equals("intercity")) {
                            total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue()));
                            intercitySummary = "Intercity-\nFrom- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("boarding_city").getValue())
                                    + "\nTo- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("destination_city").getValue())
                                    + "\nDate- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_date").getValue())
                                    + "\nMode- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_mode").getValue())
                                    + "\nAmount- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue());

                            ta_da_pushId.add(String.valueOf(i));
                            ta_da_url.add(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("img_url").getValue()));
                            ta_da_type.add(type_of_reimbursement);
                            ta_da_summary.add(intercitySummary);
                            arrayAdapter.notifyDataSetChanged();
                        } else if (type_of_reimbursement.equals("local")) {
                            total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue()));
                            localSummary = "Local-\nFrom- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("boarding_location").getValue())
                                    + "\nTo- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("destination_location").getValue())
                                    + "\nDate- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_date").getValue())
                                    + "\nMode- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_mode").getValue())
                                    + "\nAmount- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue());

                            ta_da_pushId.add(String.valueOf(i));
                            ta_da_url.add(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("img_url").getValue()));
                            ta_da_type.add(type_of_reimbursement);
                            ta_da_summary.add(localSummary);
                            arrayAdapter.notifyDataSetChanged();
                        } else if (type_of_reimbursement.equals("accommodation")) {
                            total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue()));
                            accommodationSummary = "Accommodation-\nCity- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("city").getValue())
                                    + "\nClass of City- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("city_class").getValue())
                                    + "\nDate- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_date").getValue())
                                    + "\nAmount- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue());

                            ta_da_pushId.add(String.valueOf(i));
                            ta_da_url.add(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("img_url").getValue()));
                            ta_da_type.add(type_of_reimbursement);
                            ta_da_summary.add(accommodationSummary);
                            arrayAdapter.notifyDataSetChanged();
                        } else {
                            total_amount += Integer.valueOf(String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue()));
                            dailyAllowanceSummary = "Daily Allowance-\nCity- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("city").getValue())
                                    + "\nClass of Employee- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("employee_class").getValue())
                                    + "\nFrom- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_from_date").getValue())
                                    + "\nTo- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_to_date").getValue())
                                    + "\nAmount- " + String.valueOf(dataSnapshot.child(id).child(String.valueOf(i)).child("journey_amount").getValue());

                            ta_da_pushId.add(String.valueOf(i));
                            ta_da_url.add("");
                            ta_da_type.add(type_of_reimbursement);
                            ta_da_summary.add(dailyAllowanceSummary);
                            arrayAdapter.notifyDataSetChanged();
                        }
                        Log.v("yes", "yeah");
                    }
                    Log.v("URL list", String.valueOf(ta_da_url));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
