package com.flitzen.crm.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flitzen.crm.R;
import com.flitzen.crm.activity.AddCompanyActivity;
import com.flitzen.crm.activity.LoginActivity;
import com.flitzen.crm.adapter.EnquiryAdapter;
import com.flitzen.crm.items.EnquiryItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EnquiryFragment extends Fragment implements View.OnClickListener, EnquiryAdapter.ItemClickListener {

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewEnquiry=inflater.inflate(R.layout.fragment_enquiry, container, false);
        recyclerEnquiry =  viewEnquiry.findViewById(R.id.recyclerEnquiry);
        noDataLayout =  viewEnquiry.findViewById(R.id.noDataLayout);
       // ButterKnife.bind(getContext(),viewEnquiry);
        enquiryAdapter  = new EnquiryAdapter(getActivity(),enquiryItemArrayList);
        recyclerEnquiry.setHasFixedSize(true);
        recyclerEnquiry.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerEnquiry.setAdapter(enquiryAdapter);
        enquiryAdapter.setClickListener(this);

        rootRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = SharePref.getSharePref(getActivity());

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
        cardAddEnquiry.setVisibility(View.VISIBLE);
        cardAddEnquiry.setOnClickListener(this);

        if (Helper.isOnline(getActivity())) {
            getEnquiryList();
        } else {
            Helper.createDialog(getActivity());
        }

    }

    private void getEnquiryList() {
        System.out.println("=========sharedPreferences.getString(SharePref.userId,\"\")   "+sharedPreferences.getString(SharePref.userId,""));
        Query queryLogin = rootRef.child(FirebaseConstant.Enquiry.Enquiry_TABLE).orderByChild(FirebaseConstant.Enquiry.userId).equalTo(sharedPreferences.getString(SharePref.userId,""));
        queryLogin.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    //create new user
                    recyclerEnquiry.setVisibility(View.GONE);
                    noDataLayout.setVisibility(View.VISIBLE);

                } else {
                    enquiryItemArrayList.clear();
                    for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                        System.out.println("========for in");
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
        if(view.getId()==R.id.cardAddFollowUp){
            openAddFollowUpDialog();
        }
        else {
            loadFragment(new EnquiryDetailsFragment(),getResources().getString(R.string.enquiry_details));
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

    public void showPrd() {
        prd = new ProgressDialog(getActivity());
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
