package com.log9materials.reimbursement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileReader;

public class SplashActivity extends AppCompatActivity {
    DatabaseReference mRef;
    FirebaseDatabase mDatabase;
    String empCode = "";
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mRef = FirebaseDatabase.getInstance().getReference("Employee");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser user) {
        if(user == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else {
            getType(String.valueOf(user.getEmail()));
        }
    }

    public void getType(String email) {
        for(int i=0; i<email.length(); i++) {
            if(email.charAt(i) != '@') {
                empCode += email.charAt(i);
            }
            else {
                break;
            }
        }

        Log.v("empCode", empCode);
        Log.v("Refer", String.valueOf(mRef.child(empCode).child("type")));

        mRef.child(empCode).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String type = (String) dataSnapshot.getValue();
                Log.v("Type in Splash", type);
                switchActivity(type, empCode);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void switchActivity(String type, String empCode) {
        if(type.equals("supervisor")) {
            Intent intent = new Intent(this, SupervisorProfile.class);
            intent.putExtra("User_ID", empCode);
            startActivity(intent);
        }
        else if(type.equals("employee")) {
            Intent intent = new Intent(this, EmployeeProfile.class);
            intent.putExtra("User_ID", empCode);
            startActivity(intent);
        }
    }
}
