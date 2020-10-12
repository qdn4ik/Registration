package com.example.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOTP extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    String mAuthVerificationId;
    Button btn_verify;
    PinView et_verify;
    TextView txt_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");

        txt_verify = findViewById(R.id.txt_verify);
        et_verify = findViewById(R.id.et_verify);

        btn_verify = findViewById(R.id.btn_verify);

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp = et_verify.getText().toString();

                if(otp.isEmpty()){

                    txt_verify.setVisibility(View.VISIBLE);
                    txt_verify.setText("Please fill in the form and try again.");

                } else {

                    btn_verify.setEnabled(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyOTP.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                txt_verify.setVisibility(View.VISIBLE);
                                txt_verify.setText("There was an error verifying OTP");
                            }
                        }
                        btn_verify.setEnabled(true);
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null){
            sendUserToHome();
        }
    }


    public void sendUserToHome() {
        Intent homeIntent = new Intent(VerifyOTP.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}
