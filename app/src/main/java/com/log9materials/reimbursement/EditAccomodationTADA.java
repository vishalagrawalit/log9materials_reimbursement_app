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
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditAccomodationTADA extends AppCompatActivity{

    TextView journeyReimbursementId;
    EditText journeyCity, journeyDatePicked, journeyAmount;

    String user_employee_id, number_of_reimbursement_applied, ta_da_type, ta_da_pushId, selectedDate, count;
    int mYear, mMonth, mDay, DATE_DIALOG_ID = 1;

    FirebaseDatabase mDatabase;
    DatabaseReference mReimbursementRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_accomodation_tad);

        // Extra from previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        ta_da_pushId = getIntent().getStringExtra("pushId");
        ta_da_type = getIntent().getStringExtra("type");
        count = getIntent().getStringExtra("count");

        mDatabase = FirebaseDatabase.getInstance();
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id).child(number_of_reimbursement_applied).child(ta_da_pushId);

        // EditText and TextView from XML file
        journeyReimbursementId = (TextView) findViewById(R.id.journeyReimbursementIdTextView);
        journeyCity = (EditText) findViewById(R.id.journeyCityEditText);
        journeyDatePicked = (EditText) findViewById(R.id.journeyDatePickedEditText);
        journeyAmount = (EditText) findViewById(R.id.journeyAmountEditText);

        // Setting Text to Text View
        journeyReimbursementId.setText("Reimbursement ID - " + user_employee_id + number_of_reimbursement_applied);

        ta_da_pushId = String.valueOf(Integer.valueOf(ta_da_pushId)+1);
        mReimbursementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                journeyCity.setText(String.valueOf(dataSnapshot.child("city").getValue()));
                journeyDatePicked.setText(String.valueOf(dataSnapshot.child("journey_date").getValue()));
                journeyAmount.setText(String.valueOf(dataSnapshot.child("journey_amount").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // Run when Pick Journey Date is clicked
    public void journeyChooseDate(View view){
        showDialog(DATE_DIALOG_ID);
    }

    // Function for selecting Date
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            // date case
            case 1:
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
            journeyDatePicked.setText(String.valueOf(mDay)+"-"+String.valueOf(mMonth+1)+"-"+String.valueOf(mYear));
        }
    };

    public void journeySaveChanges(View view){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (TextUtils.isEmpty(journeyDatePicked.getText().toString()) || TextUtils.isEmpty(journeyCity.getText().toString())
                || TextUtils.isEmpty(journeyAmount.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mReimbursementRef.child("city").setValue(journeyCity.getText().toString());
        mReimbursementRef.child("journey_date").setValue(journeyDatePicked.getText().toString());
        mReimbursementRef.child("journey_amount").setValue(journeyAmount.getText().toString());

        Intent intent = new Intent(EditAccomodationTADA.this, FinalSubmitTaDa.class);
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        intent.putExtra("count", count);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
