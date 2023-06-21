package com.example.environment_scheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MeetingEditConfirmActivity extends AppCompatActivity {

    String spinnerItem, key;
    Button gobackButton, confirmButton;
    TextView textView;
    TextInputEditText description1, description2, description3;
    CheckBox checkBox;
    Boolean checked = false; //default

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    DatabaseReference database, database2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meeting_edit_confirm);

        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");
        Log.d("spinnerItem: ", "" + spinnerItem);

        gobackButton = findViewById(R.id.gobackButton);
        confirmButton = findViewById(R.id.confirmButton);
        textView = findViewById(R.id.textView);
        description1 = findViewById(R.id.description1);
        description2 = findViewById(R.id.description2);
        description3 = findViewById(R.id.description3);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        //  necessary components for every class //
        //  necessary components for every class //
        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();
        //make firedatabase connected to this instance
        FirebaseDatabase budgetText = FirebaseDatabase.getInstance();
        DatabaseReference db = budgetText.getReference("Users");
        database = FirebaseDatabase.getInstance().getReference("MeetingRooms");
        //  necessary components for every class //
        //  necessary components for every class //

        db.child(currentUserID).child("bookedRooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String room = dataSnapshot.child("room").getValue(String.class);
                    Log.d("spinnerITEM: ", room + " " + spinnerItem);
                    if (room.equals(spinnerItem)) {
                        // Additional info for 'vacatedRooms'
//                        String FullName = dataSnapshot.child("FullName").getValue(String.class);
//                        String PhoneNumber = dataSnapshot.child("PhoneNumber").getValue(String.class);
                        key = dataSnapshot.getKey();
                        String date = dataSnapshot.child("date").getValue(String.class);
                        String message = dataSnapshot.child("message").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String timeofbooking = dataSnapshot.child("timeofbooking").getValue(String.class);

                        textView.setText("Please update details below and press 'Confirm' \n\nBooked on: " + timeofbooking + "\n\nRoom number: #" + room);
                        description1.setText(date);
                        description2.setText(message);
                        description3.setText(email);

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    checked = true;
                    Log.d("checked: ", "YES checked");
                } else {
                    checked = false;
                    Log.d("checked: ", "NOT checked");
                }

            }
        });

        gobackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MeetingEditConfirmActivity.this, "Going back to edit meeting page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(MeetingEditConfirmActivity.this, MeetingEditActivity.class);
                startActivity(gobackActivity);

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("reading KEY: ", ""+key);

                // Checkbox to cancel meeting is NOT ticked
                if (checked == false) {
                    Log.d("CHECKED", "UPDATING THE MEETING");
                    Map<String, Object> meetingInfo = new HashMap<>();
                    meetingInfo.put("date", description1.getText().toString());
                    meetingInfo.put("message", description2.getText().toString());
                    meetingInfo.put("email", description3.getText().toString());

                    db.child(currentUserID).child("bookedRooms/" + key).updateChildren(meetingInfo);
                }
                else {
                    // Checkbox to cancel meeting IS ticked
                    Log.d("CHECKED", "CANCELLING THE MEETING");
                    db.child(currentUserID).child("bookedRooms/" + key +"/date").setValue("0000-00-00");
                }



                // Popup yes/no window after updating firebase above
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Toast.makeText(MeetingEditConfirmActivity.this, "Successfully updated! Going back to main page", Toast.LENGTH_SHORT).show();
                                Intent confirmActivity = new Intent(MeetingEditConfirmActivity.this, MainActivity.class);
                                startActivity(confirmActivity);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MeetingEditConfirmActivity.this);
                builder.setMessage("Updated successfully. \nGo to main page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();




            }
        });

    }
}