package com.example.notetakingapp4;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    EditText currentPasswordEditText,newPasswordEditText;
    Button changePasswordBtn;
    ProgressBar progressBar;

    //private FirebaseAuth firebase;

    boolean valid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currentPasswordEditText = findViewById(R.id.currentPassword_edit_text);
        newPasswordEditText = findViewById(R.id.newPassword_edit_text);
        changePasswordBtn = findViewById(R.id.changePassword_btn);
        progressBar = findViewById(R.id.progress_bar);

        changePasswordBtn.setOnClickListener((v)->changePassword());



    }

    void changePassword(){
        String currentPassword = currentPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();

        Log.d("Gabriel", "line below is isvalidated");
        validateData(currentPassword,newPassword);

        /*
        if (!isValidated){
            return;
        }
        */
        //changePasswordInFirebase(newPassword);
    }

    void changeInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            changePasswordBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            changePasswordBtn.setVisibility(View.VISIBLE);
        }
    }

    void validateData(String currentPassword, String newPassword){
        Utility utility = new Utility();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String password = currentPassword;
        utility.isPasswordCorrect(email, password, new Utility.OnPasswordCheckListener() {
            @Override
            public void onCheckCompleted(boolean isCorrect) {
                if (isCorrect) {
                    Log.d("Gabriel", "Old Password is correct");
                    correctCurrPassword(newPassword);
                } else {
                    currentPasswordEditText.setError("Current password is incorrect");
                    valid=false;
                }
            }
        });

    }

    void correctCurrPassword(String newPassword){
        if (newPassword.length()<6){
            newPasswordEditText.setError("Password length is invalid");
        }
        else{
            resetPassword(newPassword);
        }
    }

    void resetPassword(String newPassword){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("Gabriel", newPassword);
                    //Utility.showToast(ChangePassword.this,newPassword);
                    startActivity(new Intent(ChangePassword.this,LoginActivity.class));
                    finish();
                } else {
                    Log.d("Gabriel", "change password fail");
                }
            }
        });
    }

}