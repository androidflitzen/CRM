package com.flitzen.crm.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.flitzen.crm.R;
import com.flitzen.crm.adapter.EnquiryAdapter;

public class EnquiryDetailsFragment extends Fragment implements View.OnClickListener {

    private View viewEnquiryDetails;
    private CardView cardAddFollowUp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewEnquiryDetails=inflater.inflate(R.layout.fragment_enquiry_details, container, false);

        cardAddFollowUp=viewEnquiryDetails.findViewById(R.id.cardAddFollowUp);
        cardAddFollowUp.setOnClickListener(this);
        return viewEnquiryDetails;
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView txtTile=((AppCompatActivity) getActivity()).findViewById(R.id.txtTile);
        txtTile.setText(getContext().getResources().getString(R.string.enquiry));
        CardView cardAddEnquiry=((AppCompatActivity) getActivity()).findViewById(R.id.cardAddEnquiry);
        cardAddEnquiry.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cardAddFollowUp:
                openAddFollowUpDialog();
                break;
        }
    }

    private void openAddFollowUpDialog() {
        LayoutInflater localView = LayoutInflater.from(getActivity());
        View promptsView = localView.inflate(R.layout.add_follow_up_dialog, null);

        final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);

        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }
                return true;
            }
        });

        alertDialog.show();
    }
}
