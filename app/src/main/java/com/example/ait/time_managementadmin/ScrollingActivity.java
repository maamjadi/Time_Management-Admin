package com.example.ait.time_managementadmin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ScrollingActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReferenceSetting;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceEditors;
    private ArrayList<String> users;

    private Toolbar toolbar;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceSetting = firebaseDatabase.getReference("Editors");
        databaseReferenceUsers = firebaseDatabase.getReference("Users");
        databaseReferenceEditors = firebaseDatabase.getReference("Editors");
        users = new ArrayList<>();
        showAllUsers();
        showAllEditors();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettingReq(view);
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivity(new Intent(ScrollingActivity.this, LoginActivity.class));
                }
            }
        };
//        users.add("amraamir-shit");
//        users.add("alialsaeedi19@gmail.com-user");

//        StickyListHeadersListView stickyList = (StickyListHeadersListView) findViewById(R.id.list);
//        MyAdapter adapter = new MyAdapter(this, users);
//        stickyList.setAdapter(adapter);
        final ExpandableStickyListHeadersListView expandableStickyList = (ExpandableStickyListHeadersListView) findViewById(R.id.list);
        StickyListHeadersAdapter adapter = new MyAdapter(this, users);
        expandableStickyList.setAdapter(adapter);
        expandableStickyList.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                if (expandableStickyList.isHeaderCollapsed(headerId)) {
                    expandableStickyList.expand(headerId);
                } else {
                    expandableStickyList.collapse(headerId);
                }
            }
        });


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks context the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_exit) {
            signOutUser();
        }
        if (id == R.id.action_user) {
            Intent i = new Intent(ScrollingActivity.this, SignUpActivity.class);
            startActivity(i);
        }
        if (id == R.id.action_search) {

        }

        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }
        finish();
        Intent i = new Intent(ScrollingActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void updateSettingReq(final View view) {
        final boolean[] newSetting = {false};
        newSetting[0] = false;
        DatabaseReference myRef = databaseReferenceSetting.child("Settings");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Boolean> settings = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (settings != null) {
                    Snackbar snackbar = Snackbar.make(view, "You have new setting request", Snackbar.LENGTH_LONG)
                            .setAction("See", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(ScrollingActivity.this, RequestActivity.class);
                                    startActivity(i);
                                }
                            });
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(view, "There is no new setting request", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Fail to read setting database");
                Snackbar snackbar = Snackbar.make(view, "Failed to read the database", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void showAllUsers() {
        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> allUsers = (HashMap<String, Object>) dataSnapshot.getValue();
                for (String id : allUsers.keySet()) {
                    DataSnapshot tempSnapShot = dataSnapshot.child(id);
                    Map<String, Object> singleUser = (HashMap<String, Object>) tempSnapShot.getValue();
                    String user = singleUser.get("Email").toString();
                    users.add(user + "-user");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void showAllEditors(){
        databaseReferenceEditors = databaseReferenceEditors.child("Editors");
        databaseReferenceEditors.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> allEditors = (HashMap<String, Object>) dataSnapshot.getValue();
                for (String id : allEditors.keySet()) {
                    DataSnapshot tempSnapShot = dataSnapshot.child(id);
                    Map<String, Object> singleEdtior = (HashMap<String, Object>) tempSnapShot.getValue();
                    String user = singleEdtior.get("Email").toString();
                    users.add(user + "-editor");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
