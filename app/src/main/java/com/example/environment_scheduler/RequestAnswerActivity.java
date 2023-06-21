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
import android.widget.TextView;
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

public class RequestAnswerActivity extends AppCompatActivity {

    // Create variables required for the app to work
    Button gobackButton, confirmButton, declineButton;
    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    DatabaseReference database, database2, database3, database4;
    TextInputEditText description1;
    TextView textView;

    // Main oncreate function
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_answer);

        // Get the spinnerItem from previous page with code below.
        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");

        // Setup the variables by ID from the connected xml page.
        confirmButton = findViewById(R.id.confirmButton);
        declineButton = findViewById(R.id.declineButton);
        gobackButton = findViewById(R.id.gobackButton);
        description1 = findViewById(R.id.description1);
        textView = findViewById(R.id.textView);


        //  necessary components for every class //
        //  necessary components for every class //
        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //make firedatabase connected to this instance
        database = FirebaseDatabase.getInstance().getReference("MeetingRooms");
        database2 = FirebaseDatabase.getInstance().getReference("OfficeSpace");
        database3 = FirebaseDatabase.getInstance().getReference("Admin");
        database4 = FirebaseDatabase.getInstance().getReference("Users");
        //  necessary components for every class //
        //  necessary components for every class //



        // Firebase listener for Admins - OfficeRequests, we compare users name with Spinner item from previous page.
        // Updates the textView field for the description user gave us.
        // Description is the specified reaason user gave when making the request.
        database3.child("officeRequests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // For loop navigating the Firebase to find the apropriate user via below 'if statement
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    // Using the meeting class with built Getters to access the firebase information.
                    Meeting meeting = dataSnapshot.getValue(Meeting.class);

                    // Getting the Name of the user involved.
                    String FullName = meeting.getFullName();

                    // Checking if the user corresponds to previous Spinner selected Employee.
                    if (FullName.equals(spinnerItem)) {
                        String office = meeting.getOffice();
                        Log.d("FullName IF equals: ", "" + FullName + " " + spinnerItem);
                        String description = meeting.getDescription();

                        // Update the text field to include the existing Office and Description (reason) provided by user.
                        textView.setText("Request Message:\n"+description+"\n\nOffice: "+office);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        // Accept button - you accept Employees request to be transferred to office.
        // Update users fierbase to hold the asked for Office designation.
        // Below code also deletes the request user made in the Admins' Firebase.
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // Reading into Admin database and into officeRequests node to read individual requests from users.
                database3.child("officeRequests").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // For loop navigating the Firebase to find the apropriate user via below 'if statement'
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            // Using the meeting class with built Getters to access the firebase information.
                            Meeting meeting = dataSnapshot.getValue(Meeting.class);

                            // Getting the Name of the user involved.
                            String FullName = meeting.getFullName();

                            // Checking if the user corresponds to previous Spinner selected Employee.
                            if (FullName.equals(spinnerItem)) {
                                String office = meeting.getOffice();
                                // Create a key (user ID in firebase, who created the request)
                                String key = dataSnapshot.getKey();
                                Log.d("USER ID SELECTED", "" + key);

                                // Update personal Users database with new Office
                                database4.child(key).child("officeSpace").child("office").setValue(office);

                                // Remove request from Admins database using the Key (users Firebase ID)
                                database3.child("officeRequests").child(key).removeValue();
                                Log.d("Updated OFFICE", "" + FullName + " " + office);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // Popup yes/no window after updating firebase above
                // Listener for click Yes or No for Alert below. If yes go to main page, if not do nothing.
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                // go to main page
                                Toast.makeText(RequestAnswerActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                                Intent confirmActivity = new Intent(RequestAnswerActivity.this, AdminMainActivity.class);
                                startActivity(confirmActivity);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // do nothing
                                break;
                        }
                    }
                };

                // alert builder for the above alert - it asks go to main page Yes or No
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestAnswerActivity.this);
                builder.setMessage("Request Accepted. \nGo to main page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });


        // Decline button - you do not accept Employees request to be transferred to office.
        // Below code also deletes the request user made in the Admins' Firebase.
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reading into Admin database and into officeRequests node to read individual requests from users.
                database3.child("officeRequests").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // For loop navigating the Firebase to find the apropriate user via below 'if statement'
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            // Using the meeting class with built Getters to access the firebase information.
                            Meeting meeting = dataSnapshot.getValue(Meeting.class);

                            // Getting the Name of the user involved.
                            String FullName = meeting.getFullName();

                            // Checking if the user corresponds to previous Spinner selected Employee.
                            if (FullName.equals(spinnerItem)) {
                                // Create a key (user ID in firebase, who created the request)
                                String key = dataSnapshot.getKey();
                                Log.d("USER ID SELECTED", "" + key);

                                // Remove request from Admins database using the Key (users Firebase ID)
                                database3.child("officeRequests").child(key).removeValue();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // Popup yes/no window after updating firebase above
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Toast.makeText(RequestAnswerActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                                Intent confirmActivity = new Intent(RequestAnswerActivity.this, AdminMainActivity.class);
                                startActivity(confirmActivity);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                // alert builder for the above alert - it asks go to main page Yes or No
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestAnswerActivity.this);
                builder.setMessage("Request Declined. \nGo to main page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        // Back button to be transfered back to OfficeTransfer screen
        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(RequestAnswerActivity.this, "Going back", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(RequestAnswerActivity.this, OfficeTransferActivity.class);
                startActivity(gobackActivity);

            }
        });



    }

}
