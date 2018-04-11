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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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

public class ApplyForRegular extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Image Handler
    StorageReference mStorageRef, imageRef;
    static final int SELECT_FILE=100, REQUEST_CAMERA=101, MY_PERMISSIONS_REQUEST_CAMERA=200;
    ProgressDialog mProgress;
    Uri selectedImage, imageUrl, image;
    File file;
    UploadTask uploadTask;

    // Firebase Database Variables
    FirebaseDatabase mDatabase;
    DatabaseReference mEmployeeRef, mReimbursementRef;

    EditText getRegularDate, getRegularDescription, getRegularAmount;

    String user_employee_id, number_of_reimbursement_applied, item, user_employee_name, total_amount_applied, type, selectedDate;
    int mYear, mMonth, mDay, DATE_DIALOG_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_regular);

        user_employee_id = getIntent().getStringExtra("employee_id");
        number_of_reimbursement_applied = getIntent().getStringExtra("ReimbursementCount");
        user_employee_name = getIntent().getStringExtra("employee_name");
        total_amount_applied = getIntent().getStringExtra("amount_applied");
        type = getIntent().getStringExtra("type");

        getRegularDate = (EditText) findViewById(R.id.regular_date);
        getRegularDescription = (EditText) findViewById(R.id.regular_description);
        getRegularAmount = (EditText) findViewById(R.id.regular_amount);

        // Firebase Database References
        mDatabase = FirebaseDatabase.getInstance();
        mEmployeeRef = mDatabase.getReference("Employee").child(user_employee_id);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mReimbursementRef = mDatabase.getReference("Reimbursement");

        // Increasing Value of the ReimbursementCount to generate new reimbursement ID.
        number_of_reimbursement_applied = String.valueOf(Integer.valueOf(number_of_reimbursement_applied)+1);

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.regular_type);

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
            getRegularDate.setText(String.valueOf(mDay)+"-"+String.valueOf(mMonth+1)+"-"+String.valueOf(mYear));
        }
    };
    // Execute when Upload Ticket is pressed
    public void upload_reg_bill(View view){
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ApplyForRegular.this);
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
        int permissionCheck = ContextCompat.checkSelfPermission(ApplyForRegular.this,
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
            ActivityCompat.requestPermissions(ApplyForRegular.this,
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

                    Toast.makeText(ApplyForRegular.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = new File(this.getExternalCacheDir(),
                            String.valueOf(System.currentTimeMillis()) + ".jpg");
                    selectedImage = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                    this.startActivityForResult(intent, REQUEST_CAMERA);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(ApplyForRegular.this, "Camera Permission Denied. You can't use camera for this task.", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(ApplyForRegular.this, "An error occured.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(ApplyForRegular.this, "Image selected", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    Log.d("Image", "clicked");
                    CropImage.activity(selectedImage).setGuidelines(CropImageView.Guidelines.ON).start(this);
                }
        }
    }

    public void uploadImage(final String imageName) {
        //create reference to images folder and assigning a name to the file that will be uploaded
        imageRef = mStorageRef.child("Photos/Regular/" + imageName);


        if (TextUtils.isEmpty(getRegularDate.getText().toString()) || TextUtils.isEmpty(getRegularDescription.getText().toString()) ||
                TextUtils.isEmpty(getRegularAmount.getText().toString()) || selectedImage == null) {
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
                Toast.makeText(ApplyForRegular.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                imageUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(ApplyForRegular.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                updateDatabase();
            }
        });
    }

    public void submit_regular(View view){

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
        uploadImage(user_employee_id + number_of_reimbursement_applied);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    // Save the values into database before switching an activity.
    public void updateDatabase(){

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Log.v("Into Regular", "Yes");
        mReimbursementRef = mDatabase.getReference("Reimbursement").child(user_employee_id).child(number_of_reimbursement_applied).child("0");
        mReimbursementRef.child("employee_name").setValue(user_employee_name);
        mReimbursementRef.child("date").setValue(getRegularDate.getText().toString());
        mReimbursementRef.child("amount").setValue(getRegularAmount.getText().toString());
        mReimbursementRef.child("description").setValue(getRegularDescription.getText().toString());
        mReimbursementRef.child("da_or_reg").setValue("regular");
        mReimbursementRef.child("status").setValue("pending");
        mReimbursementRef.child("type").setValue(item);
        Log.v("Into Regular", "Yipee");
        mReimbursementRef.child("img_url").setValue(String.valueOf(imageUrl));
        mReimbursementRef.child("reimbursement_id").setValue(user_employee_id + number_of_reimbursement_applied);
        mDatabase.getReference("Reimbursement").child(user_employee_id).child("count").setValue(number_of_reimbursement_applied);;

        mDatabase.getReference("Employee").child(user_employee_id).child("reimbursement_count").setValue(number_of_reimbursement_applied);
        mDatabase.getReference("Employee").child(user_employee_id).child("amount_applied").setValue(String.valueOf(Integer.valueOf(total_amount_applied) + Integer.valueOf(getRegularAmount.getText().toString())));
        mProgress.dismiss();

        Toast.makeText(this, "Form Submitted", Toast.LENGTH_SHORT).show();

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
