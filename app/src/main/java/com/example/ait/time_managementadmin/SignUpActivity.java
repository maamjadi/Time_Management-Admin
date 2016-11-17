package com.example.ait.time_managementadmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by AIT on 11/7/16.
 */

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signUpButton;
    private EditText nameText;
    private EditText emailText;
    private EditText password;
    private EditText verifyPass;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String adminEmail = "";
    private String adminPass = "";


    private static final String TAG = "SignUp Editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Editors/Editors");

        progressDialog = new ProgressDialog(this);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        nameText = (EditText) findViewById(R.id.nameTextField);
        emailText = (EditText) findViewById(R.id.emailTextField);
        password = (EditText) findViewById(R.id.input_password);
        verifyPass = (EditText) findViewById(R.id.input_reEnterPassword);

        signUpButton.setOnClickListener(this);

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        String uid = user.getUid().toString();
        DatabaseReference myRef = firebaseDatabase.getReference("Admins").child(uid);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Admin is signed in");
                adminEmail = user.getEmail();
                passwordAlertDialog(user);
                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseAuth.signOut();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Current user isn't Admin", error.toException());
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user1 = firebaseAuth.getCurrentUser();
                if (user1 != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user1.getUid());
                    firebaseAuth.signOut();
                    signInAdmin();

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
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            DatabaseReference myRef = databaseReference.child(user.getUid());
                            myRef.child("Name").setValue(name);
                            myRef.child("Email").setValue(user.getEmail());

                            firebaseAuth.signOut();

                            signInAdmin();
                        }

                        // ...
                    }
                });

        progressDialog.hide();
    }

    public void passwordAlertDialog(final FirebaseUser user) {
        final boolean[] reAuthenticate = {false};
        while (reAuthenticate[0] == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("Enter your password to process");

            // Set up the input
            final EditText input = new EditText(SignUpActivity.this);

            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adminPass = input.getText().toString();

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
                                    Log.d(TAG, "User re-authenticated.");
                                    reAuthenticate[0] = true;
                                }
                            });
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent i = new Intent(SignUpActivity.this, ScrollingActivity.class);
                    startActivity(i);
                    reAuthenticate[0] = true;
                }
            });
            builder.show();
        }
    }

    private void signInAdmin() {
        firebaseAuth.signInWithEmailAndPassword(adminEmail, adminPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (task.isSuccessful()) {
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
