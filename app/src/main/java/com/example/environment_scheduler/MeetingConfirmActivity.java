package com.example.environment_scheduler;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MeetingConfirmActivity extends AppCompatActivity {

    // Importing interface
    Button gobackButton, bookRoomButton;
    ImageView voiceInputButton;
    String spinnerItem;

    private static final int RECOGNIZER_RESULT = 1;

    TextInputEditText description, meetingemail;
    DatePicker datePicker1;

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    DatabaseReference database, database2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_confirm);

        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");
        //initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        bookRoomButton = findViewById(R.id.bookRoomButton);
        voiceInputButton = findViewById(R.id.voiceInput);

        // Confirm Meeting additional information User input to store
        description = findViewById(R.id.description);
        meetingemail = findViewById(R.id.meetingemail);
        datePicker1 = findViewById(R.id.datePicker1);

        Log.d("spinnerItem: ", ""+spinnerItem);




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

//        public boolean checkField(EditText textField){
//            if(textField.getText().toString().isEmpty()){
//                textField.setError("Error");
//                valid = false;
//            }else {
//                valid = true;
//            }
//
//            return valid;
//        }

        // Button updates Firebase and returns user to previous Meetings page
        bookRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // Creating drop-down menu to book EMPTY only rooms
                            String room = dataSnapshot.child("room").getValue(String.class);
                            String vacancy = dataSnapshot.child("vacancy").getValue(String.class);
                            Log.d("Vacancy FOUND: ", ""+vacancy);
                            if (room.equals(spinnerItem) && vacancy.equals("empty")) {

                                // Record current date and time of Booking
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = new Date();

                                //DocumentReference bookInfo = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                database2 = FirebaseDatabase.getInstance().getReference("Users/" + currentUserID + "/bookedRooms");

                                Map<String, Object> hopperUpdates = new HashMap<>();
                                hopperUpdates.put("room", spinnerItem);
                                hopperUpdates.put("timeofbooking", formatter.format(date));


                                Log.d("message:", ""+description.getText().toString());
                                Log.d("email:", ""+meetingemail.getText().toString());

                                int day = datePicker1.getDayOfMonth();
                                int month = datePicker1.getMonth() + 1;
                                int year = datePicker1.getYear();


                                String formattedTime = year + "-" + month + "-" + day;
                                hopperUpdates.put("date", formattedTime);
                                Log.d("date:",""+formattedTime);

                                hopperUpdates.put("message", description.getText().toString());
                                hopperUpdates.put("email", meetingemail.getText().toString());


                                String newkey = database2.push().getKey();
                                //rootRef.reference().child('mychild').push().key;

                                database2.getRef().child(newkey).updateChildren(hopperUpdates);
                                dataSnapshot.child("vacancy").getRef().setValue("taken"); //set vacancy under specific room node to 'Taken'
                                Log.d("Room FOUND: ", ""+room);
                                Log.d("Room FOUND: ", ""+room);

                                Toast.makeText(MeetingConfirmActivity.this, "Successfully Booked Meeting", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                MeetingConfirmActivity.this.finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        //voice input microphone when pressed will listen for voice from user to fill in description of meeting
        voiceInputButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
                startActivityForResult(speechIntent, RECOGNIZER_RESULT);
            }
        });

        // Go back to Meetings page button
        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(MeetingConfirmActivity.this, "Going back to previous page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(MeetingConfirmActivity.this, MeetingActivity.class);
                startActivity(gobackActivity);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            description.getText().clear();
            description.setText(matches.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
