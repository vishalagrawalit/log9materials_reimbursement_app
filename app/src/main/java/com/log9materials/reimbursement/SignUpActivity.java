package com.log9materials.reimbursement;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputEmployeeCode, inputEmployeePassword;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;

    FirebaseDatabase mDatabase;
    DatabaseReference mReference;
    ProgressDialog mProgress;

    private Button btnSignUp;
    String employee_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        mDatabase = FirebaseDatabase.getInstance();

        inputEmployeeCode = (EditText) findViewById(R.id.employeeCode);
        inputEmployeePassword = (EditText) findViewById(R.id.employeePassword);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress = new ProgressDialog(SignUpActivity.this);
                mProgress.setMax(100);
                mProgress.setMessage("Signing In....");
                mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                if(isNetworkAvailable())
                    mProgress.show();
                mProgress.setCancelable(false);

                mReference = mDatabase.getReference("Employee");

                mReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = 0;
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            String counter = String.valueOf(postSnapShot.child("login_status").getValue());
                            String id = String.valueOf(postSnapShot.child("employee_id").getValue());
                            employee_type= String.valueOf(postSnapShot.child("type").getValue());
                            //Toast.makeText(MainActivity.this,mEmployeeCode.getText().toString(),Toast.LENGTH_LONG).show();
                            if ("0".equals(counter) && id.equals(inputEmployeeCode.getText().toString().trim())) {
                                count = 1;
                                break;
                            } else if ("-1".equals(counter)) {
                                count = -1;
                                break;
                            }
                        }
                        if (count == 0) {
                            mProgress.dismiss();
                            Toast.makeText(SignUpActivity.this, "Please enter correct employee code", Toast.LENGTH_SHORT).show();
                        } else if (count == -1) {
                            mProgress.dismiss();
                            Toast.makeText(SignUpActivity.this, "Sorry, you're no longer allowed access", Toast.LENGTH_SHORT).show();
                        } else {
                            String password = inputEmployeePassword.getText().toString().trim();
                            String email = covertCodeToEmail(inputEmployeeCode.getText().toString());

                            if (TextUtils.isEmpty(email)) {
                                Toast.makeText(getApplicationContext(), "Enter Employee Code!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (TextUtils.isEmpty(password)) {
                                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (password.length() < 8) {
                                Toast.makeText(getApplicationContext(), "Password too short, enter minimum 8 characters!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            // Toast.makeText(ActivitySignUp.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                            if (!task.isSuccessful()) {
                                                mProgress.dismiss();
                                                Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(),
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                mProgress.dismiss();
                                                Toast.makeText(SignUpActivity.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                                                mReference = mDatabase.getReference("Employee").child(inputEmployeeCode.getText().toString());
                                                mReference.child("login_status").setValue("1");
                                                if (employee_type.equals("supervisor")){
                                                    Intent intent = new Intent(SignUpActivity.this, SupervisorProfile.class);
                                                    intent.putExtra("User_ID", inputEmployeeCode.getText().toString());
                                                    startActivity(intent);
                                                }
                                                else{
                                                    Intent intent = new Intent(SignUpActivity.this, EmployeeProfile.class);
                                                    intent.putExtra("User_ID", inputEmployeeCode.getText().toString());
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public String covertCodeToEmail(String s) {
        String email = s + "@gmail.com";
        return email;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
    }
}