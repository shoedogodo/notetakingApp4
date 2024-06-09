package com.example.notetakingapp4;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.DrmInitData;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
//import com.google.firebase.firestore.DocumentReference;

public class NoteDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final int REQUEST_GALLERY_IMAGE = 1;
    private static final int REQUEST_PICK_AUDIO = 3;
    private static final int REQUEST_RECORD_AUDIO = 2;

    EditText titleEditText,contentEditText;
    ImageButton saveNoteBtn;
    TextView pageTitleTextView;
    String title,content,docId;
    boolean isEditMode = false;
    Button deleteNoteTextViewBtn;
    Spinner categorySpinner;
    ImageButton uploadImageButton, uploadAudioButton;
    ImageButton selectImageButton, selectAudioButton;
    ImageButton openAIButton;
    ArrayList<String> urls;
    ArrayList<Integer> urlTypes;
    RecyclerView recyclerView;
    List<MediaItem> mediaItems;
    MediaAdapter adapter;


    //@SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_btn);
        categorySpinner = findViewById(R.id.note_category_spinner);
        uploadImageButton = findViewById(R.id.upload_pic_button);
        uploadAudioButton = findViewById(R.id.upload_audio_button);
        selectAudioButton = findViewById(R.id.record_voice_button);
        selectImageButton = findViewById(R.id.take_pic_button);
        openAIButton = findViewById(R.id.openai_button);
        recyclerView = findViewById(R.id.media_recycler_view);
        mediaItems = new ArrayList<>();

        //recieve data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        categorySpinner.setSelection(getIndexByEntry(getIntent().getStringExtra("category")));
        urls = getIntent().getStringArrayListExtra("urls");
        urlTypes = getIntent().getIntegerArrayListExtra("types");
        //Log.d("Gabriel", docId);

        if (docId!=null && !docId.isEmpty()){
            isEditMode = true;
        }

        titleEditText.setText(title);
        contentEditText.setText(content);

        downloadImagesAndSetupAdapter();

    }

    private void setClickListener(){
        saveNoteBtn.setOnClickListener((v)->saveNote());
        deleteNoteTextViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(NoteDetailsActivity.this)
                        .setTitle("Delete Note")  // 设置对话框的标题
                        .setMessage("Are you sure you want to delete this note?")  // 设置对话框显示的消息
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNoteFromFirebase();  // 用户确认删除后调用删除方法
                            }
                        })
                        .setNegativeButton("No", null)  // 用户点击否定时不执行任何操作
                        .show();
            }
        });
        uploadImageButton.setOnClickListener((v) -> openGallery());
        uploadAudioButton.setOnClickListener((v) -> openAudioStorage());
        selectImageButton.setOnClickListener((v)-> openCamera());
        selectAudioButton.setOnClickListener((v)-> openVoiceRecorder());
        openAIButton.setOnClickListener((v)-> startOpenAIRecommend());
        adapter.setClickListener(new MediaAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MediaItem item = mediaItems.get(position);
                if (item.getType() == MediaItem.IMAGE) {
                    // 显示大图或放大图片
                    Intent intent = new Intent(NoteDetailsActivity.this, ImageViewActivity.class);
                    intent.putExtra("image_uri", item.getUri().toString());
                    startActivity(intent);
                } else if (item.getType() == MediaItem.AUDIO) {
                    // 播放或暂停音频
                    playAudio(item.getUri());
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                MediaItem item = mediaItems.get(position);
                // 弹出对话框确认是否删除
                new AlertDialog.Builder(NoteDetailsActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Do you really want to delete this item?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            mediaItems.remove(position);
                            adapter.notifyItemRemoved(position);
                            setupRecyclerView();
                            // 如果需要，还可以在这里处理文件的实际删除
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void startOpenAIRecommend()  {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How can I help you?");
        String[] options = {"Help me to categorize the Note", "Check my notes for grammatical mistakes", "Conclude my note"};
        Context context = this;
        String title = this.titleEditText.getText().toString();
        String content = this.contentEditText.getText().toString();
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (title.replaceAll("\\s+","").equals("") && content.replaceAll("\\s+","").equals("")){
                    Utility.showToast(context,"Content is Empty");
                    return;
                }
                switch (which) {
                    case 0:
                        String str0 = "My note's content is ' Title: " + title + "\nContent: " + content +
                            "', Here are Six Categories :{Personal, Work, Meeting, Travel, Life, Other}, Please choose the most qualified category. Notice: you should only reply one word included in the six categories and Don't reply any other words";
                        Utility.showToast(context,"Thinking...");
                        new OpenAIChatTask().execute(str0);
                        break;
                    case 1:
                        String str1 = "Can you help me to check my note for grammatical mistakes: \n Title: " +title+"\nContent: "+ content;
                        Utility.showToast(context,"Thinking...");
                        new OpenAIChatTask().execute(str1);
                        // 用户选择了选项二
                        break;
                    case 2:
                        String str2 = "Can you help me to conclude my note:\n Title: " + title+ "\nContent: "+ content;
                        Utility.showToast(context,"Thinking...");
                        new  OpenAIChatTask().execute(str2);
                        break;
                }
            }
        });

        // 创建 AlertDialog
        AlertDialog dialog = builder.create();
        // 显示对话框
        dialog.show();
    }

    private void playAudio(Uri audioUri) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(NoteDetailsActivity.this, audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setupRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MediaAdapter(this, mediaItems);
        recyclerView.setAdapter(adapter);
    }

    private void openVoiceRecorder() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_RECORD_AUDIO);
        } else {
            Toast.makeText(this, "没有找到可用的录音应用", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAudioStorage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        if (intent.resolveActivity(getPackageManager()) != null) { // 检查是否有应用可以处理这个Intent
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), REQUEST_PICK_AUDIO);
        } else {
            Toast.makeText(this, "No application can handle picking an audio file. Please install a file manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }


    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "没有找到可用的相机", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri imageUri = saveImageToStorage(imageBitmap);
                if (imageUri != null) {
                    MediaItem item = new MediaItem(imageUri, MediaItem.IMAGE, false);
                    mediaItems.add(item);
                    setupRecyclerView();
                }
            } else if (requestCode == REQUEST_GALLERY_IMAGE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    MediaItem item = new MediaItem(imageUri, MediaItem.IMAGE, false);
                    mediaItems.add(item);
                    setupRecyclerView();
                }
            } else if (requestCode == REQUEST_PICK_AUDIO){
                Uri audioUri = data.getData();
                if (audioUri != null){
                    MediaItem item = new MediaItem(audioUri, MediaItem.AUDIO, false);
                    mediaItems.add(item);
                    setupRecyclerView();
                }
            }
            else if (requestCode == REQUEST_RECORD_AUDIO){
                Uri audioUri = data.getData();
                if (audioUri != null){
                    MediaItem item = new MediaItem(audioUri, MediaItem.AUDIO, false);
                    mediaItems.add(item);
                    setupRecyclerView();
                }
            }
        }
    }

    private Uri saveImageToStorage(Bitmap bitmap) {
        // Assume block needs to handle IOException
        try {
            String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
            }

            // Return a Uri to the saved image
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void saveNote(){
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        String noteCategory = categorySpinner.getSelectedItem().toString();

        if (noteTitle==null || noteTitle.isEmpty()){
            titleEditText.setError("Title is required");
            return;
        }

        Utility.showToast(this,"Saving");
        saveNoteBtn.setEnabled(false);
        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setCategory(noteCategory);
        note.setTimestamp(Timestamp.now());
        note.renewKeywords();
        if (mediaItems.size() > 0) {
            AtomicInteger uploadsCompleted = new AtomicInteger(0);
            for (MediaItem mediaItem : mediaItems) {
                uploadMediaAndGetUrl(note, mediaItem.getUri(), mediaItem.getType(), () -> {
                    if (uploadsCompleted.incrementAndGet() == mediaItems.size()) {
                        saveNoteToFirebase(note);
                        saveNoteBtn.setEnabled(true);
                    }
                });
            }
        } else {
            saveNoteToFirebase(note);
            saveNoteBtn.setEnabled(true);
        }
    }

    void uploadMediaAndGetUrl(Note note, Uri mediaUri, int type, Runnable onAllUploaded) {
        String filename = System.currentTimeMillis() + "-" + mediaUri.getLastPathSegment();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filename);

        storageReference.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                note.addMediaUrl(downloadUri.toString(), type);
                onAllUploaded.run();
            }).addOnFailureListener(e -> {
                Log.e("Upload", "Failed to get download URL", e);
                Utility.showToast(NoteDetailsActivity.this, "Failed to get download URL");
                saveNoteToFirebase(note);
            });
        }).addOnFailureListener(e -> {
            Log.e("Upload", "Failed to upload file", e);
            Utility.showToast(NoteDetailsActivity.this,"Failed to upload");
            saveNoteToFirebase(note);
        });
    }

    void saveNoteToFirebase(Note note){
        DocumentReference documentReference;
        if (isEditMode){
            //update the node
            documentReference = Utility.getCollectionReferenceforNotes().document(docId);
        } else{
            //create new note
            documentReference = Utility.getCollectionReferenceforNotes().document();  // creates the note (function in Utility)
        }

        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //note is added
                    Utility.showToast(NoteDetailsActivity.this,"Note added successfully");
                    finish();
                }else{
                    Utility.showToast(NoteDetailsActivity.this,"Failed while adding note");
                }
                //return false;
            }
        });
    }

    void deleteNoteFromFirebase(){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceforNotes().document(docId);

        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //note is added
                    Utility.showToast(NoteDetailsActivity.this,"Note deleted successfully");
                    finish();
                }else{
                    Utility.showToast(NoteDetailsActivity.this,"Failed while deleting note");
                }
                //return false;
            }
        });
    }

    int getIndexByEntry(String entry){
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null && item.equals(entry)) {
                return i;
            }
        }
        return -1;
    }

    private void downloadImagesAndSetupAdapter() {
        if (urls != null && !urls.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(urls.size());
            for (int i = 0; i < urls.size(); i++) {
                final int type = urlTypes.get(i);  // Assuming urlTypes is properly populated
                final String url = urls.get(i);  // Create a final variable for the URL
                new Thread(() -> {
                    Uri mediaUri = downloadAndSaveImage(this, url);  // Use the final variable inside the lambda
                    runOnUiThread(() -> {
                        if (mediaUri != null) {
                            MediaItem item = new MediaItem(mediaUri, type, true);
                            mediaItems.add(item);
                        }
                        latch.countDown();
                    });
                }).start();
            }
            new Thread(() -> {
                try {
                    latch.await();  // Wait for all threads to complete
                    runOnUiThread(this::setupMediaAdapter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }). start();
        } else {
            setupMediaAdapter();
        }
    }

    private void setupMediaAdapter() {
        adapter = new MediaAdapter(this, mediaItems);
        if (isEditMode){
            pageTitleTextView.setText("Edit your note");
            deleteNoteTextViewBtn.setVisibility(View.VISIBLE);
        }

        setupRecyclerView();

        setClickListener();
    }

    public Uri downloadAndSaveImage(Context context, String imageUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // 检查请求是否成功
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            // 输入流用于读取图像数据
            input = connection.getInputStream();

            // 创建文件保存下载的图像
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "downloadedImage.jpg");
            output = new FileOutputStream(file);

            byte[] data = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            // 文件成功保存后，获取其 URI
            Uri savedImageUri = Uri.fromFile(file);
            return savedImageUri;
            // 使用 savedImageUri 根据需要进行操作

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private class OpenAIChatTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OpenAIChat openAIChat = new OpenAIChat(params[0]);
                return openAIChat.getResult();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            if (str.equals("Personal")){
                Utility.showToast(NoteDetailsActivity.this, "I think it can be Categorized in " + str);
                categorySpinner.setSelection(getIndexByEntry(str));
            } else if (str.equals("Work")){
                Utility.showToast(NoteDetailsActivity.this, "I think it can be Categorized in " + str);
                categorySpinner.setSelection(getIndexByEntry(str));
            } else if (str.equals("Meeting")){
                Utility.showToast(NoteDetailsActivity.this, "I think it can be Categorized in " + str);
                categorySpinner.setSelection(getIndexByEntry(str));
            } else if (str.equals("Travel")){
                Utility.showToast(NoteDetailsActivity.this, "I think it can be Categorized in " + str);
                categorySpinner.setSelection(getIndexByEntry(str));
            } else if (str.equals("Life")){
                Utility.showToast(NoteDetailsActivity.this, "I think it can be Categorized in " + str);
                categorySpinner.setSelection(getIndexByEntry(str));
            } else if (str.equals("Other")){
                Utility.showToast(NoteDetailsActivity.this, "I think it can be Categorized in " + str);
                categorySpinner.setSelection(getIndexByEntry(str));
            } else {
                EditText editText = new EditText(NoteDetailsActivity.this);
                editText.setText(str);
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteDetailsActivity.this);
                builder.setTitle("Response");
                builder.setView(editText);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                Log.d("OpenAIResponse", str);
            }
        }
    }

}