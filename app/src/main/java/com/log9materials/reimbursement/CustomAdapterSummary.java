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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lab on 1/29/2018.
 */

public class CustomAdapterSummary extends ArrayAdapter {
    Context getContext;
    ArrayList<String> ta_da_summary, ta_da_pushId, ta_da_type, ta_da_url;
    String user_employee_id, number_of_reimbursement_applied, path, count;
    int layout;
    TextView showReimbursementDetails;
    Button editReimbursementDetails;

    public CustomAdapterSummary(@NonNull Context context, @LayoutRes int resource,
                         ArrayList<String> ta_da_summary, ArrayList<String> ta_da_pushId,
                                ArrayList<String> ta_da_type, ArrayList<String> ta_da_url, String user_employee_id,
                                String number_of_reimbursement_applied, String count, String path) {
        super(context, resource, ta_da_summary);

        this.getContext = context;
        this.ta_da_summary = ta_da_summary;
        this.ta_da_pushId = ta_da_pushId;
        this.ta_da_type = ta_da_type;
        this.user_employee_id = user_employee_id;
        this.number_of_reimbursement_applied = number_of_reimbursement_applied;
        this.ta_da_url = ta_da_url;
        this.path = path;
        this.count = count;
        this.layout = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, final ViewGroup parent) {

        LayoutInflater inflter = (LayoutInflater) getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView = inflter.inflate(layout, parent, false);

        // Text View references
        showReimbursementDetails = (TextView) rowView.findViewById(R.id.appliedSummary);
        editReimbursementDetails = (Button) rowView.findViewById(R.id.editJourneyDetails);

        // Setting text to Text Views
        showReimbursementDetails.setText(ta_da_summary.get(position));

        if (path.equals("form")) {
            editReimbursementDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ta_da_type.get(position).equals("intercity")) {
                        Intent intent = new Intent(parent.getContext(), EditIntercityTADA.class);
                        intent.putExtra("employee_id", user_employee_id);
                        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
                        intent.putExtra("pushId", String.valueOf(position));
                        intent.putExtra("type", ta_da_type.get(position));
                        intent.putExtra("count", count);
                        parent.getContext().startActivity(intent);
                    } else if (ta_da_type.get(position).equals("local")) {
                        Intent intent = new Intent(parent.getContext(), EditLocalTADA.class);
                        intent.putExtra("employee_id", user_employee_id);
                        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
                        intent.putExtra("pushId", String.valueOf(position));
                        intent.putExtra("type", ta_da_type.get(position));
                        intent.putExtra("count", count);
                        parent.getContext().startActivity(intent);
                    } else if (ta_da_type.get(position).equals("accommodation")) {
                        Intent intent = new Intent(parent.getContext(), EditLocalTADA.class);
                        intent.putExtra("employee_id", user_employee_id);
                        intent.putExtra("ReimbursementCount", number_of_reimbursement_applied);
                        intent.putExtra("pushId", String.valueOf(position));
                        intent.putExtra("type", ta_da_type.get(position));
                        intent.putExtra("count", count);
                        parent.getContext().startActivity(intent);
                    }
                    else {
                        Toast.makeText(getContext, "Sorry you cannot view or edit this.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            editReimbursementDetails.setText("View Image");
            editReimbursementDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(parent.getContext(), ViewFullTaDaDetails.class);
                    intent.putExtra("url", ta_da_url.get(position));
                    parent.getContext().startActivity(intent);
                }
            });
        }

        return rowView;
    }
}
