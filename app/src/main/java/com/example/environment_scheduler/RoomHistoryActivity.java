package com.example.environment_scheduler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoomHistoryActivity extends AppCompatActivity {

    // Importing buttons, recyclerView
    // Create variables required for the app to work
    Button gobackButton, officeRequestsButton, officeSpaceButton;
    DatabaseReference database;
    TextView textView;
    ImageView imageDisplay;

    // Firestore authentication requirement.
    FirebaseAuth fAuth; // to register new users in firebase.
    FirebaseFirestore fStore; // access the firestore firebase.
    StorageReference storageReference;

    // Global variables required for functionality
    String FullName,PhoneNumber,UserEmail,isAdmin;

    // Main oncreate function
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_history);

        //initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        officeRequestsButton = findViewById(R.id.officeRequestsButton);
        officeSpaceButton = findViewById(R.id.officeSpaceButton);
        textView = findViewById(R.id.textView);
        imageDisplay = findViewById(R.id.imageView1);

        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");
        String spinnerItem2 = extras.getString("spinnerItem2");


        //  necessary components for every class //
        //  necessary components for every class //
        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();
        //make firedatabase connected to this instance
        FirebaseDatabase budgetText = FirebaseDatabase.getInstance();
        DatabaseReference db = budgetText.getReference("Users");
        database = FirebaseDatabase.getInstance().getReference("RoomsHistory");

        database.child(spinnerItem).child("users").child(spinnerItem2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String FullName = snapshot.child("FullName").getValue(String.class);
                String PhoneNumber = snapshot.child("PhoneNumber").getValue(String.class);
                String comment = snapshot.child("comment").getValue(String.class);
                String date = snapshot.child("date").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String message = snapshot.child("message").getValue(String.class);
                String room = snapshot.child("room").getValue(String.class);
                String timeofbooking = snapshot.child("timeofbooking").getValue(String.class);

                Log.d("SUCCESFULLY READING USER", ""+FullName);


                textView.setText("[Employee Details]\nName: " +FullName+ "\nPhone Number: "+PhoneNumber+"\nEmail: "+email+" \n\n[Meeting Details] \nMeeting Description: "+message+"\nMeeting Date: "+date+"\nTime of booking: "+timeofbooking+"\nMeeting Room: "+room+"\n\n[Vacating Room]\nIncluded Message: "+comment + "\nIncluded Photo:");

                // reading storage reference to load imageview //
                // reading storage reference to load imageview //

                try {
                    File localfile = File.createTempFile("tempfile", ".jpg");

                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());

                    //set bitmap to image1
                    imageDisplay.setImageBitmap(bitmap);

                    //try to load image if exists
                    //storagereference is based on the "storage" of the firebase and not "realtie database".
                    //the url is being by going into /images/userIDFromSpinner/roomTakenFromSpinner/image

                    storageReference = FirebaseStorage.getInstance().getReference("images/" +spinnerItem2 + "/"+spinnerItem + "/image");
                    Log.d("SPINNERS:", ""+spinnerItem2 + " " + spinnerItem);
//                        storageReference = FirebaseStorage.getInstance().getReference("images/" + "8y1aoyHUE6Vs0Oe6p8YkfJIbbtq2" + "/01" + "/image");
                    storageReference.getFile(localfile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    //get image inside the bitmap
                                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());

                                    //set bitmap to image1
                                    imageDisplay.setImageBitmap(bitmap);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(RoomHistoryActivity.this, "No image linked to booking.", Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Back button to be transfered back
        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(RoomHistoryActivity.this, "Going back.", Toast.LENGTH_SHORT).show();
                Intent loginActivity = new Intent(RoomHistoryActivity.this, HistoryNameActivity.class);
                startActivity(loginActivity);

            }
        });


    }
}
