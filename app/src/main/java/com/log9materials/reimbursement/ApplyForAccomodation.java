package com.log9materials.reimbursement;

import android.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.log9materials.reimbursement.ApplyForIntercity.MY_PERMISSIONS_REQUEST_CAMERA;
import static com.log9materials.reimbursement.ApplyForIntercity.REQUEST_CAMERA;
import static com.log9materials.reimbursement.ApplyForIntercity.SELECT_FILE;

public class ApplyForAccomodation extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    TextView journeyReimbursementId;
    EditText journeyCity, journeyDatePicked, journeyAmount;

    String item, user_employee_id, number_of_reimbursement_applied, selectedDate,
            da_for_apply_for_daily_allowance, type, count;
    int mYear, mMonth, mDay, DATE_DIALOG_ID = 1;

    FirebaseDatabase mDatabase;
    DatabaseReference mReimbursementRef;

    // Image Handler
    StorageReference mStorageRef, imageRef;
    static final int SELECT_FILE=100, REQUEST_CAMERA=101, MY_PERMISSIONS_REQUEST_CAMERA=200;
    ProgressDialog mProgress;
    Uri selectedImage, imageUrl, image;
    File file;
    UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_accomodation);

        // Extra from previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        type = getIntent().getStringExtra("type");

        // Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        da_for_apply_for_daily_allowance = getIntent().getStringExtra("daily_allowance");
        count = getIntent().getStringExtra("count");
        count = String.valueOf(Integer.valueOf(count) + 1);

        // EditText and TextView from XML file
        journeyReimbursementId = (TextView) findViewById(R.id.journeyReimbursementIdTextView);
        journeyCity = (EditText) findViewById(R.id.journeyCityEditText);
        journeyDatePicked = (EditText) findViewById(R.id.journeyDatePickedEditText);
        journeyAmount = (EditText) findViewById(R.id.journeyAmountEditText);

        // Setting Text to Text View
        journeyReimbursementId.setText("Reimbursement ID" + user_employee_id + number_of_reimbursement_applied);


        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.journeyCityClass);

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
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

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

    // Execute when Upload Ticket is pressed
    // Execute when Upload Ticket is pressed
    public void journeyTicketUpload(View view){
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ApplyForAccomodation.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void cameraIntent() {
        int permissionCheck = ContextCompat.checkSelfPermission(ApplyForAccomodation.this,
                android.Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            file = new File(this.getExternalCacheDir(),
                    String.valueOf(System.currentTimeMillis()) + ".jpg");
            selectedImage = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
            this.startActivityForResult(intent, REQUEST_CAMERA);
        }
        else  {
            ActivityCompat.requestPermissions(ApplyForAccomodation.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(ApplyForAccomodation.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = new File(this.getExternalCacheDir(),
                            String.valueOf(System.currentTimeMillis()) + ".jpg");
                    selectedImage = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                    this.startActivityForResult(intent, REQUEST_CAMERA);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(ApplyForAccomodation.this, "Camera Permission Denied. You can't use camera for this task.", Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    public void galleryIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    Log.d("Image", "selected");
                    image = data.getData();
                    CropImage.activity(image).setGuidelines(CropImageView.Guidelines.ON).start(this);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(resultCode == RESULT_OK) {
                    selectedImage = result.getUri();
                } else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(ApplyForAccomodation.this, "An error occured.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(ApplyForAccomodation.this, "Image selected", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    Log.d("Image", "clicked");
                    CropImage.activity(selectedImage).setGuidelines(CropImageView.Guidelines.ON).start(this);
                }
        }
    }

    public void uploadImage(final String imageName,final Class<? extends Activity> xyz) {
        //create reference to images folder and assigning a name to the file that will be uploaded
        imageRef = mStorageRef.child("Photos/TADA/" + imageName);
        if (TextUtils.isEmpty(journeyDatePicked.getText().toString()) || TextUtils.isEmpty(journeyCity.getText().toString())
                || TextUtils.isEmpty(journeyAmount.getText().toString()) || (selectedImage == null)) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            mProgress.dismiss();
            toast.show();
            return;
        }

        //starting upload
        uploadTask = imageRef.putFile(selectedImage);
        // Observe state change events such as progress, pause, and resume
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //sets and increments value of progressbar
                mProgress.incrementProgressBy((int) progress);
            }
        });
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(ApplyForAccomodation.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                imageUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(ApplyForAccomodation.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                updateDatabase(xyz);
            }
        });
    }

    public void journeySubmit(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMax(100);
        mProgress.setMessage("Uploading....");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(isNetworkAvailable())
            mProgress.show();
        mProgress.setCancelable(false);
        uploadImage(user_employee_id + number_of_reimbursement_applied + count, FinalSubmitTaDa.class);
    }

    public void switchActivity (final Class<? extends Activity> xyz) {
        Log.d("Activity", String.valueOf(xyz));
        Intent intent = new Intent(this, xyz);
        intent.putExtra("type", type);
        intent.putExtra("employee_id", user_employee_id);
        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
        intent.putExtra("count", count);
        intent.putExtra("daily_allowance", da_for_apply_for_daily_allowance);
        startActivity(intent);
    }

    public void journeyIntercity(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMax(100);
        mProgress.setMessage("Uploading....");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(isNetworkAvailable())
            mProgress.show();
        mProgress.setCancelable(false);
        uploadImage(user_employee_id + number_of_reimbursement_applied + count, ApplyForIntercity.class);
    }

    public void journeyLocal(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMax(100);
        mProgress.setMessage("Uploading....");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(isNetworkAvailable())
            mProgress.show();
        mProgress.setCancelable(false);
        uploadImage(user_employee_id + number_of_reimbursement_applied + count, ApplyForLocal.class);
    }

    public void journeyAccomadation(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMax(100);
        mProgress.setMessage("Uploading....");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(isNetworkAvailable())
            mProgress.show();
        mProgress.setCancelable(false);
        uploadImage(user_employee_id + number_of_reimbursement_applied + count, ApplyForAccomodation.class);
    }

    public void journeyDailyAllowance(View view){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMax(100);
        mProgress.setMessage("Uploading....");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(isNetworkAvailable())
            mProgress.show();
        mProgress.setCancelable(false);
        uploadImage(user_employee_id + number_of_reimbursement_applied + count, ApplyForDailyAllowance.class);
    }

    // Save the values into database before switching an activity.
    public void updateDatabase(Class<? extends Activity> xyz){

        mReimbursementRef = mReimbursementRef.child(number_of_reimbursement_applied).child(count);
        mReimbursementRef.child("type").setValue("accommodation");
        mReimbursementRef.child("city").setValue(journeyCity.getText().toString());
        mReimbursementRef.child("city_class").setValue(item);
        mReimbursementRef.child("journey_date").setValue(journeyDatePicked.getText().toString());
        mReimbursementRef.child("journey_amount").setValue(journeyAmount.getText().toString());
        if (String.valueOf(imageUrl).length() > 0) {
            mReimbursementRef.child("img_url").setValue(String.valueOf(imageUrl));
        }
        else{
            mReimbursementRef.child("img_url").setValue(String.valueOf(""));
        }
        mProgress.dismiss();
        switchActivity(xyz);
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
            Intent intent = new Intent(ApplyForAccomodation.this, SupervisorProfile.class);
            intent.putExtra("User_ID", user_employee_id);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(ApplyForAccomodation.this, EmployeeProfile.class);
            intent.putExtra("User_ID", user_employee_id);
            startActivity(intent);
        }
    }
}
