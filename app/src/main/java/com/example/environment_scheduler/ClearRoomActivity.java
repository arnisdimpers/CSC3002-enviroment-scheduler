package com.example.environment_scheduler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClearRoomActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1234;
    private static final int CAPTURE_CODE = 1001;
    String spinnerItem, key;
    Button gobackButton, confirmButton, capturePhoto;
    ImageView imageView;
    Uri image_uri;
    TextView textView;
    TextInputEditText description1, description2;

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    DatabaseReference database, database2, database3;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clear_room);

        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");
        Log.d("spinnerItem: ", "" + spinnerItem);

        gobackButton = findViewById(R.id.gobackButton);
        confirmButton = findViewById(R.id.confirmButton);
        capturePhoto = findViewById(R.id.capture_picture_Id);
        imageView = findViewById(R.id.ImageViewId);
        textView = findViewById(R.id.textView);
        description1 = findViewById(R.id.description1);
        description2 = findViewById(R.id.description2);

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
        database2 = FirebaseDatabase.getInstance().getReference("Users/" + currentUserID + "/vacatedRooms");
        database3 = FirebaseDatabase.getInstance().getReference("RoomsHistory/");

        //  necessary components for every class //
        //  necessary components for every class //

        // record global name variable for last person using vacant room
        final String[] lastUsedBy = {""};

        Log.d("spinnerITEMITEMITEMITEMITEM", spinnerItem);
        db.child(currentUserID).child("vacatedRooms/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String room = dataSnapshot.child("room").getValue(String.class);

                    Log.d("spinnerITEM: ", room + " " + spinnerItem);
                    if (room.equals(spinnerItem)) {
//                        key = dataSnapshot.getKey();
                        String date = dataSnapshot.child("date").getValue(String.class);
                        String message = dataSnapshot.child("message").getValue(String.class);
                        String email = dataSnapshot.child("UserEmail").getValue(String.class);
                        String PhoneNumber = dataSnapshot.child("PhoneNumber").getValue(String.class);
                        String FullName = dataSnapshot.child("FullName").getValue(String.class);
                        String timeofbooking = dataSnapshot.child("timeofbooking").getValue(String.class);

                        //update textView.setText in for DataSnapshot etc etc
                        textView.setText("When Vacating room please leave it clean. " +
                                "\n(Upload picture of room for employer)" +
                                "\nWhen finished - Press Confirm." +
                                "\n\n---- Employee Details ----" +
                                "\n\nSelected Room: " + room +
                                "\nName: " + FullName +
                                "\nPhone Number: " + PhoneNumber +
                                "\nEmail: " + email +
                                "\nTime of Booking: " + timeofbooking +
                                "\n\nPlease provide additional information.");

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        capturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });


        gobackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ClearRoomActivity.this, "Going back to Vacated Rooms page", Toast.LENGTH_SHORT).show();
                Intent gobackActivity = new Intent(ClearRoomActivity.this, VacatedActivity.class);
                startActivity(gobackActivity);

            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // key for RoomHistory/User/roomnumber/"key"<--
                String key = database3.push().getKey();
                Log.d("reading KEY: ", "" + key);

                //hashmap to store the Comment to firebase
                Map<String, Object> meetingInfo = new HashMap<>();
                meetingInfo.put("comment", description1.getText().toString());

                // record the VacatedRoom details into RoomsHistory on firebase
                db.child(currentUserID).child("vacatedRooms/").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String room = dataSnapshot.child("room").getValue(String.class);

                            Log.d("spinnerITEM: ", room + " " + spinnerItem);
                            if (room.equals(spinnerItem)) {

                                String date = dataSnapshot.child("date").getValue(String.class);
                                String message = dataSnapshot.child("message").getValue(String.class);
                                String email = dataSnapshot.child("UserEmail").getValue(String.class);
                                String PhoneNumber = dataSnapshot.child("PhoneNumber").getValue(String.class);
                                String FullName = dataSnapshot.child("FullName").getValue(String.class);
                                lastUsedBy[0] = FullName;
                                String timeofbooking = dataSnapshot.child("timeofbooking").getValue(String.class);

                                meetingInfo.put("room", room);
                                database3.child(spinnerItem+"/roomnum").setValue(room);

                                meetingInfo.put("date", date);
                                meetingInfo.put("message", message);
                                meetingInfo.put("email", email);
                                meetingInfo.put("PhoneNumber", PhoneNumber);
                                meetingInfo.put("FullName", FullName);
                                meetingInfo.put("timeofbooking", timeofbooking);

                                meetingInfo.put("userID", currentUserID);


                                //get reference to store image
                                storageReference = FirebaseStorage.getInstance().getReference("images/" + currentUserID + "/" + room + "/" + "image");

                                storageReference.putFile(image_uri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                //when image has successfully uploaded to firebaseStorage, get the image reference URL
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        meetingInfo.put("imageurl", uri.toString()); //store image url


                                                        // save to firebase the above meetingInfo map, with vacant room details
                                                        database3.child(spinnerItem+"/users/"+currentUserID).updateChildren(meetingInfo);

                                                    }
                                                });

                                                //its been uploaded, reset the image URI
                                                imageView.setImageURI(null);
                                                Toast.makeText(ClearRoomActivity.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ClearRoomActivity.this, "Failed to Upload", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                // delete vacant room from user
                db.child(currentUserID).child("vacatedRooms/"+spinnerItem).removeValue();


                // reading into "room" with spinnerItem, and read the parent node ('String keys' below)
                // update actual room to "empty" and available
                Map<String, Object> emptyRoom = new HashMap<>();
                database.orderByChild("room").equalTo(spinnerItem).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot datas: dataSnapshot.getChildren()){
                            String keys=datas.getKey();
                            emptyRoom.put("room", spinnerItem);
                            emptyRoom.put("vacancy", "empty");
                            emptyRoom.put("lastUsedBy", lastUsedBy[0]);
                            Log.d("KEY in MEETINGS: ", keys);
                            //finally push Map updated room information and "empty" field to Firebase
                            database.child(keys).updateChildren(emptyRoom);
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
                                Toast.makeText(ClearRoomActivity.this, "Successfully updated! Going back to main page", Toast.LENGTH_SHORT).show();
                                Intent confirmActivity = new Intent(ClearRoomActivity.this, MainActivity.class);
                                startActivity(confirmActivity);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ClearRoomActivity.this);
                builder.setMessage("Updated successfully. \nGo to main page?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();




            }
        });

    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "new image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camintent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camintent, CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length>0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(image_uri);
        }
    }
}