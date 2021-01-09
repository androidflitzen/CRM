package com.flitzen.crm.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.flitzen.crm.R;
import com.flitzen.crm.utiles.FirebaseConstant;
import com.flitzen.crm.utiles.Helper;
import com.flitzen.crm.utiles.SharePref;
import com.fxn.cue.Cue;
import com.fxn.cue.enums.Type;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.txtSignUp)
    TextView txtSignUp;
    @BindView(R.id.img)
    ImageView img;
    @BindView(R.id.imgHidePass)
    ImageView imgHidePass;
    @BindView(R.id.imgShowPass)
    ImageView imgShowPass;
    @BindView(R.id.edtPass)
    EditText edtPass;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.cardGoogle)
    CardView cardGoogle;

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    DatabaseReference rootRef;
    ProgressDialog prd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = SharePref.getSharePref(LoginActivity.this);

        txtSignUp.setOnClickListener(this);
        imgHidePass.setOnClickListener(this);
        imgShowPass.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        cardGoogle.setOnClickListener(this);

        // Glide.with(this).load(R.drawable.ic_shape_1).into(img);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSignUp:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;

            case R.id.imgHidePass:
                edtPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                edtPass.setSelection(edtPass.length());
                imgShowPass.setVisibility(View.VISIBLE);
                imgHidePass.setVisibility(View.GONE);
                break;

            case R.id.imgShowPass:
                edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                edtPass.setSelection(edtPass.length());
                imgHidePass.setVisibility(View.VISIBLE);
                imgShowPass.setVisibility(View.GONE);
                break;

            case R.id.btnSignIn:
                simpleLogin();
                break;

            case R.id.cardGoogle:
                if(Helper.isOnline(LoginActivity.this)){
                    signIn();
                } else {
                    Helper.createDialog(LoginActivity.this);
                }

                break;
        }
    }

    private void simpleLogin() {
        if (edtEmail.getText().toString().trim().isEmpty()) {
            edtEmail.setError(getString(R.string.enter_email));
            edtEmail.requestFocus();
            return;
        } else if (!edtEmail.getText().toString().trim().matches(Helper.emailPattern)) {
            edtEmail.setError(getString(R.string.enter_valide_email));
            edtEmail.requestFocus();
            return;
        } else if (edtPass.getText().toString().trim().isEmpty()) {
            edtPass.setError(getString(R.string.enter_password));
            edtPass.requestFocus();
            return;
        } else {
            if(Helper.isOnline(LoginActivity.this)){
                showPrd();
                mAuth.signInWithEmailAndPassword(edtEmail.getText().toString().trim(),edtPass.getText().toString().trim())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                hidePrd();
                                if (task.isSuccessful()) {

                                    Cue.init()
                                            .with(LoginActivity.this)
                                            .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                            .setMessage(getString(R.string.login_successfully))
                                            .setType(Type.SUCCESS)
                                            .show();

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(SharePref.isLoggedIn, true);
                                    // editor.putString(SharePref.userName, edtUserName.getText().toString());
                                    editor.putString(SharePref.password, edtPass.getText().toString().trim());
                                    editor.putString(SharePref.gstType, getString(R.string.simple_user));
                                    editor.commit();

                                    startActivity(new Intent(LoginActivity.this, AddCompanyActivity.class));
                                    finish();
                                }
                                else {
                                    Cue.init()
                                            .with(LoginActivity.this)
                                            .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                            .setMessage(getString(R.string.invalid_email_pass))
                                            .setType(Type.DANGER)
                                            .show();

                                }
                            }
                        });
            }
            else {
                Helper.createDialog(LoginActivity.this);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        showPrd();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                Query queryLogin = rootRef.child(FirebaseConstant.User.User_TABLE).orderByChild(FirebaseConstant.User.user_email).equalTo(user.getEmail());
                                queryLogin.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            //create new user

                                            String key = rootRef.child(FirebaseConstant.User.User_TABLE).push().getKey();
                                            Map<String, Object> map = new HashMap<>();
                                            map.put(FirebaseConstant.User.id, key);
                                            map.put(FirebaseConstant.User.user_name, user.getDisplayName());
                                            map.put(FirebaseConstant.User.user_email, user.getEmail());
                                            map.put(FirebaseConstant.User.user_password, " ");

                                            rootRef.child(FirebaseConstant.User.User_TABLE).child(key).setValue(map).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    hidePrd();
                                                    Cue.init()
                                                            .with(LoginActivity.this)
                                                            .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                                            .setMessage(getString(R.string.login_successfully))
                                                            .setType(Type.SUCCESS)
                                                            .show();

                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putBoolean(SharePref.isLoggedIn, true);
                                                    editor.putString(SharePref.userName, user.getDisplayName());
                                                    editor.putString(SharePref.userId, key);
                                                    editor.putString(SharePref.userEmail, user.getEmail());
                                                    editor.putString(SharePref.gstType, getString(R.string.google));
                                                    editor.commit();

                                                    startActivity(new Intent(LoginActivity.this, AddCompanyActivity.class));
                                                    finish();
                                                }

                                            }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    hidePrd();
                                                    Cue.init()
                                                            .with(LoginActivity.this)
                                                            .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                                            .setMessage(getString(R.string.try_later))
                                                            .setType(Type.DANGER)
                                                            .show();
                                                }

                                            });

                                        } else {
                                            hidePrd();
                                            for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                                                Cue.init()
                                                        .with(LoginActivity.this)
                                                        .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                                        .setMessage(getString(R.string.login_successfully))
                                                        .setType(Type.SUCCESS)
                                                        .show();

                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean(SharePref.isLoggedIn, true);
                                                editor.putString(SharePref.userName, user.getDisplayName());
                                                editor.putString(SharePref.userId, npsnapshot.child(FirebaseConstant.User.id).getValue().toString());
                                                editor.putString(SharePref.userEmail, user.getEmail());
                                                editor.putString(SharePref.gstType, getString(R.string.google));
                                                editor.commit();

                                                startActivity(new Intent(LoginActivity.this, AddCompanyActivity.class));
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        hidePrd();
                                        Cue.init()
                                                .with(LoginActivity.this)
                                                .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                                .setMessage(getString(R.string.somthing_went))
                                                .setType(Type.DANGER)
                                                .show();
                                    }
                                });
                            }
                        } else {
                            hidePrd();
                            Cue.init()
                                    .with(LoginActivity.this)
                                    .setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM)
                                    .setMessage(getString(R.string.somthing_went))
                                    .setType(Type.DANGER)
                                    .show();

                        }

                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void showPrd() {
        prd = new ProgressDialog(LoginActivity.this);
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