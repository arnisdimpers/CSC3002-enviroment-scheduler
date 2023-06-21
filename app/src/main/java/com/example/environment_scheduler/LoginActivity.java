package com.example.environment_scheduler;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    EditText email,password;
    Button loginBtn,gotoRegister;
    boolean valid = true;
    FirebaseAuth fAuth; //authenticate user in firebase
    FirebaseFirestore fStore; //to check if user is admin or not in database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        gotoRegister = findViewById(R.id.gotoRegister);


        loginBtn.setOnClickListener(new View.OnClickListener() { //Login button listener
            @Override
            public void onClick(View v) {
                checkField(email); //checkField function returns boolean if login fields are empty or not
                checkField(password);
                Log.d("TAG", "Login, 48, onClick: " + email.getText().toString());

                if(valid) { //based on email and password provided - login the user
                    fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() { //try authenticate users email+password
                        @Override
                        public void onSuccess(AuthResult authResult) { //if user successfully logged in
                            Toast.makeText(LoginActivity.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                            checkUserAccessLevel(authResult.getUser().getUid());
                        }
                    }).addOnFailureListener(new OnFailureListener() { //if user failed to log in
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(LoginActivity.this, "Login was not successful: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });


        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });




    }

    private void checkUserAccessLevel(String uid) { //function to check on Login if user is admin or average user level
        DocumentReference df = fStore.collection("Users").document(uid); //navigate df to collection database of Users document under users uid
        //extract the data from the document (from the database Users, with uid to see their isAdmin, isUser level
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) { //documentSnapshot contains the data of .document(uid) identified with users uid
                Log.d("TAG", "Login, 90, onSuccess: " + documentSnapshot.getData());
                // identify the user access level

                if(documentSnapshot.getString("isAdmin") != null) { //2nd user for future (ADMIN)
                    //user is admin
                    startActivity(new Intent(getApplicationContext(), AdminMainActivity.class));
                    finish();
                }
                if(documentSnapshot.getString("isUser") != null) {
                    //user is normal user
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }

    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }

    @Override
    protected void onStart() { //override start to check if user is already logged in
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) { //checks firebase authenticated users if they are logged in
            DocumentReference df = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) { //check users status admin or user to login into correct page automatically on startup

                    fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
                    fStore = FirebaseFirestore.getInstance();
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String currentUserID = currentFirebaseUser.getUid();
                    // Access the firestore firebase and navigate to an individual user to get their information.
                    fStore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            // Upon a successfull read of the existing user (if they exist in the firebase)
                            if (task.isSuccessful()) {

                                // Read user level information and record it.
                                try {
                                    String isAdmin = task.getResult().get("isAdmin").toString();
                                    startActivity(new Intent(getApplicationContext(), AdminMainActivity.class));
                                    finish();
                                } catch (NullPointerException e) {

                                }

                                try {
                                    String isUser = task.getResult().get("isUser").toString();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                } catch (NullPointerException e) {

                                }

                            }

                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            });

        }
    }


}

