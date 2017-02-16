package com.example.ait.time_managementadmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by AIT context 11/7/16.
 */

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signUpButton;
    private EditText nameText;
    private EditText emailText;
    private EditText password;
    private EditText verifyPass;
    private ProgressDialog progressDialog;
    private LinearLayout signupView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String adminEmail = "";
    private String adminPass = "";
    private String adminPassFromCache = "";
    private boolean checkState = false;
    final Context context = this;


    private static final String TAG = "SignUp Editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Editors/Editors");

        progressDialog = new ProgressDialog(this);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        nameText = (EditText) findViewById(R.id.nameTextField);
        emailText = (EditText) findViewById(R.id.editorEmailTextField);
        password = (EditText) findViewById(R.id.input_password);
        verifyPass = (EditText) findViewById(R.id.input_reEnterPassword);
        signupView = (LinearLayout) findViewById(R.id.signupView);

        signUpButton.setOnClickListener(this);

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            readPasswordCache();
            adminEmail = user.getEmail();
            String uid = user.getUid().toString();
            DatabaseReference myRef = firebaseDatabase.getReference("Admins/Admins").child(uid);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "Admin is signed in");
                        passwordAlertDialog(user);
                    } else {
                        // Failed to read value
                        Log.w(TAG, "Current user isn't Admin");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Fail to read data", error.toException());
                }
            });
        }

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user1 = firebaseAuth.getCurrentUser();
                if (user1 != null) {
                    visibility(true);
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user1.getUid());
                    if (checkState == true) {
                        firebaseAuth.signOut();
                        signInAdmin();
                    }
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == signUpButton) {
            addUser();
        }
    }

    @Override
    public void finish() {
        super.finish();
        signInAdmin();
    }

    private boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String pass = password.getText().toString();
        String verPass = verifyPass.getText().toString();
        String name = nameText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailText.setError("Required");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (TextUtils.isEmpty(name)) {
            nameText.setError("Required");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (TextUtils.isEmpty(pass)) {
            password.setError("Required");
            valid = false;
        } else {
            password.setError(null);
            if (!(pass.equals(verPass))) {
                verifyPass.setError("Password isn't match");
                valid = false;
            } else {
                verifyPass.setError(null);
            }
        }

        return valid;
    }

    private void visibility(boolean value) {
        int visible = 0;
        if (value == true) {
            visible = View.VISIBLE;
        } else {
            visible = View.INVISIBLE;
        }
        signupView.setVisibility(visible);
    }

    private void addUser() {
        String email = emailText.getText().toString();
        String pass = password.getText().toString();
        final String name = nameText.getText().toString();

        Log.d(TAG, "signIn:" + email);
        if (!validate()) {
            return;
        }

        progressDialog.setMessage("Registering the Editor...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!(task.isSuccessful())) {
                            Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                DatabaseReference myRef1 = databaseReference.child(user.getUid());
                                myRef1.child("Name").setValue(name);
                                myRef1.child("Email").setValue(user.getEmail());
                                firebaseAuth.signOut();
                            }

                            signInAdmin();
                        }

                        // ...
                    }
                });

        progressDialog.hide();
    }

    public void passwordAlertDialog(final FirebaseUser user) {

        adminEmail = user.getEmail();
        readPasswordCache();

                if (!(TextUtils.isEmpty(adminPass))) {

                    // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(adminEmail, adminPass);

                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    checkState = true;
                                    Log.d(TAG, "User re-authenticated.");
                                    visibility(true);
                                    firebaseAuth.signOut();
                                }
                            });
                }
            }


//        alertDialogBuilderUserInput.setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(final DialogInterface dialog, int which) {
//                if (!(TextUtils.isEmpty(adminPass))) {
//
//                    // Get auth credentials from the user for re-authentication. The example below shows
//                    // email and password credentials but there are multiple possible providers,
//                    // such as GoogleAuthProvider or FacebookAuthProvider.
//                    AuthCredential credential = EmailAuthProvider
//                            .getCredential(adminEmail, adminPass);
//
//                    // Prompt the user to re-provide their sign-in credentials
//                    user.reauthenticate(credential)
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    savePasswordCache(adminPass);
//                                    checkState = true;
//                                    Log.d(TAG, "User re-authenticated.");
//                                    visibility(true);
//                                    firebaseAuth.signOut();
//                                    dialog.dismiss();
//                                }
//                            });
//                }
//            }
//        });
//        alertDialogBuilderUserInput.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//                Intent i = new Intent(SignUpActivity.this, ScrollingActivity.class);
//                startActivity(i);
//            }
//        });




    private void signInAdmin() {
        readPasswordCache();
        if (!(adminEmail.isEmpty()) && !(adminPassFromCache.isEmpty())) {
            firebaseAuth.signInWithEmailAndPassword(adminEmail, adminPassFromCache).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                    checkState = false;

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!(task.isSuccessful())) {
                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                    } else {
                        finish();
                        Intent i = new Intent(SignUpActivity.this, ScrollingActivity.class);
                        startActivity(i);
                    }
                }
            });
        }
    }

//    private void savePasswordCache(String data) {
//        File file;
//        FileOutputStream outputStream;
//        try {
//            // file = File.createTempFile("MyCache", null, getCacheDir()); //pass getFilesDir() to save file
//            file = new File(getCacheDir(), "PasswordCache");
//
//            outputStream = new FileOutputStream(file);
//            outputStream.write(data.getBytes());
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void readPasswordCache() {
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "PasswordCache"); // Pass getFilesDir() to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            Log.d(TAG, buffer.toString());
            adminPass = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
