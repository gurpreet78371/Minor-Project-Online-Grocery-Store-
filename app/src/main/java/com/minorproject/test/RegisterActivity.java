package com.minorproject.test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.minorproject.test.model.User;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private TextInputLayout userEmail, userPassword, confirmPassword, userName;
    private TextInputEditText inputPassword;
    private Button Register;
    private TextView gotoLogin, passwordStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userEmail = findViewById(R.id.inputEmailLayout);
        userPassword = findViewById(R.id.inputPasswordLayout);
        Register = findViewById(R.id.btnLogin);
        gotoLogin = findViewById(R.id.gotoLogin);
        confirmPassword = findViewById(R.id.inputConfirmPasswordLayout);
        userName = findViewById(R.id.inputNameLayout);
        passwordStrength = findViewById(R.id.passwordStrength);
        inputPassword = findViewById(R.id.inputPassword);
        final ProgressDialog mLoadingBar = new ProgressDialog(RegisterActivity.this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Objects.requireNonNull(userEmail.getEditText()).getText().toString().trim();
                String password = Objects.requireNonNull(userPassword.getEditText()).getText().toString().trim();
                String password2 = Objects.requireNonNull(confirmPassword.getEditText()).getText().toString().trim();
                final String name = Objects.requireNonNull(userName.getEditText()).getText().toString().trim();

                if (email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email and password!", Toast.LENGTH_SHORT).show();
                } else if (name.isEmpty()) {
                    userName.setError("Please enter your name");
                    userName.requestFocus();
                } else if (email.isEmpty()) {
                    userEmail.setError("Please enter your email.");
                    userEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    userEmail.setError("Please enter valid email.");
                    userEmail.requestFocus();
                } else if (password.isEmpty()) {
                    userPassword.setError("Please enter your password.");
                    userPassword.requestFocus();
                } else if (!password2.equals(password)) {
                    confirmPassword.setError("Password doesn't match");
                    confirmPassword.requestFocus();
                } else {
                    mLoadingBar.setTitle("Creating account");
                    mLoadingBar.setMessage("Please wait...");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();
                    System.out.println(email + " " + password);
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
//                                recreate();
                                    } else {
                                        User user = new User(name, "0", "0", "customer", "0", email);
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(user).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });

        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mFirebaseAuth.getCurrentUser() != null) {
            //handle the already login user
        }
    }

    String[] checkPasswordStrength(String input) {
        int n = input.length();

        // Checking lower alphabet in string
        boolean hasLower = false, hasUpper = false;
        boolean hasDigit = false, specialChar = false;

        for (int i = 0; i < n; i++) {
            char val = input.charAt(i);
            if (Character.isLowerCase(val))
                hasLower = true;
            if (Character.isLowerCase(val))
                hasUpper = true;
            if (Character.isLowerCase(val))
                hasDigit = true;

            char[] chars = input.toCharArray();
            for (char ch : chars) {
                if (!(ch >= 'a' && ch <= 'z') && !(ch >= 'A' && ch <= 'Z') && !(ch >= '0' && ch <= '9')) {
                    specialChar = true;
                    break;
                }
            }
        }

        String[] arr = new String[2];
        if (hasLower && hasUpper && hasDigit && specialChar && (n >= 8)) {
            arr[0] = "Strong";
            arr[1] = "#008000";
            return arr;
        } else if ((hasLower || hasUpper) && specialChar && (n >= 6)) {
            arr[0] = "Moderate";
            arr[1] = "#FFA500";
            return arr;
        } else {
            arr[0] = "Weak";
            arr[1] = "#FF0000";
            return arr;
        }
    }
}