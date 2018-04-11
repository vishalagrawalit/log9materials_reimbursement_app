package com.log9materials.reimbursement;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class updateEmployeeProfile extends AppCompatActivity {

    EditText employee_id, employee_name, employee_daily_allowance, employee_account_number, employee_ifsc_code, employee_type, search_employee_id;

    TextView text_employee_id, text_employee_name, text_employee_daily_allowance, text_employee_account_number,
    text_employee_ifsc_code, text_employee_type;

    Button submit_button;

    String user_employee_id, employee_supervisor;

    FirebaseDatabase mDatabase;
    DatabaseReference mEmployeeRef, mSupervisorRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employee_profile);

        // Firebase Database
        mDatabase = FirebaseDatabase.getInstance();
        mEmployeeRef = mDatabase.getReference("Employee");

        // Edit Text References
        employee_id = (EditText) findViewById(R.id.add_employee_id);
        employee_name = (EditText) findViewById(R.id.add_employee_name);
        employee_daily_allowance = (EditText) findViewById(R.id.add_daily_allowance);
        employee_account_number = (EditText) findViewById(R.id.add_account_number);
        employee_ifsc_code = (EditText) findViewById(R.id.add_ifsc_code);
        employee_type = (EditText) findViewById(R.id.add_employee_type);
        search_employee_id = (EditText) findViewById(R.id.search_employee);


        // Text View References
        text_employee_id = (TextView) findViewById(R.id.employee_id);
        text_employee_name = (TextView) findViewById(R.id.employee_name);
        text_employee_daily_allowance = (TextView) findViewById(R.id.employee_daily_allowance);
        text_employee_account_number = (TextView) findViewById(R.id.employee_account_number);
        text_employee_ifsc_code = (TextView) findViewById(R.id.employee_ifsc_code);
        text_employee_type = (TextView) findViewById(R.id.employee_type);

        // Button References
        submit_button = (Button) findViewById(R.id.submit_button);

        // Setting Invisible TextView & EditText View
        employee_id.setVisibility(View.INVISIBLE);
        employee_name.setVisibility(View.INVISIBLE);
        employee_daily_allowance.setVisibility(View.INVISIBLE);
        employee_account_number.setVisibility(View.INVISIBLE);
        employee_ifsc_code.setVisibility(View.INVISIBLE);
        employee_type.setVisibility(View.INVISIBLE);

        text_employee_id.setVisibility(View.INVISIBLE);
        text_employee_name.setVisibility(View.INVISIBLE);
        text_employee_daily_allowance.setVisibility(View.INVISIBLE);
        text_employee_account_number.setVisibility(View.INVISIBLE);
        text_employee_ifsc_code.setVisibility(View.INVISIBLE);
        text_employee_type.setVisibility(View.INVISIBLE);

        submit_button.setVisibility(View.INVISIBLE);

        // User Id from Act as An Admin Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
    }

    public void update_employee_details(View view) {
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (TextUtils.isEmpty(employee_id.getText().toString()) || TextUtils.isEmpty(employee_name.getText().toString()) ||
                TextUtils.isEmpty(employee_daily_allowance.getText().toString()) || TextUtils.isEmpty(employee_account_number.getText().toString())
                || TextUtils.isEmpty(employee_ifsc_code.getText().toString())
                || TextUtils.isEmpty(employee_type.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mEmployeeRef = mEmployeeRef.child(employee_id.getText().toString());

        mEmployeeRef.child("employee_id").setValue(employee_id.getText().toString());
        mEmployeeRef.child("employee_name").setValue(employee_name.getText().toString());
        mEmployeeRef.child("daily_allowance").setValue(employee_daily_allowance.getText().toString());
        mEmployeeRef.child("account_number").setValue(employee_account_number.getText().toString());
        mEmployeeRef.child("ifsc_code").setValue(employee_ifsc_code.getText().toString());
        mEmployeeRef.child("type").setValue(employee_type.getText().toString());

    }

    public void search_for_employee(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mEmployeeRef = mDatabase.getReference("Employee");

        mSupervisorRef = mDatabase.getReference("Supervisor");
        mSupervisorRef.child(user_employee_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = String.valueOf(dataSnapshot.child("count").getValue());
                for (int i = 1; i <= Integer.valueOf(count); i++) {
                    String subordinate = String.valueOf(dataSnapshot.child(String.valueOf(i)).getValue());
                    if (subordinate.equals(search_employee_id.getText().toString())) {
                        // Visibility is On again
                        employee_id.setVisibility(View.VISIBLE);
                        employee_name.setVisibility(View.VISIBLE);
                        employee_daily_allowance.setVisibility(View.VISIBLE);
                        employee_account_number.setVisibility(View.VISIBLE);
                        employee_ifsc_code.setVisibility(View.VISIBLE);
                        employee_type.setVisibility(View.VISIBLE);

                        text_employee_id.setVisibility(View.VISIBLE);
                        text_employee_name.setVisibility(View.VISIBLE);
                        text_employee_daily_allowance.setVisibility(View.VISIBLE);
                        text_employee_account_number.setVisibility(View.VISIBLE);
                        text_employee_ifsc_code.setVisibility(View.VISIBLE);
                        text_employee_type.setVisibility(View.VISIBLE);

                        submit_button.setVisibility(View.VISIBLE);

                        mEmployeeRef.child(search_employee_id.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, String> employee_details = (Map<String, String>) dataSnapshot.getValue();
                                employee_id.setText(employee_details.get("employee_id"));
                                employee_name.setText(employee_details.get("employee_name"));
                                employee_daily_allowance.setText(employee_details.get("daily_allowance"));
                                employee_account_number.setText(employee_details.get("account_number"));
                                employee_ifsc_code.setText(employee_details.get("ifsc_code"));
                                employee_type.setText(employee_details.get("type"));

                                employee_supervisor = employee_details.get("supervisor");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    } else {
                        Toast.makeText(updateEmployeeProfile.this, "Unauthorized Access", Toast.LENGTH_SHORT).show();
                    }
                }
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
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActAsAdmin.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }
}