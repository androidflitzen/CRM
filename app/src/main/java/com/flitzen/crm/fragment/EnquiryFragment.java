package com.flitzen.crm.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.flitzen.crm.R;
import com.flitzen.crm.activity.AddCompanyActivity;
import com.flitzen.crm.activity.LoginActivity;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EnquiryFragment extends Fragment implements View.OnClickListener, EnquiryAdapter.ItemClickListener,Importance_EnquirySource_Adapter.ItemClickListener  {

    private View viewEnquiry;

    EnquiryAdapter enquiryAdapter;
    ArrayList<EnquiryItem> enquiryItemArrayList=new ArrayList<>();
    @Nullable
    @BindView(R.id.recyclerEnquiry)
    RecyclerView recyclerEnquiry;

    CardView cardAddEnquiry;
    RelativeLayout noDataLayout;
    DatabaseReference rootRef;
    SharedPreferences sharedPreferences;
    ProgressDialog prd;
    SwipeRefreshLayout swipeRefresh;
    EditText edtEnquirySource;
    private androidx.appcompat.app.AlertDialog alertDialogEnquirySource;
    private Importance_EnquirySource_Adapter importanceEnquirySourceAdapter;
    private ArrayList<Importance_EnquirySourceItem> sourceItemArrayList = new ArrayList<Importance_EnquirySourceItem>();
    DatabaseReference databaseReference;
    TextView txtAddBtnName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewEnquiry=inflater.inflate(R.layout.fragment_enquiry, container, false);
        recyclerEnquiry =  viewEnquiry.findViewById(R.id.recyclerEnquiry);
        noDataLayout =  viewEnquiry.findViewById(R.id.noDataLayout);
        swipeRefresh =  viewEnquiry.findViewById(R.id.swipeRefresh);
       // ButterKnife.bind(getContext(),viewEnquiry);
        enquiryAdapter  = new EnquiryAdapter(getActivity(),enquiryItemArrayList);
        recyclerEnquiry.setHasFixedSize(true);
        recyclerEnquiry.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerEnquiry.setAdapter(enquiryAdapter);
        enquiryAdapter.setClickListener(this);

        rootRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = SharePref.getSharePref(getActivity());
        databaseReference = FirebaseDatabase.getInstance().getReference();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Helper.isOnline(getActivity())) {
                    getEnquiryList(1);
                } else {
                    Helper.createDialog(getActivity());
                }
            }
        });

        if (Helper.isOnline(getActivity())) {
            getEnquirySource();
        } else {
            Helper.createDialog(getActivity());
        }

        return viewEnquiry;
    }

    private void loadFragment(Fragment fragment, String fragmentName) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(fragmentName);
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView txtTile=((AppCompatActivity) getActivity()).findViewById(R.id.txtTile);
        txtTile.setText(getContext().getResources().getString(R.string.enquiry));
        cardAddEnquiry=((AppCompatActivity) getActivity()).findViewById(R.id.cardAddEnquiry);
        txtAddBtnName=((AppCompatActivity) getActivity()).findViewById(R.id.txtAddBtnName);
        txtAddBtnName.setText(getActivity().getResources().getString(R.string.add_enquiry));
        cardAddEnquiry.setVisibility(View.VISIBLE);
        cardAddEnquiry.setOnClickListener(this);

        if (Helper.isOnline(getActivity())) {
            getEnquiryList(0);
        } else {
            Helper.createDialog(getActivity());
        }

    }

    private void getEnquiryList(int i) {
        if(i==0){
            showPrd();
        }
        Query queryLogin = rootRef.child(FirebaseConstant.Enquiry.Enquiry_TABLE).orderByChild(FirebaseConstant.Enquiry.userId).equalTo(sharedPreferences.getString(SharePref.userId,""));
        queryLogin.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefresh.setRefreshing(false);
                hidePrd();
                if (!snapshot.exists()) {
                    //create new user
                    recyclerEnquiry.setVisibility(View.GONE);
                    noDataLayout.setVisibility(View.VISIBLE);

                } else {
                    enquiryItemArrayList.clear();
                    for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                        EnquiryItem enquiryItem=new EnquiryItem();
                        enquiryItem.setId(npsnapshot.child(FirebaseConstant.Enquiry.id).getValue().toString());
                        enquiryItem.setUserId(npsnapshot.child(FirebaseConstant.Enquiry.userId).getValue().toString());
                        enquiryItem.setCompanyName(npsnapshot.child(FirebaseConstant.Enquiry.companyName).getValue().toString());
                        enquiryItem.setPersonName(npsnapshot.child(FirebaseConstant.Enquiry.personName).getValue().toString());
                        enquiryItem.setEmail(npsnapshot.child(FirebaseConstant.Enquiry.email).getValue().toString());
                        enquiryItem.setContactNo(npsnapshot.child(FirebaseConstant.Enquiry.contactNo).getValue().toString());
                        enquiryItem.setEnquiryFor(npsnapshot.child(FirebaseConstant.Enquiry.enquiryFor).getValue().toString());
                        enquiryItem.setEnquirySource(npsnapshot.child(FirebaseConstant.Enquiry.enquirySource).getValue().toString());
                        enquiryItem.setImportance(npsnapshot.child(FirebaseConstant.Enquiry.importance).getValue().toString());
                        enquiryItem.setOtherDetails(npsnapshot.child(FirebaseConstant.Enquiry.otherDetails).getValue().toString());
                        enquiryItem.setEnquiryTime(npsnapshot.child(FirebaseConstant.Enquiry.enquiryTime).getValue().toString());
                        enquiryItem.setEnquiryDate(npsnapshot.child(FirebaseConstant.Enquiry.enquiryDate).getValue().toString());
                        enquiryItemArrayList.add(enquiryItem);
                    }
                    if(enquiryItemArrayList.size()>0){
                        recyclerEnquiry.setVisibility(View.VISIBLE);
                        noDataLayout.setVisibility(View.GONE);
                    }
                    else {
                        recyclerEnquiry.setVisibility(View.GONE);
                        noDataLayout.setVisibility(View.VISIBLE);
                    }
                    Collections.reverse(enquiryItemArrayList);
                    enquiryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hidePrd();
                Cue.init()
                        .with(getActivity())
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(getString(R.string.somthing_went))
                        .setType(Type.DANGER)
                        .show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cardAddEnquiry:
                loadFragment(new AddNewEnquiry(),getResources().getString(R.string.add_new_enquiry));
                break;
        }
    }

    @Override
    public void onClick(View view, int position) {
        if (alertDialogEnquirySource.isShowing()) {
            alertDialogEnquirySource.dismiss();
            edtEnquirySource.setText(sourceItemArrayList.get(position).getName());
        }
    }

    @Override
    public void onClickEnquiryItem(View view, int position) {
        if(view.getId()==R.id.cardAddFollowUp){
            openAddFollowUpDialog(position);
        }
        else {
            loadFragment(new EnquiryDetailsFragment(enquiryItemArrayList,position),getResources().getString(R.string.enquiry_details));
        }
    }

    private void openAddFollowUpDialog(int position) {
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
                        addFollowUP(edtFollowDate.getText().toString().trim(),edtFollowTime.getText().toString().trim(),edtEnquirySource.getText().toString().trim(),edtAction.getText().toString()
                                ,alertDialog,position);
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

    private void addFollowUP(String date, String time, String enquirySource, String action, AlertDialog alertDialog, int position) {
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
