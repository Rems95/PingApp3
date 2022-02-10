package com.example.ping3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ping3.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    TextInputEditText email,password,confirmpwd,pseudo;
    Button register;
    TextView alrlogin;
    FirebaseAuth fAuth;
    ProgressBar progressbar;
    FirebaseFirestore db;


    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.reg_email);
        password = findViewById(R.id.reg_password);
        confirmpwd = findViewById(R.id.reg_confirmpwd);
        register = findViewById(R.id.reg_button);
        alrlogin = findViewById(R.id.reg_Login);
        pseudo=findViewById(R.id.reg_pseudo);
        fAuth = FirebaseAuth.getInstance();
        progressbar = findViewById(R.id.reg_progress);
        db = FirebaseFirestore.getInstance();


        alrlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

//        if(fAuth.getCurrentUser()!= null){
//            startActivity(new Intent(getApplicationContext(),MainActivity.class));
//            finish();
//        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateandsubmit();
            }
        });

    }

    public void validateandsubmit(){
        if(!validate()){
            Toast.makeText(RegisterActivity.this, "Enter required field correctly !!", Toast.LENGTH_SHORT).show();
        }
        else{
            String mEmail = email.getText().toString().trim();

            String mPassword = password.getText().toString().trim();

            String pseudonyme = pseudo.getText().toString();




            progressbar.setVisibility(View.VISIBLE);
            System.out.println(mEmail);
            System.out.println(mPassword);
            fAuth.createUserWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        FirebaseUser user = fAuth.getCurrentUser();
                        Map<String, Object> map = new HashMap<>();
                        map.put("pseudo", "" + pseudonyme);
                        db.collection("users").document(user.getUid().toString()).set(map);
                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressbar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this, "Verification Email Has been sent !!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();



                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressbar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this, "Email Not sent!!  "+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });


                    }else{
                        progressbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Error"+task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public boolean validate(){

        boolean valid =  true;

        String mEmail = email.getText().toString().trim();

        String mPassword = password.getText().toString().trim();

        String mCPassword = confirmpwd.getText().toString().trim();
        String pseudonyme = pseudo.getText().toString().trim();



        if (TextUtils.isEmpty(mEmail)) {
            email.setError("Email is required");
            valid =  false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            email.setError("Please enter a valid email address");
            valid =  false;
        }

        if (TextUtils.isEmpty(mPassword)) {
            password.setError("Password is empty");
            valid =  false;
        } else if (!PASSWORD_PATTERN.matcher(mPassword).matches()) {
            password.setError("Password too weak - must contain atleast 1 uppercase, " +
                    "1 lowercase" +
                    " 6-20 characters in length   ");
            valid =  false;
        }

        if (TextUtils.isEmpty(mCPassword)){
            confirmpwd.setError("Confirm password field is empty");
            valid =  false;
        }else if(!(mPassword).equals(mCPassword)){
            confirmpwd.setError("Passwords do not match!");
            valid =  false;
        }
        if (TextUtils.isEmpty(pseudonyme)){
            confirmpwd.setError("Confirm password field is empty");
            valid =  false;
        }


        return valid;

    }


}
