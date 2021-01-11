package com.flitzen.crm.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flitzen.crm.MainActivity;
import com.flitzen.crm.R;
import com.flitzen.crm.adapter.CurrencyAdapter;
import com.flitzen.crm.items.CurrencyItem;
import com.flitzen.crm.utiles.FirebaseConstant;
import com.flitzen.crm.utiles.Helper;
import com.flitzen.crm.utiles.Permission;
import com.flitzen.crm.utiles.SharePref;
import com.fxn.cue.Cue;
import com.fxn.cue.enums.Type;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddCompanyActivity extends AppCompatActivity implements View.OnClickListener, CurrencyAdapter.ItemClickListener {

    @BindView(R.id.imgLogo)
    ImageView imgLogo;
    @BindView(R.id.imgPlus)
    ImageView imgPlus;
    @BindView(R.id.edtCompanyName)
    EditText edtCompanyName;
    @BindView(R.id.edtBaseCurrency)
    EditText edtBaseCurrency;
    @BindView(R.id.checkProduct)
    AppCompatCheckBox checkProduct;
    @BindView(R.id.checkService)
    AppCompatCheckBox checkService;
    @BindView(R.id.btnSave)
    Button btnSave;

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private ArrayList<CurrencyItem> currencyArray = new ArrayList<>();
    DatabaseReference databaseReference;
    CurrencyAdapter currencyAdapter;
    String TAG = "AddCompanyActivity";
    ArrayList<CurrencyItem> temp = new ArrayList<>();
    AlertDialog alertDialog;
    DatabaseReference rootRef;
    ProgressDialog prd;
    String companyTypeSelection="";
    Uri selectedImage= Uri.parse("");
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);

        ButterKnife.bind(this);

        imgLogo.setOnClickListener(this);
        imgPlus.setOnClickListener(this);
        edtBaseCurrency.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        rootRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = SharePref.getSharePref(AddCompanyActivity.this);

        getCurrency();

    }

    private void getCurrency() {

        Query query = databaseReference.child(FirebaseConstant.Currency.Currency_TABLE).orderByKey();
        //databaseReference.keepSynced(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currencyArray.clear();
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                            CurrencyItem currencyItem = new CurrencyItem();
                            currencyItem.setId(npsnapshot.child(FirebaseConstant.Currency.id).getValue().toString());
                            currencyItem.setCurrencyName(npsnapshot.child(FirebaseConstant.Currency.currencyName).getValue().toString());

                            currencyArray.add(currencyItem);
                            temp.add(currencyItem);
                            if (currencyAdapter != null) {
                                currencyAdapter.notifyDataSetChanged();
                            }
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
                        .with(AddCompanyActivity.this)
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(databaseError.getMessage().toString())
                        .setType(Type.DANGER)
                        .show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgLogo:
                pickImage();
                break;

            case R.id.imgPlus:
                pickImage();
                break;

            case R.id.edtBaseCurrency:
                openCurrencyDialog();
                break;

            case R.id.btnSave:
                checkValidation();
                break;
        }
    }

    private void checkValidation() {
        companyTypeSelection="";
        if(checkProduct.isChecked()){
            companyTypeSelection="0";
        }

        if(checkService.isChecked()) {
            if(checkProduct.isChecked()){
                companyTypeSelection=companyTypeSelection+",1";
            }
            else {
                companyTypeSelection="1";
            }
        }

        if (edtCompanyName.getText().toString().trim().isEmpty()) {
            edtCompanyName.setError(getString(R.string.enter_company_name));
            edtCompanyName.requestFocus();
            return;
        } else if (String.valueOf(selectedImage).equals("")) {
            Cue.init()
                    .with(AddCompanyActivity.this)
                    .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                    .setMessage(getString(R.string.select_company_logo))
                    .setType(Type.DANGER)
                    .show();
            return;
        }else if (edtBaseCurrency.getText().toString().trim().isEmpty()) {
            edtBaseCurrency.setError(getString(R.string.select_currency));
            edtBaseCurrency.requestFocus();
            return;
        }else if (companyTypeSelection.trim().equals("")) {
            Cue.init()
                    .with(AddCompanyActivity.this)
                    .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                    .setMessage(getString(R.string.select_company_type))
                    .setType(Type.DANGER)
                    .show();
            return;
        }else {
            if(String.valueOf(selectedImage).equals("")){
                if(Helper.isOnline(AddCompanyActivity.this)){
                    showPrd();
                    addCompany("");
                }
                else {
                    Helper.createDialog(AddCompanyActivity.this);
                }
            }
            else {
                if(Helper.isOnline(AddCompanyActivity.this)){
                    showPrd();
                    uploadImage();
                }
                else {
                    Helper.createDialog(AddCompanyActivity.this);
                }
            }
        }
    }

    private void uploadImage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        final StorageReference sRef = storageReference.child(FirebaseConstant.companyLogoFolder + UUID.randomUUID().toString());
        sRef.putFile(selectedImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onSuccess(Uri uri) {
                                if(Helper.isOnline(AddCompanyActivity.this)){
                                    addCompany(uri.toString());
                                }
                                else {
                                    Helper.createDialog(AddCompanyActivity.this);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        hidePrd();
                        Toast.makeText(AddCompanyActivity.this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.e("progress  ", String.valueOf(progress));
                    }
                });
    }

    private void addCompany(String logoString) {

        String key = rootRef.child(FirebaseConstant.Company.Company_TABLE).push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put(FirebaseConstant.Company.id, key);
        map.put(FirebaseConstant.Company.companyName, edtCompanyName.getText().toString().trim());
        map.put(FirebaseConstant.Company.companyLogo, logoString);
        map.put(FirebaseConstant.Company.baseCurrency, edtBaseCurrency.getText().toString().trim());
        map.put(FirebaseConstant.Company.companyType, companyTypeSelection);

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(new Date());

        map.put(FirebaseConstant.Company.creationDate, currentDate+" "+currentTime);

        rootRef.child(FirebaseConstant.Company.Company_TABLE).child(key).setValue(map).addOnCompleteListener(AddCompanyActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                hidePrd();
                selectedImage= Uri.parse("");
                Cue.init()
                        .with(AddCompanyActivity.this)
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(getString(R.string.company_add))
                        .setType(Type.SUCCESS)
                        .show();


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SharePref.isAddCompany, true);
                editor.commit();

                startActivity(new Intent(AddCompanyActivity.this, HomeActivity.class));
                finish();
            }

        }).addOnFailureListener(AddCompanyActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hidePrd();
                Cue.init()
                        .with(AddCompanyActivity.this)
                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                        .setMessage(getString(R.string.try_later))
                        .setType(Type.DANGER)
                        .show();
            }

        });
    }

    private void openCurrencyDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddCompanyActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_dialog, null);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();

        EditText edt_spn_search = view.findViewById(R.id.edt_spn_search);
        final RecyclerView recycleCurrency = view.findViewById(R.id.recycleCurrency);
        final TextView txt_loading = view.findViewById(R.id.txt_loading);
        //ArrayList<ProductModel.Result> list = new ArrayList<>();

        edt_spn_search.setHint("Search Currency");

        //list.clear();

        txt_loading.setVisibility(View.GONE);
        recycleCurrency.setVisibility(View.VISIBLE);

        currencyAdapter = new CurrencyAdapter(AddCompanyActivity.this, currencyArray);
        if (currencyArray != null) {
            if (currencyArray.size() > 0) {
                recycleCurrency.setLayoutManager(new LinearLayoutManager(this));
                currencyAdapter.setClickListener(this);
                recycleCurrency.setAdapter(currencyAdapter);
                currencyAdapter.notifyDataSetChanged();
            }
        }


        edt_spn_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void filter(String text) {
        temp = new ArrayList();
        for (CurrencyItem d : currencyArray) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getCurrencyName().contains(text)) {
                temp.add(d);
            }
        }
        //update recyclerview
        currencyAdapter.updateList(temp);
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Permission.hasPermissions(AddCompanyActivity.this, permissions)) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 0);
            } else {
                Permission.requestPermissions(AddCompanyActivity.this, permissions);
            }
        } else {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    selectedImage = data.getData();
                    imgLogo.setImageURI(selectedImage);
                    imgPlus.setVisibility(View.GONE);
                    //imgEdit.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onClick(View view, int position) {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        edtBaseCurrency.setText(temp.get(position).getCurrencyName());
    }

    public void showPrd() {
        prd = new ProgressDialog(AddCompanyActivity.this,R.style.MyAlertDialogStyle);
        prd.setMessage("Please wait...");
        prd.setCancelable(false);
        prd.show();
    }

    public void hidePrd() {
        if(prd!=null){
            if (prd.isShowing()){
                prd.dismiss();
            }
        }
    }
}