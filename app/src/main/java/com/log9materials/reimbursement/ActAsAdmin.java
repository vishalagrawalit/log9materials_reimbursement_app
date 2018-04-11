package com.log9materials.reimbursement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ActAsAdmin extends AppCompatActivity {

    String user_employee_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_as_admin);

        user_employee_id = getIntent().getStringExtra("employee_id");
    }

    public void add_new_employee(View view){
        Intent intent = new Intent(this, addNewEmployee.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }

    public void view_pending(View view){
        Intent intent = new Intent(this, viewPendingReimbursement.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }

    public void view_paid(View view){
        Intent intent = new Intent(this, viewPaidReimbursement.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }

    public void update_employee_profile(View view){
        Intent intent = new Intent(this, updateEmployeeProfile.class);
        intent.putExtra("employee_id", user_employee_id);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SupervisorProfile.class);
        intent.putExtra("User_ID", user_employee_id);
        startActivity(intent);
        finish();
    }
}
