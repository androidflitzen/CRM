package com.flitzen.crm.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flitzen.crm.R;
import com.flitzen.crm.activity.AddCompanyActivity;
import com.flitzen.crm.activity.LoginActivity;
import com.flitzen.crm.activity.RegisterActivity;
import com.flitzen.crm.activity.Splash2Activiy;
import com.flitzen.crm.adapter.CurrencyAdapter;
import com.flitzen.crm.adapter.Importance_EnquirySource_Adapter;
import com.flitzen.crm.items.CurrencyItem;
import com.flitzen.crm.items.Importance_EnquirySourceItem;
import com.flitzen.crm.utiles.FirebaseConstant;
import com.flitzen.crm.utiles.Helper;
import com.flitzen.crm.utiles.SharePref;
import com.fxn.cue.Cue;
import com.fxn.cue.enums.Type;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddNewEnquiry extends Fragment implements View.OnClickListener, Importance_EnquirySource_Adapter.ItemClickListener {

    private View viewNewEnquiry;
    private EditText edtImportance, edtCompanyName, edtPersonName, edtEmail, edtContactNo, edtEnquiryFor, edtEnquirySource, edtOtherDetails;
    private AlertDialog alertDialog;
    private Importance_EnquirySource_Adapter importanceEnquirySourceAdapter;
    private int checkEDTSelection = 6;
    private ArrayList<Importance_EnquirySourceItem> sourceItemArrayList = new ArrayList<Importance_EnquirySourceItem>();
    private ArrayList<Importance_EnquirySourceItem> sourceItemArrayListImportance = new ArrayList<Importance_EnquirySourceItem>();
    DatabaseReference databaseReference;

    private Button btnAddNewEnquiry;
    ProgressDialog prd;
    DatabaseReference rootRef;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewNewEnquiry = inflater.inflate(R.layout.fragment_add_new_enquiry, container, false);

        List<String> importanceList = Arrays.asList(getResources().getString(R.string.high), getResources().getString(R.string.medium), getResources().getString(R.string.low));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        edtImportance = viewNewEnquiry.findViewById(R.id.edtImportance);
        edtCompanyName = viewNewEnquiry.findViewById(R.id.edtCompanyName);
        edtPersonName = viewNewEnquiry.findViewById(R.id.edtPersonName);
        edtEmail = viewNewEnquiry.findViewById(R.id.edtEmail);
        edtContactNo = viewNewEnquiry.findViewById(R.id.edtContactNo);
        edtEnquiryFor = viewNewEnquiry.findViewById(R.id.edtEnquiryFor);
        edtEnquirySource = viewNewEnquiry.findViewById(R.id.edtEnquirySource);
        edtOtherDetails = viewNewEnquiry.findViewById(R.id.edtOtherDetails);
        btnAddNewEnquiry = viewNewEnquiry.findViewById(R.id.btnAddNewEnquiry);

        edtImportance.setOnClickListener(this);
        edtEnquirySource.setOnClickListener(this);
        btnAddNewEnquiry.setOnClickListener(this);

        rootRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = SharePref.getSharePref(getActivity());

        for (int i = 0; i < 3; i++) {
            sourceItemArrayListImportance.add(new Importance_EnquirySourceItem(String.valueOf(i), importanceList.get(i)));
        }

        if (Helper.isOnline(getActivity())) {
            getEnquirySource();
        } else {
            Helper.createDialog(getActivity());
        }

        return viewNewEnquiry;
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
                           /* if (importanceEnquirySourceAdapter != null) {
                                importanceEnquirySourceAdapter.notifyDataSetChanged();
                            }*/
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
            case R.id.edtImportance:
                checkEDTSelection = 7;
                openDialog(7);
                break;

            case R.id.edtEnquirySource:
                checkEDTSelection = 6;
                openDialog(6);
                break;

            case R.id.btnAddNewEnquiry:
                checkValidation();
                break;
        }
    }

    private void checkValidation() {
        if (edtCompanyName.getText().toString().trim().isEmpty()) {
            edtCompanyName.setError(getString(R.string.enter_company_name));
            edtCompanyName.requestFocus();
            return;
        } else if (edtPersonName.getText().toString().trim().isEmpty()) {
            edtPersonName.setError(getString(R.string.enter_person_name));
            edtPersonName.requestFocus();
            return;
        } else if (edtContactNo.getText().toString().trim().isEmpty()) {
            edtContactNo.setError(getString(R.string.enter_contact_number));
            edtContactNo.requestFocus();
            return;
        } else if (edtEnquiryFor.getText().toString().trim().isEmpty()) {
            edtEnquiryFor.setError(getString(R.string.enter_enquiry_for));
            edtEnquiryFor.requestFocus();
            return;
        } else if (edtEnquirySource.getText().toString().trim().isEmpty()) {
            edtEnquirySource.setError(getString(R.string.enter_enquiry_source));
            edtEnquirySource.requestFocus();
            return;
        } else if (edtImportance.getText().toString().trim().isEmpty()) {
            edtImportance.setError(getString(R.string.enter_importance));
            edtImportance.requestFocus();
            return;
        } else {
            addNewEnquiry();
        }
    }

    private void addNewEnquiry() {
        if (Helper.isOnline(getActivity())) {
            showPrd();

            String key = rootRef.child(FirebaseConstant.Enquiry.Enquiry_TABLE).push().getKey();
            Map<String, Object> map = new HashMap<>();
            map.put(FirebaseConstant.Enquiry.id, key);
            map.put(FirebaseConstant.Enquiry.userId,sharedPreferences.getString(SharePref.userId,""));
            map.put(FirebaseConstant.Enquiry.companyName, edtCompanyName.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.personName, edtPersonName.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.email, edtEmail.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.contactNo, edtContactNo.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.enquiryFor, edtEnquiryFor.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.enquirySource, edtEnquirySource.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.importance, edtImportance.getText().toString().trim());
            map.put(FirebaseConstant.Enquiry.otherDetails, edtOtherDetails.getText().toString().trim());

            String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());

            map.put(FirebaseConstant.Enquiry.enquiryDate, currentDate);
            map.put(FirebaseConstant.Enquiry.enquiryTime, currentTime);

            String currentDate1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            String currentTime1 = new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(new Date());

            map.put(FirebaseConstant.Enquiry.creationDate, currentDate1+" "+currentTime1);

            rootRef.child(FirebaseConstant.Enquiry.Enquiry_TABLE).child(key).setValue(map).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hidePrd();
                    Cue.init()
                            .with(getActivity())
                            .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                            .setMessage(getString(R.string.add_enquiry_success))
                            .setType(Type.SUCCESS)
                            .show();
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

        } else {
            Helper.createDialog(getActivity());
        }
    }

    private void openDialog(int checkEDTSelection) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_dialog, null);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();

        EditText edt_spn_search = view.findViewById(R.id.edt_spn_search);
        final RecyclerView recycleCurrency = view.findViewById(R.id.recycleCurrency);
        final TextView txt_loading = view.findViewById(R.id.txt_loading);
        final TextView txtTitle = view.findViewById(R.id.txtTitle);

        edt_spn_search.setVisibility(View.GONE);
        txt_loading.setVisibility(View.GONE);
        recycleCurrency.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);

        if (checkEDTSelection == 6) {
            txtTitle.setText(getResources().getString(R.string.enquiry_source_withoutdash));
            importanceEnquirySourceAdapter = new Importance_EnquirySource_Adapter(getActivity(), sourceItemArrayList);
            if (sourceItemArrayList != null) {
                if (sourceItemArrayList.size() > 0) {
                    recycleCurrency.setLayoutManager(new LinearLayoutManager(getActivity()));
                    importanceEnquirySourceAdapter.setClickListener(this);
                    recycleCurrency.setAdapter(importanceEnquirySourceAdapter);
                    importanceEnquirySourceAdapter.notifyDataSetChanged();
                }
            }
        } else {
            txtTitle.setText(getResources().getString(R.string.importance));
            importanceEnquirySourceAdapter = new Importance_EnquirySource_Adapter(getActivity(), sourceItemArrayListImportance);
            if (sourceItemArrayListImportance != null) {
                if (sourceItemArrayListImportance.size() > 0) {
                    recycleCurrency.setLayoutManager(new LinearLayoutManager(getActivity()));
                    importanceEnquirySourceAdapter.setClickListener(this);
                    recycleCurrency.setAdapter(importanceEnquirySourceAdapter);
                    importanceEnquirySourceAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onClick(View view, int position) {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        if (checkEDTSelection == 6) {
            edtEnquirySource.setText(sourceItemArrayList.get(position).getName());
        } else {
            edtImportance.setText(sourceItemArrayListImportance.get(position).getName());
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
