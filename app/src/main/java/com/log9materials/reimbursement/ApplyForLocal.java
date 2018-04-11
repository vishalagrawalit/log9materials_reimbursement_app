package com.log9materials.reimbursement;

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
import android.widget.DatePicker;
import android.widget.EditText;
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

public class ApplyForLocal extends AppCompatActivity {

    TextView journeyReimbursementId;
    EditText journeyFromCity, journeyToCity, journeyDatePicked, journeyMode, journeyAmount;

    FirebaseDatabase mDatabase;
    DatabaseReference mReimbursementRef;

    String user_employee_id, number_of_reimbursement_applied, selectedDate, da_for_apply_for_daily_allowance, count, type;
    int mYear, mMonth, mDay, DATE_DIALOG_ID = 1;

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
        setContentView(R.layout.activity_apply_for_local);


        // Extra from previous Activity
        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        da_for_apply_for_daily_allowance = getIntent().getStringExtra("daily_allowance");
        count = getIntent().getStringExtra("count");
        count = String.valueOf(Integer.valueOf(count) + 1);
        type = getIntent().getStringExtra("type");

        // Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // EditText and TextView from XML file
        journeyReimbursementId = (TextView) findViewById(R.id.journeyReimbursementIdTextView);
        journeyFromCity = (EditText) findViewById(R.id.journeyFromCityEditText);
        journeyToCity = (EditText) findViewById(R.id.journeyToCityEditText);
        journeyDatePicked = (EditText) findViewById(R.id.journeyDatePickedEditText);
        journeyMode = (EditText) findViewById(R.id.journeyModeEditText);
        journeyAmount = (EditText) findViewById(R.id.journeyAmountEditText);


        // Setting Text to Text View
        journeyReimbursementId.setText("Your Reimbursement ID is - " + user_employee_id + number_of_reimbursement_applied);

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
    public void journeyTicketUpload(View view){
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ApplyForLocal.this);
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
        int permissionCheck = ContextCompat.checkSelfPermission(ApplyForLocal.this,
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
            ActivityCompat.requestPermissions(ApplyForLocal.this,
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

                    Toast.makeText(ApplyForLocal.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = new File(this.getExternalCacheDir(),
                            String.valueOf(System.currentTimeMillis()) + ".jpg");
                    selectedImage = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                    this.startActivityForResult(intent, REQUEST_CAMERA);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(ApplyForLocal.this, "Camera Permission Denied. You can't use camera for this task.", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(ApplyForLocal.this, "An error occured.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(ApplyForLocal.this, "Image selected", Toast.LENGTH_SHORT).show();
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

        if (TextUtils.isEmpty(journeyFromCity.getText().toString()) || TextUtils.isEmpty(journeyToCity.getText().toString()) ||
                TextUtils.isEmpty(journeyDatePicked.getText().toString()) || TextUtils.isEmpty(journeyMode.getText().toString())
                || TextUtils.isEmpty(journeyAmount.getText().toString()) || selectedImage == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            mProgress.dismiss();
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
                Toast.makeText(ApplyForLocal.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                imageUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(ApplyForLocal.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
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
        mReimbursementRef.child("boarding_city").setValue(journeyFromCity.getText().toString());
        mReimbursementRef.child("destination_city").setValue(journeyToCity.getText().toString());
        mReimbursementRef.child("journey_date").setValue(journeyDatePicked.getText().toString());
        mReimbursementRef.child("journey_mode").setValue(journeyMode.getText().toString());
        mReimbursementRef.child("journey_amount").setValue(journeyAmount.getText().toString());
        mReimbursementRef.child("type").setValue("local");
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
