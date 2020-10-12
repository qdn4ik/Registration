package com.example.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Registration extends AppCompatActivity {

    TextInputLayout et_user_register , et_password_register , et_phone_register , et_confirm_password_register;
    CountryCodePicker ccp_register;
    Button btn_register;
    TextView txt_register , txt_error_register;
    Animation slide_down_anim , move_left_anim;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        findId();
        btn_register();
    }

    public void findId(){
        et_user_register = findViewById(R.id.et_user_register);
        et_phone_register = findViewById(R.id.et_phone_register);
        et_password_register = findViewById(R.id.et_password_register);
        et_confirm_password_register = findViewById(R.id.et_confirm_password_register);
        btn_register = findViewById(R.id.btn_register);
        txt_register = findViewById(R.id.txt_register);
        ccp_register = findViewById(R.id.ccp_register);
        txt_error_register = findViewById(R.id.txt_error_register);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        move_left_anim = AnimationUtils.loadAnimation(this,R.anim.move_left_anim);
        txt_register.setAnimation(move_left_anim);
        slide_down_anim = AnimationUtils.loadAnimation(this,R.anim.slide_down_anim);
        btn_register.setAnimation(slide_down_anim);


    }

    public void btn_register(){
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("users");

                String name = et_user_register.getEditText().getText().toString();
                String phone_number = et_phone_register.getEditText().getText().toString();
                String ccp = ccp_register.getSelectedCountryCode();
                String phone = "+" + ccp + phone_number;
                String password = et_password_register.getEditText().getText().toString();
                String confirm = et_confirm_password_register.getEditText().getText().toString();

                if (!name.isEmpty() && !phone.isEmpty() && phone.length() == 12 && password.length() == 8 && !password.isEmpty()  && password.equals(confirm)){
                    btn_register.setEnabled(false);
                    UserHelperClass helperClass = new UserHelperClass(name,phone,password);
                    reference.child(phone).setValue(helperClass);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phone,
                            60,
                            TimeUnit.SECONDS,
                            Registration.this,
                            mCallbacks
                    );
                }else {
                    txt_error_register.setText("Please fill in the form to continue.");
                    txt_error_register.setVisibility(View.VISIBLE);
                }



            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                txt_error_register.setText("Verification Failed, please try again.");
                txt_error_register.setVisibility(View.VISIBLE);
                btn_register.setEnabled(true);
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Intent otpIntent = new Intent(Registration.this, VerifyOTP.class);
                                otpIntent.putExtra("AuthCredentials", s);
                                startActivity(otpIntent);
                            }
                        },
                        10000);
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null){
            sendUserToHome();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                            // ...
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                txt_error_register.setVisibility(View.VISIBLE);
                                txt_error_register.setText("There was an error verifying OTP");
                            }
                        }
                        btn_register.setEnabled(true);
                    }
                });
    }

    private void sendUserToHome() {
        Intent homeIntent = new Intent(Registration.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }


}
