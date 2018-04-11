package com.log9materials.reimbursement;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class addNewEmployee extends AppCompatActivity {

    EditText employee_id, employee_name, employee_daily_allowance, employee_account_number, employee_ifsc_code,
            employee_supervisor_id, employee_type;

    String user_employee_id, count;

    FirebaseDatabase mDatabase;
    DatabaseReference mEmployeeRef, mSupervisorRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_employee);

        // Firebase Database
        mDatabase = FirebaseDatabase.getInstance();
        mEmployeeRef = mDatabase.getReference("Employee");

        user_employee_id = getIntent().getStringExtra("employee_id");

        employee_id = (EditText) findViewById(R.id.add_employee_id);
        employee_name = (EditText) findViewById(R.id.add_employee_name);
        employee_daily_allowance = (EditText) findViewById(R.id.add_daily_allowance);
        employee_account_number = (EditText) findViewById(R.id.add_account_number);
        employee_ifsc_code = (EditText) findViewById(R.id.add_ifsc_code);
        employee_supervisor_id = (EditText) findViewById(R.id.add_supervisor_id);
        employee_type = (EditText) findViewById(R.id.add_employee_type);
    }

    public void submit_new_employee(View view) {

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (TextUtils.isEmpty(employee_id.getText().toString()) || TextUtils.isEmpty(employee_name.getText().toString()) ||
                TextUtils.isEmpty(employee_daily_allowance.getText().toString()) || TextUtils.isEmpty(employee_account_number.getText().toString())
                || TextUtils.isEmpty(employee_ifsc_code.getText().toString()) || TextUtils.isEmpty(employee_supervisor_id.getText().toString())
                || TextUtils.isEmpty(employee_type.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        String id = employee_id.getText().toString();
        if (id.length() != 3){
            Toast.makeText(this, "Please change the employee id.Employee Id must be of three letters only.", Toast.LENGTH_SHORT).show();
            return;
        }

        mEmployeeRef = mEmployeeRef.child(employee_id.getText().toString());
        mEmployeeRef.child("employee_id").setValue(employee_id.getText().toString());
        mEmployeeRef.child("employee_name").setValue(employee_name.getText().toString());
        mEmployeeRef.child("daily_allowance").setValue(employee_daily_allowance.getText().toString());
        mEmployeeRef.child("account_number").setValue(employee_account_number.getText().toString());
        mEmployeeRef.child("ifsc_code").setValue(employee_ifsc_code.getText().toString());
        mEmployeeRef.child("supervisor").setValue(employee_supervisor_id.getText().toString());
        mEmployeeRef.child("amount_applied").setValue("0");
        mEmployeeRef.child("amount_paid").setValue("0");
        mEmployeeRef.child("login_status").setValue("0");
        mEmployeeRef.child("reimbursement_count").setValue("0");
        mEmployeeRef.child("type").setValue(employee_type.getText().toString());

        mEmployeeRef = mDatabase.getReference("Employee");
        add_into_supervisors(employee_id.getText().toString(), employee_supervisor_id.getText().toString());
    }

    public void add_into_supervisors(final String empId, final String supId) {
        if (supId.equals("")) {
            Toast.makeText(this, "New Employee Added", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActAsAdmin.class);
            intent.putExtra("employee_id", user_employee_id);
            startActivity(intent);
        } else {
            mSupervisorRef = mDatabase.getReference("Supervisor");
            mSupervisorRef.child(supId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null) {
                        count = (String) dataSnapshot.child("count").getValue();
                        mSupervisorRef.child(supId).child(String.valueOf(Integer.valueOf(count) + 1)).setValue(empId);
                        mSupervisorRef.child(supId).child("count").setValue(Integer.valueOf(count) + 1);
                        getNextSupervisor(empId, supId);
                    }
                    else{
                        mSupervisorRef.child(supId).child("1").setValue(empId);
                        mSupervisorRef.child(supId).child("count").setValue("1");
                        getNextSupervisor(empId, supId);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getNextSupervisor(final String empId, String supId){
        mEmployeeRef.child(supId).child("supervisor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String supervisor_id = (String) dataSnapshot.getValue();
                add_into_supervisors(empId, supervisor_id);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActAsAdmin.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }
}
