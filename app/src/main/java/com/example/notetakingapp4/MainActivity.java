package com.example.notetakingapp4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.notetakingapp4.databinding.ActivityMainBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.app.ProgressDialog;

import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton addNoteBtn;
    RecyclerView recyclerView;
    ImageButton menuBtn;
    NoteAdapter noteAdapter;

    TextView userUsernameView, userMessageView;

    ImageView profilePicView;

    ActivityMainBinding binding;
    Uri imageUri;
    SearchView searchNotes;

    Spinner categorySpinner;

    StorageReference storageReference;
    private boolean adapterloaded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        addNoteBtn = findViewById(R.id.add_note_btn);
        recyclerView = findViewById(R.id.recyclerview);
        menuBtn = findViewById(R.id.menu_btn);

        userUsernameView = findViewById(R.id.user_username_textview);
        Log.d("Gabriel", "added username text");

        //userMessageView = (TextView)findViewById(R.id.user_message);
        userMessageView = findViewById(R.id.user_message_textview);
        //Log.d("Gabriel", "added message text");

        profilePicView = findViewById(R.id.user_profile_pic);

        categorySpinner = findViewById(R.id.category_spinner);

        searchNotes = findViewById(R.id.search_notes);
        adapterloaded = false;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        addNoteBtn.setOnClickListener((v)->startActivity(new Intent(MainActivity.this,NoteDetailsActivity.class)));
        menuBtn.setOnClickListener((v)->showMenu());
        profilePicView.setOnClickListener((v)->updatePhoto());
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        searchNotes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                refreshRecyclerView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                refreshRecyclerView();
                return false;
            }
        });

        Query query = Utility.getCollectionReferenceforNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteAdapter = new NoteAdapter(options,this);
        recyclerView.setAdapter(noteAdapter);
        adapterloaded = true;

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
    void refreshRecyclerView(){ //notes view
        if(!adapterloaded)
            return;
        Query query;
        String q = searchNotes.getQuery().toString()
                .replaceAll("\\s+","")
                .toLowerCase();
        if (categorySpinner.getSelectedItem().toString()
                .equals("All") &&
                q.equals("")){
            query = Utility.getCollectionReferenceforNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        }
        else if (categorySpinner.getSelectedItem().toString()
                .equals("All")){
            query = Utility.getCollectionReferenceforNotes()
                    .whereArrayContains("keywords", q)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }
        else if (q.equals("")){
            query = Utility.getCollectionReferenceforNotes()
                    .whereEqualTo("category",categorySpinner.getSelectedItem().toString())
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }
        else {
            query = Utility.getCollectionReferenceforNotes()
                    .whereArrayContains("keywords",q)
                    .whereEqualTo("category",categorySpinner.getSelectedItem().toString())
                    .orderBy("timestamp",Query.Direction.DESCENDING);
        }

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        noteAdapter.notifyDataSetChanged();
        noteAdapter.updateOptions(options);
        noteAdapter.notifyDataSetChanged();
    }

    void setupProfile(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email = email.replaceAll("[.#$\\[\\]]", "");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("downloading Profile Info...");
        progressDialog.show();

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
        //setting up profile picture
        storageReference = FirebaseStorage.getInstance().getReference(filename);
        */
        String filename = "images/"+email+"/profile_pic";  //e.g. saved as images/gabrielyu08@gmailcom/profile_pic
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference(filename);

        imageRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d("Gabriel", "successfully fetched profile picture");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                profilePicView.setImageBitmap(bitmap);
            }
        });

        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }

    }

    void updatePhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==100 && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profilePicView.setImageURI(imageUri);
        }
        else{
            return;
        }

        //need to save photo

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email = email.replaceAll("[.#$\\[\\]]", "");
        String filename = "images/"+email+"/profile_pic";  //e.g. saved as images/gabrielyu08@gmailcom/profile_pic
        storageReference = FirebaseStorage.getInstance().getReference(filename);


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("uploading File...");
        progressDialog.show();

        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Gabriel", "uploaded image ");
                Utility.showToast(MainActivity.this,"Successfully uploaded photo to cloud");
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Log.d("Gabriel", "did not upload image ");
                Utility.showToast(MainActivity.this,"Failed to upload photo");
            }
        });

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