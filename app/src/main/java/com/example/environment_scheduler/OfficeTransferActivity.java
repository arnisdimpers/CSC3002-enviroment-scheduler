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

public class OfficeTransferActivity extends AppCompatActivity {

    // Importing buttons, recyclerView and variables.
    Button gobackButton, confirmButton;
    Spinner spinner;
    ArrayList<String> dropList;
    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Meeting> list;

    // Main oncreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_transfer);

        // initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        confirmButton = findViewById(R.id.confirmButton);
        spinner = findViewById(R.id.spinner);

        // Initializing Firebase RecyclerView requirements
        recyclerView = findViewById(R.id.tranfersList);
        database = FirebaseDatabase.getInstance().getReference("Admin/officeRequests");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Initialized Spinner (drop down menu) dropList, should be updated each array 'dropList' iteration: dropAdapter.notifyDataSetChanged();
        dropList = new ArrayList<>();
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(OfficeTransferActivity.this, android.R.layout.simple_spinner_item, dropList);
        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // Create a list adapter for the recycler view to display the fields with CUSTOM adapter.
        // Custom adapter uses special @officetransfer parameter to know which field to create in the MyAdapter class.
        // We use custom parameters to use the appropriate XML item files.
        // For more on the adapter, look at MyAdapter class for more comments.
        list = new ArrayList<>(); // Create the list array
        myAdapter = new MyAdapter(this, list, "officetransfer"); // Populate the array
        // Set recyclerview to use this custom adapter.
        recyclerView.setAdapter(myAdapter);



        // Read Firebase to load in RecyclerView
        // We read the requests made in admin/officetransfers and use a custom
        // MyAdapter to display the recyclerView with firebase Strings(office,name)
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                // For loop navigating the Firebase to find the apropriate user via below 'if statement'
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    // Using the meeting class with built Getters to access the firebase information.
                    Meeting meeting = dataSnapshot.getValue(Meeting.class); // Reading from Users/ all sub folders to insert into List
                    list.add(meeting);

                    // Creating drop-down menu to book EMPTY only rooms
                    // Geteting the office and name of user.
                    String office = meeting.getOffice();
                    String name = meeting.getFullName();

                    // Adding to dropList array the name of existing user,
                    // dropList is the spinner which can be accessed and selected.
                    dropList.add(name);
                    // Update the spinner for newly added data.
                    dropAdapter.notifyDataSetChanged();
                    Log.d("Added: ", "" + office + " " + name);

                }
                // Update the adapter to hold newly added information
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Log.d("Spinner1", "Updating spinner adapter");
        // Spinner listener for drop down menu selection
        // Spinner holds the droplist array we filled out in above code with necessary values from Firebase.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Initialize the spinenr item.
                String value = "";

                // Access the spinner item on select.
                value = adapterView.getItemAtPosition(i).toString();
                Log.d("dropDownSelected: ", value);

                // Toast display to let user know what request was selected in the spinner menu.
                Toast.makeText(OfficeTransferActivity.this, "Selected Request: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Final setAdapter to update the above Spinner.
        // This is required for every iteration of the spinner.
        spinner.setAdapter(dropAdapter);


        // Back button to be transfered back to OfficeTransfer screen
        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(OfficeTransferActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(OfficeTransferActivity.this, AdminMainActivity.class);
                startActivity(gobackActivity);

            }
        });

        // Confirm button that gets the selected Spinner item and transfers user to new screen,
        // with the spinner item passed as well to the newly opened class.
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // Try catch - if requests exist or not to pass into the next class.
                // If spinner item exists and does not crash then we can proceed to pass in the
                // Spinner item to the next class, if it does not exist then program crashes.
                try {
                    // If successfully proceeds - opens next class with spinner item.
                    Intent confirmActivity = new Intent(OfficeTransferActivity.this, RequestAnswerActivity.class);
                    confirmActivity.putExtra("spinnerItem", spinner.getSelectedItem().toString()); // Transferring data to another class
                    Toast.makeText(OfficeTransferActivity.this, "Confirming selected request", Toast.LENGTH_SHORT).show();
                    startActivity(confirmActivity);
                } catch (NullPointerException e) {
                    // If crashes - no spinner item exists - then display toast with message.
                    Toast.makeText(OfficeTransferActivity.this, "No existing request made.", Toast.LENGTH_SHORT).show();
                }

            }
        });







    }

}
