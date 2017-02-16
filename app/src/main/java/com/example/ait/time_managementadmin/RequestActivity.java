package com.example.ait.time_managementadmin;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RequestActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReferenceEditorSettings;
    private DatabaseReference databaseReferenceAdminSettings;

    private FloatingActionButton accepted;
    private FloatingActionButton ignored;
    private DatabaseReference settingsReference;
    private CheckBox mode24Hours;
    private CheckBox modeDarkTime;
    private CheckBox modeDarkDate;
    private CheckBox modeCustomAccentTime;
    private CheckBox modeCustomAccentDate;
    private CheckBox vibrateTime;
    private CheckBox vibrateDate;
    private CheckBox dismissTime;
    private CheckBox dismissDate;
    private CheckBox titleTime;
    private CheckBox titleDate;
    private CheckBox showYearFirst;
    private CheckBox enableSeconds;
    private CheckBox enableMinutes;
    private CheckBox limitTimes;
    private CheckBox limitDates;
    private CheckBox disableDates;
    private CheckBox highlightDates;
    private HashMap<String, Boolean> defaultSettings;



    private static final String TAG = "Update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceEditorSettings = firebaseDatabase.getReference("Editors/Settings");
        databaseReferenceAdminSettings = firebaseDatabase.getReference("Admins/Settings");
        settingsReference = firebaseDatabase.getReference("Editors");


        mode24Hours = (CheckBox) findViewById(R.id.mode_24_hours);
        modeDarkTime = (CheckBox) findViewById(R.id.mode_dark_time);
        modeDarkDate = (CheckBox) findViewById(R.id.mode_dark_date);
        modeCustomAccentTime = (CheckBox) findViewById(R.id.mode_custom_accent_time);
        modeCustomAccentDate = (CheckBox) findViewById(R.id.mode_custom_accent_date);
        vibrateTime = (CheckBox) findViewById(R.id.vibrate_time);
        vibrateDate = (CheckBox) findViewById(R.id.vibrate_date);
        dismissTime = (CheckBox) findViewById(R.id.dismiss_time);
        dismissDate = (CheckBox) findViewById(R.id.dismiss_date);
        titleTime = (CheckBox) findViewById(R.id.title_time);
        titleDate = (CheckBox) findViewById(R.id.title_date);
        showYearFirst = (CheckBox) findViewById(R.id.show_year_first);
        enableSeconds = (CheckBox) findViewById(R.id.enable_seconds);
        enableMinutes = (CheckBox) findViewById(R.id.enable_minutes);
        limitTimes = (CheckBox) findViewById(R.id.limit_times);
        limitDates = (CheckBox) findViewById(R.id.limit_dates);
        disableDates = (CheckBox) findViewById(R.id.disable_dates);
        highlightDates = (CheckBox) findViewById(R.id.highlight_dates);
        defaultSettings = new HashMap<>();

        accepted = (FloatingActionButton) findViewById(R.id.acceptedSetting);
        ignored = (FloatingActionButton) findViewById(R.id.ignoredSetting);
        accepted.setOnClickListener(this);
        ignored.setOnClickListener(this);
        getDefaultSettings();


    }

    @Override
    public void onClick(View view) {
        if (view == accepted) {
            publishNewSettings();
        }
        else if (view == ignored) {
            deleteNewSettings();
        }
    }

    private void publishNewSettings() {
        databaseReferenceEditorSettings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    databaseReferenceAdminSettings.setValue(defaultSettings);
                    Log.d(TAG, "Published");
                    Toast.makeText(RequestActivity.this, "New Settings Successfully Published",
                            Toast.LENGTH_SHORT).show();
                    deleteNewSettings();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void deleteNewSettings() {
        if (databaseReferenceEditorSettings.getKey() != null) {
            databaseReferenceEditorSettings.getRef().removeValue();
            Toast.makeText(RequestActivity.this, "New Settings have been deleted",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void getDefaultSettings() {
        settingsReference = settingsReference.child("Settings");
        settingsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                defaultSettings = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (defaultSettings != null) {
                    for (String choice : defaultSettings.keySet()) {
                        switch (choice) {
                            case "disableDates":
                                disableDates.setChecked(defaultSettings.get(choice));
                                break;
                            case "dismissDate":
                                dismissDate.setChecked(defaultSettings.get(choice));
                                break;
                            case "enableMinutes":
                                enableMinutes.setChecked(defaultSettings.get(choice));
                                break;
                            case "highlightDates":
                                highlightDates.setChecked(defaultSettings.get(choice));
                                break;
                            case "enableSeconds":
                                enableSeconds.setChecked(defaultSettings.get(choice));
                                break;
                            case "limitDates":
                                limitDates.setChecked(defaultSettings.get(choice));
                                break;
                            case "limitTimes":
                                limitTimes.setChecked(defaultSettings.get(choice));
                                break;
                            case "mode24Hours":
                                mode24Hours.setChecked(defaultSettings.get(choice));
                                break;
                            case "modeCustomAccentDate":
                                modeCustomAccentDate.setChecked(defaultSettings.get(choice));
                                break;
                            case "modeCustomAccentTime":
                                modeCustomAccentTime.setChecked(defaultSettings.get(choice));
                                break;
                            case "modeDarkDate":
                                modeDarkDate.setChecked(defaultSettings.get(choice));
                                break;
                            case "modeDarkTime":
                                modeDarkTime.setChecked(defaultSettings.get(choice));
                                break;

                            case "showYearFirst":
                                showYearFirst.setChecked(defaultSettings.get(choice));
                                break;
                            case "titleDate":
                                titleDate.setChecked(defaultSettings.get(choice));
                                break;

                            case "titleTime":
                                titleTime.setChecked(defaultSettings.get(choice));
                                break;
                            case "vibrateDate":
                                vibrateDate.setChecked(defaultSettings.get(choice));
                                break;

                            case "vibrateTime":
                                vibrateTime.setChecked(defaultSettings.get(choice));
                                break;
                            default:
                                break;

                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
