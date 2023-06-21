package com.example.environment_scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminMainActivity extends AppCompatActivity {

    // Importing buttons, recyclerView
    // Create variables required for the app to work
    Button gobackButton, officeRequestsButton, officeSpaceButton;
    DatabaseReference database;
    TextView textView, textView1;

    // Firestore authentication requirement.
    FirebaseAuth fAuth; // to register new users in firebase.
    FirebaseFirestore fStore; // access the firestore firebase.

    // Global variables required for functionality
    String FullName,PhoneNumber,UserEmail,isAdmin;

    // Main oncreate function
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        //initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        officeRequestsButton = findViewById(R.id.officeRequestsButton);
        officeSpaceButton = findViewById(R.id.officeSpaceButton);
        textView = findViewById(R.id.textView);
        textView1 = findViewById(R.id.textView1);

        //  necessary components for every class //
        //  necessary components for every class //
        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();
        //make firedatabase connected to this instance
        FirebaseDatabase budgetText = FirebaseDatabase.getInstance();
        DatabaseReference db = budgetText.getReference("Users");
        database = FirebaseDatabase.getInstance().getReference("MeetingRooms/");

        //  necessary components for every class //
        //  necessary components for every class //

        // Access the firestore firebase and navigate to an individual user to get their information.
        fStore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                // Upon a successfull read of the existing user (if they exist in the firebase)
                if (task.isSuccessful()) {

                    // Read users full information and record it.
                    FullName = task.getResult().get("FullName").toString();
                    PhoneNumber = task.getResult().get("PhoneNumber").toString();
                    UserEmail = task.getResult().get("UserEmail").toString();
                    isAdmin = task.getResult().get("isAdmin").toString();

                    // We check if the user is an admin account and set the text apropriately on main page.
                    if (isAdmin.equals("1")) {
                        textView1.setText("\nCurrently Logged in as Admin Account:\n"+FullName);
                    }

                    Log.d("UserDataREF", FullName + " " + PhoneNumber + " " + UserEmail);

                }

            }
        });

        // Back button to be transfered back to OfficeTransfer screen
        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminMainActivity.this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                Intent loginActivity = new Intent(AdminMainActivity.this, LoginActivity.class);
                startActivity(loginActivity);

            }
        });

        // Opening request page.
        officeRequestsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminMainActivity.this, "Going to request page", Toast.LENGTH_SHORT).show();
                Intent confirmActivity = new Intent(AdminMainActivity.this, OfficeTransferActivity.class);
                startActivity(confirmActivity);

            }
        });

        // Opening office page.
        officeSpaceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminMainActivity.this, "Going back to History page", Toast.LENGTH_SHORT).show();
                Intent confirmActivity = new Intent(AdminMainActivity.this, HistoryActivity.class);
                startActivity(confirmActivity);

            }
        });

    }
}
