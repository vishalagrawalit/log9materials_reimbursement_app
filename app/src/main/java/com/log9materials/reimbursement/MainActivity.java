package com.log9materials.reimbursement;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {

    private EditText inputCode, inputPassword;
    private FirebaseAuth auth;
    private Button btnLogin;
    private TextView linkSignUp;
    String type, email, password;
    private int x = 0;

    FirebaseDatabase mDatabase;
    DatabaseReference mReference;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        inputCode = (EditText) findViewById(R.id.employeeCodeSignIn);
        inputPassword = (EditText) findViewById(R.id.passwordSignIn);
        linkSignUp = (TextView) findViewById(R.id.linkToSignUp);
        btnLogin = (Button) findViewById(R.id.btnSignIn);

        mDatabase = FirebaseDatabase.getInstance();

        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });

        mReference = mDatabase.getReference("Employee");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x = 1;

                email = covertCodeToEmail(inputCode.getText().toString());
                password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (x == 1) {
                    mReference = mReference.child(inputCode.getText().toString());
                    mReference.child("type").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            type = (String) dataSnapshot.getValue();
                            authenticate(type);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    public String covertCodeToEmail(String s) {
        String email = s + "@gmail.com";
        return email;
    }

    public void authenticate(final String type){
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMax(100);
        mProgress.setMessage("Logging In....");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(isNetworkAvailable())
            mProgress.show();
        mProgress.setCancelable(false);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.v("TYPE", type);
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 8) {
                                inputPassword.setError("Password too short");
                                mProgress.dismiss();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to Sign in", Toast.LENGTH_LONG).show();
                                mProgress.dismiss();
                            }
                        } else {
                            if (type.equals("supervisor")) {
                                mProgress.dismiss();
                                Intent intent = new Intent(MainActivity.this, SupervisorProfile.class);
                                intent.putExtra("User_ID", inputCode.getText().toString());
                                startActivity(intent);
                            } else if (type.equals("employee")) {
                                mProgress.dismiss();
                                Log.v("id", inputCode.getText().toString());
                                Intent intent = new Intent(MainActivity.this, EmployeeProfile.class);
                                intent.putExtra("User_ID", inputCode.getText().toString());
                                startActivity(intent);
                            }
                            finish();
                        }
                    }
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

}
