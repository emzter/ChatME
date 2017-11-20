package com.emz.chatme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.emz.chatme.Util.UIUpdateClass.createProgressDialog;
import static com.emz.chatme.Util.UIUpdateClass.createSnackbar;
import static com.emz.chatme.Util.UIUpdateClass.dismissProgressDialog;
import static com.emz.chatme.Util.Util.convertString;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private LinearLayout loginBox;
    private ImageView appLogo;
    private Animation animTranslate;
    private Animation animFade;
    private Button loginButton;
    private TextView registerButton;
    private EditText emailText;
    private EditText passwordText;
    private SignInButton googleSignInButton;

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private String password;
    private String email;
    private boolean valid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        bindView();
        setupView();
        authCheck();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        googleSignInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gso_button:
                signIn();
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.link_signup:
                signup();
                break;
        }
    }

    public void bindView() {
        loginBox = (LinearLayout) findViewById(R.id.LoginBox);
        appLogo = (ImageView) findViewById(R.id.appLogo);
        animTranslate = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.translate);
        animFade = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.fade);
        loginButton = (Button) findViewById(R.id.btn_login);
        registerButton = (TextView) findViewById(R.id.link_signup);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        googleSignInButton = (SignInButton) findViewById(R.id.gso_button);
    }

    public void setupView() {
        loginBox.setVisibility(View.GONE);
    }

    public void authCheck() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            animStart();
        } else {
            startMainActivity();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            int statusCode = result.getStatus().getStatusCode();

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.w(TAG, "firebaseAuthWithGoogle: " + statusCode);
                onLoginFailed();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        createProgressDialog(this, getString(R.string.AuthenticatingText));

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    onLoginFailed();
                }else{
                    startMainActivity();
                }
            }
        });
    }

    public void startMainActivity() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                dismissProgressDialog();
                finish();
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 3000);
    }

    public void animStart() {
        appLogo.startAnimation(animTranslate);
        animTranslate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loginBox.setVisibility(View.VISIBLE);
                loginBox.startAnimation(animFade);
                animTranslate.setFillAfter(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        convertLoginInfo();

        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);
        createProgressDialog(this, getString(R.string.AuthenticatingText));

        authUser(email, password);
    }

    public void signup() {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
    }

    public void authUser(String email, String password) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    onLoginFailed();
                } else {
                    startMainActivity();
                }
            }
        });
    }

    public void convertLoginInfo(){
        email = convertString(emailText);
        password = convertString(passwordText);
    }

    public boolean validate() {
        valid = true;

        checkEmail();
        checkPassword();

        return valid;
    }

    private void checkPassword() {
        if (password.isEmpty() || password.length() < 8 || password.length() > 12) {
            passwordText.setError(getString(R.string.password_error));
            valid = false;
        } else {
            passwordText.setError(null);
        }
    }

    private void checkEmail() {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.email_error));
            valid = false;
        } else {
            emailText.setError(null);
        }
    }

    public void onLoginFailed() {
        View view = findViewById(R.id.signin_root_view);
        createSnackbar(view, getString(R.string.auth_failed));
        loginButton.setEnabled(true);
        dismissProgressDialog();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        View view = findViewById(R.id.signin_root_view);
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Snackbar.make(view, "Google Play Services error.", Snackbar.LENGTH_SHORT).show();
    }
}