package com.log9materials.reimbursement;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by VISHAL on 17-01-2018.
 */

public class CustomAdapter extends BaseAdapter {

    Context getContext;
    ArrayList<String> employee_names_list, employee_id_list, amount_list, date_list, image_url_list,
            reimbursement_id_list, pending_list_count, pending_list_state;
    int layout;
    TextView showEmployeeName, showEmployeeId, showAmount, showDate, showReimbursementId;
    Button viewImageButton, disapproveButton;
    String status;

    FirebaseDatabase mDatabase;
    DatabaseReference mReimbursementRef;

    public CustomAdapter(@NonNull Context context, @LayoutRes int resource,
                         ArrayList<String> pending_list_employee_name, ArrayList<String>pending_list_employee_id,
                         ArrayList<String>pending_list_amount, ArrayList<String>pending_list_date,
                         ArrayList<String>pending_list_image_url, ArrayList<String>pending_list_reimbursement_id,
                         ArrayList<String>pending_list_count, ArrayList<String>pending_list_state, String reimbursement_status ) {

        this.getContext = context;
        this.employee_names_list = pending_list_employee_name;
        this.employee_id_list = pending_list_employee_id;
        this.amount_list = pending_list_amount;
        this.date_list = pending_list_date;
        this.image_url_list = pending_list_image_url;
        this.reimbursement_id_list = pending_list_reimbursement_id;
        this.pending_list_count = pending_list_count;
        this.pending_list_state = pending_list_state;
        this.status = reimbursement_status;
        this.layout = resource;
    }

    public int getCount() {
        Log.d("list size", String.valueOf(employee_names_list.size()));
        return employee_names_list.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, final ViewGroup parent) {

        LayoutInflater inflter = (LayoutInflater) getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView = inflter.inflate(layout, parent, false);

        // Text View references
        showEmployeeName = (TextView) rowView.findViewById(R.id.employee_name_view);
        showEmployeeId = (TextView) rowView.findViewById(R.id.employee_id_view);
        showAmount = (TextView) rowView.findViewById(R.id.amount_view);
        showReimbursementId= (TextView) rowView.findViewById(R.id.reimbursement_id_view);
        showDate = (TextView) rowView.findViewById(R.id.date_view);
        viewImageButton = (Button) rowView.findViewById(R.id.view_image_button_id);
        disapproveButton = (Button) rowView.findViewById(R.id.disapprove_reimbursement_id);

        disapproveButton.setVisibility(View.INVISIBLE);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mReimbursementRef = mDatabase.getReference("Reimbursement");

        // Setting text to Text Views

        showEmployeeName.setText(employee_names_list.get(position));
        showEmployeeId.setText(employee_id_list.get(position));
        showAmount.setText(amount_list.get(position));
        showReimbursementId.setText(reimbursement_id_list.get(position));
        showDate.setText(date_list.get(position));

        // show disapprove button only in pending list
        if (status.equals("pending")){
            disapproveButton.setVisibility(View.VISIBLE);
        }

        disapproveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extracting Reimbursement Number from Reimbursement ID
                String reimburse_id = "";
                for (int i=4; i<reimbursement_id_list.get(position).length(); i++){
                    reimburse_id += reimbursement_id_list.get(position).charAt(i);
                }
                // Changing status from pending to disapproved
                mReimbursementRef.child(employee_id_list.get(position)).child(reimburse_id).child("0").child("status").setValue("disapproved");
            }
        });

        Log.v("List", String.valueOf(pending_list_state));
        viewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("Clicked", pending_list_state.get(position));
                if (pending_list_state.get(position).equals("regular")) {
                    Intent intent = new Intent(parent.getContext(), ViewReimburseDetail.class);
                    intent.putExtra("name", employee_names_list.get(position));
                    intent.putExtra("id", employee_id_list.get(position));
                    intent.putExtra("amount", amount_list.get(position));
                    intent.putExtra("date", date_list.get(position));
                    intent.putExtra("img", image_url_list.get(position));
                    intent.putExtra("reid", reimbursement_id_list.get(position));
                    parent.getContext().startActivity(intent);
                }
                else {
                    Intent intent = new Intent(parent.getContext(), ViewTaDaDetails.class);
                    intent.putExtra("id", employee_id_list.get(position));
                    intent.putExtra("reid", reimbursement_id_list.get(position));
                    parent.getContext().startActivity(intent);
                }
            }
        });
        return rowView;
        }
}