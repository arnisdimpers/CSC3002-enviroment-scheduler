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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OfficeActivity extends AppCompatActivity {

    // Importing buttons, recyclerView
    Button gobackButton, confirmButton;


    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    DatabaseReference database;
    TextView textView, textView1;


    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office);


        //initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        confirmButton = findViewById(R.id.confirmButton);
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


        db.child(currentUserID + "/officeSpace/office").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String office = snapshot.getValue(String.class);

                Log.d("office: ", "uid: " + currentUserID + " office: " + office);
                if(office!=null) {
                    textView.setText("Welcome to your Office Space page." +
                            "\nHere you will find the Office space you belong to. New Employees MUST choose an office space." +
                            "\n\nCurrent Office Space: " + office +
                            "\n\nIMPORTANT: To avoid spam requests please pay attention to below instructions. " +
                            "\n\n• 1: Request should be made with permission from Employer." +
                            "\n\n• 2: Clearly type your reason for requesting the Office change." +
                            "\n\n• 3: Include relevant information - current Office Space, permission granter.");
                    textView1.setText("\nCurrent Office Space:\n" + office);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(OfficeActivity.this, "Going back to main page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(OfficeActivity.this, MainActivity.class);
                startActivity(gobackActivity);

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(OfficeActivity.this, "Going back to request page", Toast.LENGTH_SHORT).show();
                Intent confirmActivity = new Intent(OfficeActivity.this, RequestActivity.class);
//                confirmActivity.putExtra("spinnerItem", spinner.getSelectedItem().toString()); // Transferring data to another class
                startActivity(confirmActivity);

            }
        });

    }
}
