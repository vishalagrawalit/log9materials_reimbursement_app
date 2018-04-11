package com.log9materials.reimbursement;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ApplyForDailyAllowance extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    TextView journeyReimbursementId, journeyAmount;
    EditText journeyCity, journeyFromDatePicked, journeyToDatePicked, journeyDays;

    // Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference mReimbursementRef;

    String user_employee_id, number_of_reimbursement_applied, da_for_apply_for_daily_allowance, count,
            selectedDate, item, type;
    int mDay, mMonth, mYear, DATE_DIALOG_ID=1,DATE_DIALOG_ID_2=2 ,flag, click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_daily_allowance);

        Log.v("Daily Allowance", "Open");
        // Extra from previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        da_for_apply_for_daily_allowance = getIntent().getStringExtra("daily_allowance");
        Log.v("da", da_for_apply_for_daily_allowance);
        count = getIntent().getStringExtra("count");
        count = String.valueOf(Integer.valueOf(count) + 1);
        type = getIntent().getStringExtra("type");

        // Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id);

        // EditText & TextView
        journeyReimbursementId = (TextView) findViewById(R.id.journeyReimbursementIdTextView);
        journeyAmount = (TextView) findViewById(R.id.journeyAmountEditText);
        journeyCity = (EditText) findViewById(R.id.journeyCityEditText);
        journeyFromDatePicked = (EditText) findViewById(R.id.journeyFromDatePickedEditText);
        journeyToDatePicked = (EditText) findViewById(R.id.journeyToDatePickedEditText);
        journeyDays = (EditText) findViewById(R.id.journeyDaysEditText);


        // Setting Text to Text View
        journeyReimbursementId.setText("Reimbursement ID - " + user_employee_id + number_of_reimbursement_applied);


        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.journeyEmployeeClass);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Automobile");
        categories.add("Business Services");
        categories.add("Computers");
        categories.add("Education");
        categories.add("Personal");
        categories.add("Travel");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        // On selecting a spinner item
        item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    public void onNothingSelected(AdapterView arg0) {
        // TODO Auto-generated method stub
    }

    // Run when Pick Journey Date is clicked
    public void journeyFromDate(View view){
        flag = 0;
        showDialog(DATE_DIALOG_ID);

    }

    public void journeyToDate(View view){
        flag = 1;
        showDialog(DATE_DIALOG_ID_2);
    }

    // Function for selecting Date
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            // date case
            case 1:
                click = 1;
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);

            case 2:
                click = 2;
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);

        }
        return null;
    }

    // Date Picker for picking the Date
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;

            // Typecasting Date into string and adding hypens"-"
            selectedDate = String.valueOf(mDay)+"-"+String.valueOf(mMonth+1)+"-"+String.valueOf(mYear);

            if (click == 1)
                journeyFromDatePicked.setText(selectedDate);
            else if (click == 2)
                journeyToDatePicked.setText(selectedDate);
        }
    };

    public void journeySubmit(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        updateDatabase();
        Intent intent = new Intent(ApplyForDailyAllowance.this, FinalSubmitTaDa.class);
        intent.putExtra("type", type);
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        intent.putExtra("count", count);
        startActivity(intent);
    }

    // Save the values into database before switching an activity.
    public void updateDatabase(){

        if (TextUtils.isEmpty(journeyCity.getText().toString()) || TextUtils.isEmpty(journeyFromDatePicked.getText().toString()) ||
                TextUtils.isEmpty(journeyToDatePicked.getText().toString()) || TextUtils.isEmpty(journeyDays.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mReimbursementRef = mReimbursementRef.child(number_of_reimbursement_applied).child(count);
        mReimbursementRef.child("type").setValue("daily_allowance");
        mReimbursementRef.child("city").setValue(journeyCity.getText().toString());
        mReimbursementRef.child("journey_from_date").setValue(journeyFromDatePicked.getText().toString());
        mReimbursementRef.child("journey_to_date").setValue(journeyToDatePicked.getText().toString());
        mReimbursementRef.child("journey_amount").setValue(String.valueOf(Integer.valueOf(da_for_apply_for_daily_allowance) * Integer.valueOf(journeyDays.getText().toString())));
        mReimbursementRef.child("employee_class").setValue(item);
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

        if (type.equals("supervisor")) {
            Intent intent = new Intent(ApplyForDailyAllowance.this, SupervisorProfile.class);
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
