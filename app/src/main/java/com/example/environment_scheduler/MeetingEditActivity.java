package com.example.environment_scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MeetingEditActivity extends AppCompatActivity {

    Button gobackButton, confirmButton;

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    DatabaseReference database, database2;

    Spinner spinner;
    ArrayList<String> dropList;

    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    MyAdapter myAdapter;
    ArrayList<Meeting> list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_edit);

        confirmButton = findViewById(R.id.confirmButton);
        gobackButton = findViewById(R.id.gobackButton);

        spinner = findViewById(R.id.spinner);

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


        // Initializing Firebase RecyclerView requirements
        recyclerView = findViewById(R.id.meetingsList);
        database2 = FirebaseDatabase.getInstance().getReference("Users/" + currentUserID + "/bookedRooms");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialized Spinner (drop down menu) dropList, should be updated each array 'dropList' iteration: dropAdapter.notifyDataSetChanged();
        dropList = new ArrayList<>();
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(MeetingEditActivity.this, android.R.layout.simple_spinner_item, dropList);
        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        list = new ArrayList<>(); // Create the list array
        myAdapter = new MyAdapter(this, list, "bookedroom"); // Populate the array
        recyclerView.setAdapter(myAdapter);





        // Read Firebase to load in RecyclerView
        database2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    // add custom adapter and item.xml to display more information (editable as well via interface)
                    String room = dataSnapshot.child("room").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String message = dataSnapshot.child("message").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);

                    Meeting meeting = dataSnapshot.getValue(Meeting.class); // Reading from Users/ all sub folders to insert into List
                    list.add(meeting);

                    dropList.add(room);
                    dropAdapter.notifyDataSetChanged();
                    Log.d("Added: ", room);

                    // Creating drop-down menu to book EMPTY only rooms
//                    String room = meeting.getRoom();
//                    String vacancy = meeting.getVacancy();
//
//                    if (vacancy.equals("empty")) {
//                        dropList.add(room);
//                        dropAdapter.notifyDataSetChanged();
//                        Log.d("Added: ", room);
//                    }
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d("Spinner1", "Updating spinner adapter");
        // Spinner listener for drop down menu selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapterView.getItemAtPosition(i).toString();
                Log.d("dropDownSelected: ", value);
                Toast.makeText(MeetingEditActivity.this, "Selected Room: #" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Final setAdapter to update the above Spinner
        spinner.setAdapter(dropAdapter);



        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(MeetingEditActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                Intent confirmActivity = new Intent(MeetingEditActivity.this, MeetingEditConfirmActivity.class);
                confirmActivity.putExtra("spinnerItem", spinner.getSelectedItem().toString()); // Transferring data to another class
                startActivity(confirmActivity);

            }
        });



        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(MeetingEditActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(MeetingEditActivity.this, MainActivity.class);
                startActivity(gobackActivity);

            }
        });



    }

}
