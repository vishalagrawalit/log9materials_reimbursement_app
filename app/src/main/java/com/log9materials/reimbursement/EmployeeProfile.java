package com.log9materials.reimbursement;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class EmployeeProfile extends AppCompatActivity{

    private FirebaseDatabase mEmployeeDatabase;
    private DatabaseReference mEmployeeRef;
    private FirebaseAuth auth;

    String user_employee_id, number_of_reimbursement_applied, employee_name_for_other_activity,
            da_for_apply_for_daily_allowance, total_amount_applied;

    TextView employee_name, employee_id, employee_da, employee_account_number, employee_amount_applied,
    employee_amount_paid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);

        user_employee_id = getIntent().getStringExtra("User_ID");

        mEmployeeDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        mEmployeeRef = mEmployeeDatabase.getReference("Employee").child(user_employee_id);

        // Views variable from Activity
        employee_name = (TextView) findViewById(R.id.employee_name);
        employee_id = (TextView) findViewById(R.id.employee_id);
        employee_da = (TextView) findViewById(R.id.daily_allowance);
        employee_account_number = (TextView) findViewById(R.id.account_number);
        employee_amount_applied = (TextView) findViewById(R.id.amount_applied);
        employee_amount_paid = (TextView) findViewById(R.id.amount_paid);

        mEmployeeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> employee_details = (Map<String, String>) dataSnapshot.getValue();
                employee_name.setText(employee_details.get("employee_name"));
                employee_id.setText(employee_details.get("employee_id"));
                employee_da.setText(employee_details.get("daily_allowance"));
                employee_account_number.setText(employee_details.get("account_number"));
                employee_amount_applied.setText(employee_details.get("amount_applied"));
                employee_amount_paid.setText(employee_details.get("amount_paid"));
                number_of_reimbursement_applied = employee_details.get("reimbursement_count");

                // Variable used to get an employee name and use it later in other activity.
                employee_name_for_other_activity = employee_details.get("employee_name");
                da_for_apply_for_daily_allowance = employee_details.get("daily_allowance");
                total_amount_applied = employee_details.get("amount_applied");

                Log.v("Da", da_for_apply_for_daily_allowance);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // Apply for Daily Allowance
    public void ApplyforDailyAllowance(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Intent intent = new Intent(EmployeeProfile.this, ApplyForTaDa.class);
        intent.putExtra("type", "employee");
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        intent.putExtra("daily_allowance", da_for_apply_for_daily_allowance);
        /*
        intent.putExtra("employee_name", employee_name_for_other_activity);
        */
        startActivity(intent);
        // Toast.makeText(this, "Hello baby", Toast.LENGTH_SHORT).show();
    }

    // Apply for other reimbursement than Daily Allowance.
    public void ApplyforRegular(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Intent intent = new Intent(EmployeeProfile.this, ApplyForRegular.class);
        intent.putExtra("type", "employee");
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount",number_of_reimbursement_applied);
        intent.putExtra("employee_name", employee_name_for_other_activity);
        intent.putExtra("amount_applied", total_amount_applied);
        startActivity(intent);
    }

    public void sign_out(View view){
        auth.signOut();
        Intent intent = new Intent(EmployeeProfile.this, MainActivity.class);
        startActivity(intent);
    }

    public void PaidReimbursementList(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Intent intent = new Intent(EmployeeProfile.this, viewIndividualPaidReimbursement.class);
        intent.putExtra("type", "employee");
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        startActivity(intent);
    }

    public void PendingReimbursementList(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Intent intent = new Intent(EmployeeProfile.this, viewIndividualPendingReimbursement.class);
        intent.putExtra("type", "employee");
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).setNegativeButton("no", null).show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void reset(View view){
        Intent intent = new Intent(EmployeeProfile.this, ResetPasswordActivity.class);
        intent.putExtra("type", "employee");
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }
}
