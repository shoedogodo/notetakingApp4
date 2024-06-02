package com.example.notetakingapp4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

    EditText username_edit_text, customMessage_edit_text;
    MaterialButton update_profile_btn;
    String username,message,docId;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        username_edit_text = findViewById(R.id.username_edit_text);
        customMessage_edit_text = findViewById(R.id.customMessage_edit_text);
        update_profile_btn = findViewById(R.id.update_profile_btn);

        //recieve data
        username = getIntent().getStringExtra("title");
        message = getIntent().getStringExtra("message");
        docId = getIntent().getStringExtra("docId");

        if (docId!=null && !docId.isEmpty()){
            isEditMode = true;
        }

        username_edit_text.setText(username);
        customMessage_edit_text.setText(message);

        update_profile_btn.setOnClickListener((v)->updateProfile());
    }

    void updateProfile(){
        String username = username_edit_text.getText().toString();
        String message = customMessage_edit_text.getText().toString();

        if (username.length()<2 || username.length()>20){
            username_edit_text.setError("Username length invalid");
            return;
        }
        if (message.length()<2 || message.length()>30){
            username_edit_text.setError("Message length invalid");
            return;
        }

        Profile profile = new Profile();
        profile.setUsername(username);
        profile.setMessage(message);

        saveProfileToFirebase(profile);
        saveDatatoFirebase(username,message);
    }

    void saveDatatoFirebase(String username, String message){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference profiles = db.collection("profiles");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email = email.replaceAll("[.#$\\[\\]]", "");


        Map<String,Object> data1 = new HashMap<>();
        data1.put("username", username);
        data1.put("message", message);
        profiles.document(email).set(data1);

        Utility.showToast(UpdateProfile.this,"Successfully uploaded data");

        startActivity(new Intent(UpdateProfile.this,MainActivity.class));


        Log.d("Gabriel", "uploaded data 1");
    }

    void saveProfileToFirebase(Profile profile){


        /*
        Log.d("Gabriel", "saving profile to firebase");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> user = new HashMap<>();
        user.put("username",username);
        user.put("message",message);

        Log.d("Gabriel", "Created hashmap, updating data");

        db.collection("profiles").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Utility.showToast(UpdateProfile.this,"Successful in adding data");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utility.showToast(UpdateProfile.this,"Failed in adding data");
            }
        });
        */

        /*
        DocumentReference documentReference;
        if (isEditMode){
            //update the node
            documentReference = Utility.getCollectionReferenceforProfiles().document(docId);
        }else{
            //create new note
            documentReference = Utility.getCollectionReferenceforProfiles().document();  // creates the profile (function in Utility)
        }
        documentReference.set(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //note is added
                    Utility.showToast(UpdateProfile.this,"Profile updated successfully");
                    Log.d("Gabriel", "finished updating profile in UpdateProfile.java");
                    startActivity(new Intent(UpdateProfile.this,MainActivity.class));
                    finish();
                }else{
                    Utility.showToast(UpdateProfile.this,"Failed while updating profile");
                    Log.d("Gabriel", "failed to update profile in UpdateProfile.java");
                }
                //return false;
            }
        });

         */

    }
}