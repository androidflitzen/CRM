package com.flitzen.crm.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flitzen.crm.R;
import com.flitzen.crm.adapter.EnquiryAdapter;
import com.flitzen.crm.adapter.Importance_EnquirySource_Adapter;
import com.flitzen.crm.items.EnquiryItem;
import com.flitzen.crm.items.Importance_EnquirySourceItem;
import com.flitzen.crm.utiles.FirebaseConstant;
import com.flitzen.crm.utiles.Helper;
import com.flitzen.crm.utiles.SharePref;
import com.fxn.cue.Cue;
import com.fxn.cue.enums.Type;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnquiryDetailsFragment extends Fragment implements View.OnClickListener,Importance_EnquirySource_Adapter.ItemClickListener {

    private View viewEnquiryDetails;
    private CardView cardAddFollowUp;
    private TextView txtServiceName, txtEnquirySource, txtOtherDetails, txtEnquiryCategory, txtImportance, txtCustomerName, txtPhoneNo, txtEmail;
    ArrayList<EnquiryItem> enquiryItemArrayList;
    int position;
    DatabaseReference databaseReference;
    private ArrayList<Importance_EnquirySourceItem> sourceItemArrayList = new ArrayList<Importance_EnquirySourceItem>();
    private androidx.appcompat.app.AlertDialog alertDialogEnquirySource;
    private Importance_EnquirySource_Adapter importanceEnquirySourceAdapter;
    EditText edtEnquirySource;
    ProgressDialog prd;
    DatabaseReference rootRef;
    SharedPreferences sharedPreferences;

    public EnquiryDetailsFragment(ArrayList<EnquiryItem> enquiryItemArrayList, int position) {
        this.enquiryItemArrayList = enquiryItemArrayList;
        this.position = position;
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewEnquiryDetails = inflater.inflate(R.layout.fragment_enquiry_details, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        rootRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = SharePref.getSharePref(getActivity());

        txtServiceName = viewEnquiryDetails.findViewById(R.id.txtServiceName);
        txtEnquirySource = viewEnquiryDetails.findViewById(R.id.txtEnquirySource);
        txtOtherDetails = viewEnquiryDetails.findViewById(R.id.txtOtherDetails);
        txtEnquiryCategory = viewEnquiryDetails.findViewById(R.id.txtEnquiryCategory);
        txtImportance = viewEnquiryDetails.findViewById(R.id.txtImportance);
        txtCustomerName = viewEnquiryDetails.findViewById(R.id.txtCustomerName);
        txtPhoneNo = viewEnquiryDetails.findViewById(R.id.txtPhoneNo);
        txtEmail = viewEnquiryDetails.findViewById(R.id.txtEmail);

        cardAddFollowUp = viewEnquiryDetails.findViewById(R.id.cardAddFollowUp);
        cardAddFollowUp.setOnClickListener(this);

        txtServiceName.setText(enquiryItemArrayList.get(position).getEnquiryFor());
        txtEnquirySource.setText(enquiryItemArrayList.get(position).getEnquirySource());
        txtImportance.setText(enquiryItemArrayList.get(position).getImportance());
        txtCustomerName.setText(enquiryItemArrayList.get(position).getPersonName());
        txtPhoneNo.setText(enquiryItemArrayList.get(position).getContactNo());
        if (enquiryItemArrayList.get(position).getEmail().trim().equals("")) {
            txtEmail.setText("-");
        } else {
            txtEmail.setText(enquiryItemArrayList.get(position).getEmail());
        }

        if (enquiryItemArrayList.get(position).getOtherDetails().trim().equals("")) {
            txtOtherDetails.setText(" -");
        } else {
            txtOtherDetails.setText(enquiryItemArrayList.get(position).getOtherDetails());
        }

        if (enquiryItemArrayList.get(position).getImportance().equals(getString(R.string.high))) {
            txtImportance.setBackgroundTintList(getResources().getColorStateList(R.color.high));
        } else if (enquiryItemArrayList.get(position).getImportance().equals(getString(R.string.medium))) {
            txtImportance.setBackgroundTintList(getResources().getColorStateList(R.color.medium));

        } else if (enquiryItemArrayList.get(position).getImportance().equals(getString(R.string.low))) {
            txtImportance.setBackgroundTintList(getResources().getColorStateList(R.color.low));
        }


        if (Helper.isOnline(getActivity())) {
            getEnquirySource();
        } else {
            Helper.createDialog(getActivity());
        }

        return viewEnquiryDetails;
    }

    private void getEnquirySource() {
        Query query = databaseReference.child(FirebaseConstant.EnquirySource.EnquirySource_TABLE).orderByKey();
        //databaseReference.keepSynced(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sourceItemArrayList.clear();
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                            Importance_EnquirySourceItem sourceItem = new Importance_EnquirySourceItem();
                            sourceItem.setId(npsnapshot.child(FirebaseConstant.EnquirySource.id).getValue().toString());
                            sourceItem.setName(npsnapshot.child(FirebaseConstant.EnquirySource.enquirySource).getValue().toString());

                            sourceItemArrayList.add(sourceItem);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Test  ", e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Cue.init()
                        .with(getActivity())
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(databaseError.getMessage().toString())
                        .setType(Type.DANGER)
                        .show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView txtTile = ((AppCompatActivity) getActivity()).findViewById(R.id.txtTile);
        txtTile.setText(getContext().getResources().getString(R.string.enquiry));
        CardView cardAddEnquiry = ((AppCompatActivity) getActivity()).findViewById(R.id.cardAddEnquiry);
        cardAddEnquiry.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

        EditText edtFollowDate = promptsView.findViewById(R.id.edtFollowDate);
        EditText edtFollowTime = promptsView.findViewById(R.id.edtFollowTime);
        edtEnquirySource = promptsView.findViewById(R.id.edtEnquirySource);
        EditText edtAction = promptsView.findViewById(R.id.edtAction);
        Button btnAddFollowUp = promptsView.findViewById(R.id.btnAddFollowUp);


        //Open date picker
        edtFollowDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "dd MMM yyyy"; // your format
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                        edtFollowDate.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        //Open Time picker
        edtFollowTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                final Calendar myCalendar = Calendar.getInstance();
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        myCalendar.set(Calendar.MINUTE, selectedMinute);
                        //myCalendar.set(Calendar.AM_PM, dayOfMonth);

                        String myFormat = "hh:mm aa"; // your format
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                        edtFollowTime.setText(sdf.format(myCalendar.getTime()));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.show();

            }
        });

        edtEnquirySource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEnquirySourceDialog();
            }
        });

        btnAddFollowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtFollowDate.getText().toString().trim().isEmpty()){
                    edtFollowDate.setError(getString(R.string.enter_date));
                    edtFollowDate.requestFocus();
                }else if(edtFollowTime.getText().toString().trim().isEmpty()){
                    edtFollowTime.setError(getString(R.string.enter_time));
                    edtFollowTime.requestFocus();
                }else if(edtEnquirySource.getText().toString().trim().isEmpty()){
                    edtFollowDate.setError(getString(R.string.enter_communication_medium));
                    edtFollowDate.requestFocus();
                }else if(edtAction.getText().toString().trim().isEmpty()){
                    edtFollowDate.setError(getString(R.string.enter_action));
                    edtFollowDate.requestFocus();
                }else {
                    if (Helper.isOnline(getActivity())) {
                        addFollowUP(edtFollowDate.getText().toString().trim(),edtFollowTime.getText().toString().trim(),edtEnquirySource.getText().toString().trim(),edtAction.getText().toString(),alertDialog);
                    } else {
                        Helper.createDialog(getActivity());
                    }
                }
            }
        });

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

    private void addFollowUP(String date, String time, String enquirySource, String action, AlertDialog alertDialog) {
        showPrd();

        String key = rootRef.child(FirebaseConstant.FollowUp.FollowUp_TABLE).push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put(FirebaseConstant.FollowUp.id, key);
        map.put(FirebaseConstant.FollowUp.followerId,sharedPreferences.getString(SharePref.userId,""));
        map.put(FirebaseConstant.FollowUp.followUpDate, date);
        map.put(FirebaseConstant.FollowUp.followUpTime, time);
        map.put(FirebaseConstant.FollowUp.enquirySource, enquirySource);
        map.put(FirebaseConstant.FollowUp.action, action);
        map.put(FirebaseConstant.FollowUp.enquiryId, enquiryItemArrayList.get(position).getId());

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(new Date());

        map.put(FirebaseConstant.FollowUp.creationDate, currentDate+" "+currentTime);

        rootRef.child(FirebaseConstant.FollowUp.FollowUp_TABLE).child(key).setValue(map).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                hidePrd();
                Cue.init()
                        .with(getActivity())
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(getString(R.string.add_follow_success))
                        .setType(Type.SUCCESS)
                        .show();
                alertDialog.dismiss();
                getActivity().onBackPressed();
            }

        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hidePrd();
                Cue.init()
                        .with(getActivity())
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(getString(R.string.try_later))
                        .setType(Type.DANGER)
                        .show();
            }
        });
    }

    private void openEnquirySourceDialog() {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_dialog, null);
        builder.setView(view);
        alertDialogEnquirySource = builder.create();
        alertDialogEnquirySource.show();

        EditText edt_spn_search = view.findViewById(R.id.edt_spn_search);
        final RecyclerView recycleCurrency = view.findViewById(R.id.recycleCurrency);
        final TextView txt_loading = view.findViewById(R.id.txt_loading);
        final TextView txtTitle = view.findViewById(R.id.txtTitle);

        edt_spn_search.setVisibility(View.GONE);
        txt_loading.setVisibility(View.GONE);
        recycleCurrency.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);

        txtTitle.setText(getResources().getString(R.string.importance));
        importanceEnquirySourceAdapter = new Importance_EnquirySource_Adapter(getActivity(), sourceItemArrayList);
        if (sourceItemArrayList != null) {
            if (sourceItemArrayList.size() > 0) {
                recycleCurrency.setLayoutManager(new LinearLayoutManager(getActivity()));
                importanceEnquirySourceAdapter.setClickListener(this);
                recycleCurrency.setAdapter(importanceEnquirySourceAdapter);
                importanceEnquirySourceAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View view, int position) {
        if (alertDialogEnquirySource.isShowing()) {
            alertDialogEnquirySource.dismiss();
            edtEnquirySource.setText(sourceItemArrayList.get(position).getName());
        }
    }

    public void showPrd() {
        prd = new ProgressDialog(getActivity(),R.style.MyAlertDialogStyle);
        prd.setMessage("Please wait...");
        prd.setCancelable(false);
        prd.show();
    }

    public void hidePrd() {
        if (prd != null) {
            if (prd.isShowing()) {
                prd.dismiss();
            }
        }
    }
}
