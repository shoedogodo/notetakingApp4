package com.example.notetakingapp4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton addNoteBtn;
    RecyclerView recyclerView;
    ImageButton menuBtn;
    NoteAdapter noteAdapter;

    TextView userUsernameView, userMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteBtn = findViewById(R.id.add_note_btn);
        recyclerView = findViewById(R.id.recyclerview);
        menuBtn = findViewById(R.id.menu_btn);

        userUsernameView = findViewById(R.id.user_username_textview);
        Log.d("Gabriel", "added username text");

        //userMessageView = (TextView)findViewById(R.id.user_message);
        userMessageView = findViewById(R.id.user_message_textview);
        //Log.d("Gabriel", "added message text");

        addNoteBtn.setOnClickListener((v)->startActivity(new Intent(MainActivity.this,NoteDetailsActivity.class)));
        menuBtn.setOnClickListener((v)->showMenu());


        setupRecyclerView();

        setupProfile();
    }

    void showMenu(){
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuBtn);
        popupMenu.getMenu().add("Logout");
        popupMenu.getMenu().add("Update Profile");
        popupMenu.getMenu().add("Change Password");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle()=="Logout"){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                    return true;
                }
                else if (menuItem.getTitle()=="Update Profile"){
                    startActivity(new Intent(MainActivity.this,UpdateProfile.class));
                    finish();
                    //return true;
                }

                else if (menuItem.getTitle()=="Change Password"){
                    //Log.d("Gabriel", "Trying to change password");
                    startActivity(new Intent(MainActivity.this,ChangePassword.class));
                    finish();
                }
                return false;
            }
        });
    }
    void setupRecyclerView(){ //notes view
        Query query = Utility.getCollectionReferenceforNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteAdapter = new NoteAdapter(options,this);
        recyclerView.setAdapter(noteAdapter);
    }

    void setupProfile(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email = email.replaceAll("[.#$\\[\\]]", "");

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Log.d("Gabriel","Updating main page");

        DocumentReference docRef = db.collection("profiles").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Gabriel", "DocumentSnapshot data: " + document.getData());
                        Map<String, Object> data = document.getData();
                        if (data != null){
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();

                                Log.d("Gabriel", key);
                                Log.d("Gabriel",value.toString());

                                if (key.equals("username")){
                                    String text = value.toString();
                                    Log.d("Gabriel", "set username ");
                                    userUsernameView.setText(text);
                                }
                                else if (key.equals("message")){
                                    String text = value.toString();
                                    Log.d("Gabriel", "set message ");
                                    userMessageView.setText(text);
                                }
                            }
                        }
                    } else {
                        Log.d("Gabriel", "No such document");
                    }
                } else {
                    Log.d("Gabriel", "get failed with ", task.getException());
                }
            }
        });

        /*
        mDatabase.child("profiles").child(email).child("username").addValueEventListener(new ValueEventListener() {
            @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Gabriel","Trying to change username");
                // Get data as a String
               String username = dataSnapshot.getValue(String.class);
               userUsername.setText(username);
           }

           @Override
           public void onCancelled(DatabaseError error) {
               // Failed to read value
               Log.w("MainActivity", "Failed to read username.", error.toException());
           }
        });

        mDatabase.child("profiles").child(email).child("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get data as a String
                String message = dataSnapshot.getValue(String.class);
                userMessage.setText(message);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("MainActivity", "Failed to read message.", error.toException());
            }
        });

         */


        //FirebaseFirestore db = FirebaseFirestore.getInstance();
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("profiles/user123/my profiles/his profile");

        //db.collection("profiles").

        //FirestoreRecyclerOptions<Profile> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }
}