package com.log9materials.reimbursement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ApplyForTaDa extends AppCompatActivity {

    TextView journeyGuidelines;

    String user_employee_id, number_of_reimbursement_applied, da_for_apply_for_daily_allowance, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_ta_da);

        // Extra from previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        da_for_apply_for_daily_allowance = getIntent().getStringExtra("daily_allowance");
        type = getIntent().getStringExtra("type");

        // Increasing Value of the ReimbursementCount to generate new reimbursement ID.
        number_of_reimbursement_applied = String.valueOf(Integer.valueOf(number_of_reimbursement_applied)+1);

        journeyGuidelines = (TextView) findViewById(R.id.journeyGuidelinesTextView);
        journeyGuidelines.setText("Before proceeding further, do read the following guidelines: \n " +
                "Apply for Intercity, Local & Accomodation as many time as you want before applying for daily allowance, " +
                "as soon as you press daily allowance button you have no longer access to apply for the other three.\n" +
                "Back Pressing at any moment before submitting will delete all your progress so be careful \n" +
                "When you hit submit a summary will be generated of your TA/DA application and you can edit there if you want to.\n" +
                "You cannot edit or delete as soon as you hit Final Submit.\n" +
                "Your Reimbursement Id is - " + user_employee_id + number_of_reimbursement_applied);
    }

    public void journeyContinue(View view){
        Intent intent = new Intent(ApplyForTaDa.this, ApplyForIntercity.class);
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("type", type);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        intent.putExtra("daily_allowance", da_for_apply_for_daily_allowance);
        intent.putExtra("count", "0");
        startActivity(intent);
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
