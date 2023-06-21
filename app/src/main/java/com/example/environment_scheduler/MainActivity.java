package com.example.environment_scheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    //importing buttons//
    private Button logoutButton, meetingButton, officeButton, myMeetingsButton, vacatedRoomsButton;
    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    CollectionReference usersRef;

    String FullName, PhoneNumber, UserEmail;

    TextView meetingroom;

    ArrayList<String> meetingArr;
    ArrayList<String> dateArr;

    DatabaseReference database2, database3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing buttons//
        logoutButton = findViewById(R.id.logoutButton);
        meetingButton = findViewById(R.id.meetingButton);
        officeButton = findViewById(R.id.officeButton);
        myMeetingsButton = findViewById(R.id.myMeetingsButton);
        vacatedRoomsButton = findViewById(R.id.vacatedRoomsButton);


        //  necessary components for every class //
        //  necessary components for every class //
        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();
        usersRef = fStore.collection("Users");
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();
        //make firedatabase connected to this instance
        FirebaseDatabase budgetText = FirebaseDatabase.getInstance();
        DatabaseReference db = budgetText.getReference("Users");
        database2 = FirebaseDatabase.getInstance().getReference("Users/" + currentUserID + "/bookedRooms");
        database3 = FirebaseDatabase.getInstance().getReference("Users/" + currentUserID + "/vacatedRooms");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        DatabaseReference userReference = databaseReference.child(currentUserID);
        //  necessary components for every class //
        //  necessary components for every class //

        meetingroom = findViewById(R.id.meetingroom);

        meetingArr = new ArrayList<>(); // Create the list array
        dateArr = new ArrayList<>(); // Create the list array


        Log.d("current user", currentUserID);

        //go into current users ID tree
        db.child(currentUserID).child("bookedRooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    String room = dataSnapshot.child("room").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String message = dataSnapshot.child("message").getValue(String.class);
                    String timeofbooking = dataSnapshot.child("timeofbooking").getValue(String.class);
                    // Check if meeting date is in the past
                    try {
                        if (date != null) {
                            // Meeting is only saved to MeetingArr - to display as upcoming "Next Meeting" if the date is AFTER currents
                            if (new SimpleDateFormat("yyyy-MM-dd").parse(date).after(new Date())) {

                                meetingArr.add("Date: " + date + " \nRoom: #" + room);
                                Log.d("MeetingBooked: ", meetingArr.get(0));

                            } else {
                                fStore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            FullName = task.getResult().get("FullName").toString();
                                            PhoneNumber = task.getResult().get("PhoneNumber").toString();
                                            UserEmail = task.getResult().get("UserEmail").toString();

                                            Log.d("UserDataREF", FullName + " " + PhoneNumber + " " + UserEmail);

                                        }
                                        // Map to hold user information for vacated rooms
                                        Map<String, Object> hopperUpdates = new HashMap<>();
                                        hopperUpdates.put("room", room);
                                        hopperUpdates.put("date", date);
                                        hopperUpdates.put("message", message);
                                        hopperUpdates.put("FullName", FullName);
                                        hopperUpdates.put("timeofbooking", timeofbooking);
                                        hopperUpdates.put("PhoneNumber", PhoneNumber);
                                        hopperUpdates.put("UserEmail", UserEmail);


                                        // Populate the vacatedRooms firebase with above collected information
                                        database3.getRef().child(room).updateChildren(hopperUpdates);

                                    }
                                });

                                //previous iteration of "if date < today, record the user details.
                                //Problem lied in access to Firestore to access users info
                                //Above code now works as intended to get FullName, PhoneNuber, UserEmail
//                                usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                FullName = document.getString("FullName");
//                                                PhoneNumber = document.getString("PhoneNumber");
//                                                UserEmail = document.getString("UserEmail");
//                                                Log.d("UserDataREF", FullName + " " + PhoneNumber + " " + UserEmail);
//
//                                            }
//
//                                        }
//                                        // Map to hold user information for vacated rooms
//                                        Map<String, Object> hopperUpdates = new HashMap<>();
//                                        hopperUpdates.put("room", room);
//                                        hopperUpdates.put("date", date);
//                                        hopperUpdates.put("message", message);
//                                        hopperUpdates.put("FullName", FullName);
//                                        hopperUpdates.put("timeofbooking", timeofbooking);
//                                        hopperUpdates.put("PhoneNumber", PhoneNumber);
//                                        hopperUpdates.put("UserEmail", UserEmail);
//
//
//                                        // Populate the vacatedRooms firebase with above collected information
//                                        database3.getRef().child(room).updateChildren(hopperUpdates);
//                                    }
//                                });

                                // Delete room from users bookedRooms database - transferred to vacatedRooms database
                                database2.getRef().child(key).removeValue();

                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    //dateArr.add(date);

                }

                boolean isSwapped = false;

                // If meeting exists - update the main menu UI "Next Meeting" box
                if (meetingArr.size() > 0) {
                    do {
                        isSwapped = false;
                        for (int i = 0; i < meetingArr.size() - 1; i++) {
                            if (meetingArr.get(i).compareTo(meetingArr.get(i + 1)) > 0) {
                                String temp = meetingArr.get(i + 1);
                                meetingArr.set(i + 1, meetingArr.get(i));
                                meetingArr.set(i, temp);
                                isSwapped = true;
                            }
                        }
                    } while ((isSwapped));

                    meetingroom.setText(meetingArr.get(0));
                } else { // If no meetings found - display message
                    meetingroom.setText("No upcoming meetings");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        meetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Going to Meeting page", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getApplicationContext(), MeetingActivity.class));
                Intent meetingActivity = new Intent(MainActivity.this, MeetingActivity.class);
                startActivity(meetingActivity);

            }
        });

        myMeetingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Going to Edit Meetings page", Toast.LENGTH_SHORT).show();
                Intent meetingActivity = new Intent(MainActivity.this, MeetingEditActivity.class);
                startActivity(meetingActivity);
            }
        });

        officeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Going to Office page", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getApplicationContext(), MeetingActivity.class));
                Intent meetingActivity = new Intent(MainActivity.this, OfficeActivity.class);
                startActivity(meetingActivity);
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginActivity);

            }
        });

        vacatedRoomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Going to Vacated Rooms page", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getApplicationContext(), MeetingActivity.class));
                Intent meetingActivity = new Intent(MainActivity.this, VacatedActivity.class);
                startActivity(meetingActivity);
            }
        });


    }
}