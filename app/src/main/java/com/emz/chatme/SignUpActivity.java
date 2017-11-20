package com.emz.chatme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.emz.chatme.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static com.emz.chatme.Util.UIUpdateClass.createProgressDialog;
import static com.emz.chatme.Util.UIUpdateClass.dismissProgressDialog;
import static com.emz.chatme.Util.Util.convertString;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordAgianEditText;
    private Button signUpButton;
    private boolean valid = true;
    private String emailText;
    private String passwordText;
    private String passwordAgainText;

    private static final String TAG = "EmailPassword";
    public static final String USERS_CHILD = "users";

    private FirebaseAuth mAuth;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        bindView();

        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpButtonPress();
            }
        });
    }

    private void onSignUpButtonPress() {
        convertRegisterInfo();

        if (!validate()) {
            onSignUpFailed();
            return;
        }

        signUpButton.setEnabled(false);
        createAccount(emailText, passwordText);
    }

    private void onSignUpFailed() {
        View view = findViewById(R.id.signup_root_view);
        Snackbar.make(view, R.string.signup_failed, Snackbar.LENGTH_LONG).show();
        signUpButton.setEnabled(true);
        dismissProgressDialog();
    }

    private void createAccount(String email, String password) {
        createProgressDialog(this, getString(R.string.AuthenticatingText));
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    onSignUpFailed();
                } else {
                    onSignUpCompleted();
                }
            }
        });
    }

    private void onSignUpCompleted() {
        FirebaseUser newUser = mAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        UserModel user = new UserModel(newUser.getProviders().toString(), newUser.getUid(), newUser.getEmail(), "", "");
        mFirebaseDatabaseReference.child(USERS_CHILD).child(user.getId()).setValue(user);
        Intent intent = new Intent(getBaseContext(), CreateProfileActivity.class);
        dismissProgressDialog();
        startActivity(intent);
    }

    private boolean validate() {
        checkEmail();
        checkPassword();
        return valid;
    }

    private void checkPassword() {
        if (passwordText.isEmpty() || passwordText.length() < 8 || passwordText.length() > 12) {
            passwordEditText.setError(getString(R.string.password_error));
            valid = false;
        } else if (!Objects.equals(passwordText, passwordAgainText)) {
            passwordAgianEditText.setError(getString(R.string.password_not_match));
            valid = false;
        } else {
            passwordEditText.setError(null);
        }
    }

    private void checkEmail() {
        if (emailText.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailEditText.setError(getString(R.string.email_error));
            valid = false;
        } else {
            emailEditText.setError(null);
        }
    }

    private void convertRegisterInfo() {
        emailText = convertString(emailEditText);
        passwordText = convertString(passwordEditText);
        passwordAgainText = convertString(passwordAgianEditText);
    }

    public void bindView() {
        emailEditText = (EditText) findViewById(R.id.input_regis_email);
        passwordEditText = (EditText) findViewById(R.id.input_regis_password);
        passwordAgianEditText = (EditText) findViewById(R.id.input_regis_password_again);
        signUpButton = (Button) findViewById(R.id.btn_signup);
    }
}
