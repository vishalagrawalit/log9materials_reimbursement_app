package com.log9materials.reimbursement;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class ViewReimburseDetail extends AppCompatActivity {

    TextView showEmployeeName, showEmployeeId, showAmount, showDate, showReimbursementId;
    ImageView transactionImage;

    String user_employee_name, user_employee_id, amount, date, re_id, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reimburse_detail);

        user_employee_name = getIntent().getStringExtra("name");
        user_employee_id = getIntent().getStringExtra("id");
        amount = getIntent().getStringExtra("amount");
        date = getIntent().getStringExtra("employee_id");
        re_id = getIntent().getStringExtra("reid");
        url = getIntent().getStringExtra("img");

        showEmployeeName = (TextView) findViewById(R.id.employee_name_view);
        showEmployeeId = (TextView) findViewById(R.id.employee_id_view);
        showAmount = (TextView) findViewById(R.id.amount_view);
        showReimbursementId= (TextView) findViewById(R.id.reimbursement_id_view);
        showDate = (TextView) findViewById(R.id.date_view);
        transactionImage = (ImageView) findViewById(R.id.Image);

        Log.v("Url", url);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_image)
                .into(transactionImage);

        showEmployeeName.setText(user_employee_name);
        showEmployeeId.setText(user_employee_id);
        showAmount.setText(amount);
        showDate.setText(date);
        showReimbursementId.setText(re_id);
    }
}
