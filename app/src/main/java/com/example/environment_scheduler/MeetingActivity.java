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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MeetingActivity extends AppCompatActivity {
    // Importing buttons, recyclerView
    Button gobackButton, confirmButton;

    Spinner spinner;
    ArrayList<String> dropList;

    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Meeting> list;


    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        //initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        confirmButton = findViewById(R.id.confirmButton);
        spinner = findViewById(R.id.spinner);

        // Initializing Firebase RecyclerView requirements
        recyclerView = findViewById(R.id.meetingsList);
        database = FirebaseDatabase.getInstance().getReference("MeetingRooms");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Initialized Spinner (drop down menu) dropList, should be updated each array 'dropList' iteration: dropAdapter.notifyDataSetChanged();
        dropList = new ArrayList<>();
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(MeetingActivity.this, android.R.layout.simple_spinner_item, dropList);
        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        list = new ArrayList<>(); // Create the list array
        myAdapter = new MyAdapter(this, list, "roomvacancy"); // Populate the array
        recyclerView.setAdapter(myAdapter);



        // Read Firebase to load in RecyclerView
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Meeting meeting = dataSnapshot.getValue(Meeting.class); // Reading from Users/ all sub folders to insert into List
                    list.add(meeting);

                    // Creating drop-down menu to book EMPTY only rooms
                    String room = meeting.getRoom();
                    String vacancy = meeting.getVacancy();

                    if (vacancy.equals("empty")) {
                        dropList.add(room);
                        dropAdapter.notifyDataSetChanged();
                        Log.d("Added: ", room);
                    }
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
                Toast.makeText(MeetingActivity.this, "Selected Room: #" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Final setAdapter to update the above Spinner
        spinner.setAdapter(dropAdapter);








        //  necessary components for every class //
        //  necessary components for every class //
        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();
        //make firedatabase connected to this instance
        FirebaseDatabase budgetText = FirebaseDatabase.getInstance();
        DatabaseReference db = budgetText.getReference("Users");
        //  necessary components for every class //
        //  necessary components for every class //



        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(MeetingActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(MeetingActivity.this, MainActivity.class);
                startActivity(gobackActivity);

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                try {
                    Toast.makeText(MeetingActivity.this, "Confirming selected meeting", Toast.LENGTH_SHORT).show();
                    Intent confirmActivity = new Intent(MeetingActivity.this, MeetingConfirmActivity.class);
                    confirmActivity.putExtra("spinnerItem", spinner.getSelectedItem().toString()); // Transferring data to another class
                    startActivity(confirmActivity);
                }
                catch(NullPointerException e){
                    Toast.makeText(MeetingActivity.this, "Please select an available room.", Toast.LENGTH_SHORT).show();
                }

            }
        });







    }

}
