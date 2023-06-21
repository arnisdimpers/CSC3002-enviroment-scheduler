package com.example.environment_scheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.internal.http.RequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity {

    // Initializing button, text and variables.
    Button gobackButton, confirmButton;

    // Firebase initialization to access Firestore and Fire Database.
    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    DatabaseReference database, database2, database3;

    // Initializing spinner variables.
    Spinner spinner;
    ArrayList<String> dropList;
    TextInputEditText description1;

    // Loading in spinner variables.
    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    MyAdapter myAdapter;
    ArrayList<Meeting> list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Connect variables by ID.
        confirmButton = findViewById(R.id.confirmButton);
        gobackButton = findViewById(R.id.gobackButton);
        description1 = findViewById(R.id.description1);
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
        database2 = FirebaseDatabase.getInstance().getReference("OfficeSpace");
        database3 = FirebaseDatabase.getInstance().getReference("Admin");
        //  necessary components for every class //
        //  necessary components for every class //

        // Initialized Spinner (drop down menu) dropList, should be updated each array 'dropList' iteration: dropAdapter.notifyDataSetChanged();
        dropList = new ArrayList<>();
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(RequestActivity.this, android.R.layout.simple_spinner_item, dropList);
        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Loading in array list for spinner item.
        list = new ArrayList<>(); // Create the list array
        myAdapter = new MyAdapter(this, list, "bookedroom"); // Populate the array

        // Read Firebase to load in RecyclerView
        database2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Looping teh firebase to get the office value and add it to spinner.
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    // Reading the office value from snapshot.
                    String office = dataSnapshot.getValue(String.class);

                    // Adding the office to spinner menu.
                    dropList.add(office);

                    // Updating the spinner.
                    dropAdapter.notifyDataSetChanged();
                    Log.d("Added: ", "yessir" + office);

                }
                // Updating the required adapter.
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
                Toast.makeText(RequestActivity.this, "Selected Office: #" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Final setAdapter to update the above Spinner
        spinner.setAdapter(dropAdapter);

        // Send to firebase the description field and chosen office to work in.
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // Create a spinneritem selected item.
                String spinnerItem = spinner.getSelectedItem().toString();

                // Create a map to update to firebase.
                Map<String, Object> officeRequest = new HashMap<>();

                // Fill the map with office and description from this class filled by user.
                officeRequest.put("office", spinnerItem);
                officeRequest.put("description", description1.getText().toString());

                // get this users name to add to firebase map upload.
                fStore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Get users full name and add it to request firebase push.
                            String FullName = task.getResult().get("FullName").toString();
                            officeRequest.put("FullName", FullName);
                            Log.d("UserDataREF", ""+FullName);
                        }
                        // upload Map to firebase with all relevant information.
                        database3.child("officeRequests").child(currentUserID).updateChildren(officeRequest);
                    }
                });


                // Popup yes/no window after updating firebase above
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Toast.makeText(RequestActivity.this, "Successfully requested! Going back to main page", Toast.LENGTH_SHORT).show();
                                Intent confirmActivity = new Intent(RequestActivity.this, MainActivity.class);
                                startActivity(confirmActivity);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                // Allert builder for the above code pop up. asking to go to main page yes or no.
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                builder.setMessage("Request sent. \nGo to main page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });

        // Go back button to office activity page.
        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(RequestActivity.this, "Going back", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(RequestActivity.this, OfficeActivity.class);
                startActivity(gobackActivity);

            }
        });



    }

}
