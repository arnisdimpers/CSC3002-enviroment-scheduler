package com.example.environment_scheduler;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText fullName,email,password,phone;
    Button registerBtn,goToLogin;
    boolean valid = true;
    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    CheckBox isAdmin, isUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();

        fullName = findViewById(R.id.textHolder);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);

        isUser = findViewById(R.id.isStudent);
        isAdmin = findViewById(R.id.isTeacher);

        // check boxes logic, 1 box at a time
        isUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()) {
                    isAdmin.setChecked(false);
                }
            }
        });

        isAdmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()) {
                    isUser.setChecked(false);
                }
            }
        });

        registerBtn.setOnClickListener((view) -> { //Register button listener

            checkField(fullName); //checkField function returns boolean if field is empty or not
            checkField(email);
            checkField(password);
            checkField(phone);

            //checkbox validation, at least 1 must be selected student or teacher or display Toast. Code above check box logic for 1 at a time
            if (!(isAdmin.isChecked() || isUser.isChecked())) {
                Toast.makeText(RegisterActivity.this, "Select the account type", Toast.LENGTH_SHORT).show();
                return;
            }

            //start the user registration process
            if(valid) {
                fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() { //firebase create user & success listener
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = fAuth.getCurrentUser(); //create user reference for firebase
                        Toast.makeText(RegisterActivity.this, "Account Created", Toast.LENGTH_SHORT).show(); //on success creation display 'created' text in this class Register
                        DocumentReference df = fStore.collection("Users").document(user.getUid()); //creates Users instance in firebase database and adds this user. getUid available only in onSucess method
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("FullName", fullName.getText().toString()); //create FullName field in Map<String, Object> and put this users info ..
                        userInfo.put("UserEmail", email.getText().toString());
                        userInfo.put("PhoneNumber", phone.getText().toString());

                        FirebaseDatabase userBudget = FirebaseDatabase.getInstance();
                        DatabaseReference db = userBudget.getReference("Users");
//                        db.child(user.getUid()).child("Progress Bar").setValue("0");
//                        db.child(user.getUid()).child("Budget").setValue("0");


                        //specify if the user is admin
                        if(isAdmin.isChecked()) {
                            userInfo.put("isAdmin", "1");
                        }
                        if(isUser.isChecked()) {
                            userInfo.put("isUser", "1");
                        }


                        df.set(userInfo); //save using DocumentReference in fStore database instance "Users"

                        if(isUser.isChecked()) {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class)); //start new activity Main on success creation of user
                            finish(); //finish doesn't let user go back with button to this Register page
                        }
                        if(isAdmin.isChecked()) {
                            startActivity(new Intent(getApplicationContext(), AdminMainActivity.class)); //start new activity Admin on success creation of admin
                            finish(); //finish doesn't let user go back with button to this Register page
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() { //if fAuth create user failed check
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Failed to Create Account", Toast.LENGTH_SHORT).show(); //display user Toast they failed
                    }
                });


            }


        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
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
}