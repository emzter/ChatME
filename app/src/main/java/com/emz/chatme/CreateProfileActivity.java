package com.emz.chatme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.emz.chatme.Model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.emz.chatme.Util.UIUpdateClass.createProgressDialog;
import static com.emz.chatme.Util.UIUpdateClass.createSnackbar;
import static com.emz.chatme.Util.UIUpdateClass.dismissProgressDialog;
import static com.emz.chatme.Util.Util.FOLDER_STORAGE_PROFILE_IMG;
import static com.emz.chatme.Util.Util.URL_STORAGE_REFERENCE;

public class CreateProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private EditText displayNameEt;
    private Button btnUpdateProfile;
    private View mainView;

    private String userId;
    private String name;
    private String displayname;
    private String provider;
    private String profilePicture;

    private static final String TAG = "UpdateProfileActivity";

    private File selectedProfilePic;

    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;
    private UserModel user;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private String recentPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        bindView();

        authCheck();

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyImage.openChooserWithGallery(CreateProfileActivity.this, getString(R.string.select_images), 0);
            }
        });
    }

    public void authCheck() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            toSignInActivity();
        } else {
            retriveUserProfile();
        }
    }

    private void retriveUserProfile() {
        if (currentUser.getDisplayName() != null) {
            displayname = currentUser.getDisplayName();
            displayNameEt.setText(displayname);
        }
        if (currentUser.getPhotoUrl() != null) {
            profilePicture = currentUser.getPhotoUrl().toString();
            Glide.with(profilePic.getContext()).load(profilePicture).into(profilePic);
        }
        userId = currentUser.getUid();
        name = currentUser.getEmail();
        provider = currentUser.getProviders().toString();
    }

    public void toSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateProfile() {
        createProgressDialog(CreateProfileActivity.this, getString(R.string.registering));
        String finalDisplayName = displayNameEt.getText().toString();
        if (selectedProfilePic == null) {
            sendProfileFirebase(finalDisplayName, profilePicture);
        } else {
            StorageReference storageRef = storage.getReferenceFromUrl(URL_STORAGE_REFERENCE).child(FOLDER_STORAGE_PROFILE_IMG).child(selectedProfilePic.getName().toString());
            sendFileFirebase(storageRef, selectedProfilePic, finalDisplayName);
        }
    }

    private void sendProfileFirebase(String dpname, String picture) {
        user = new UserModel(provider, userId, name, dpname, picture);
        Log.i(TAG, "UpdatedName:" + dpname);
        Log.i(TAG, "UpdatedFile:" + picture);
        mFirebaseDatabaseReference.child("users").child(user.getId()).setValue(user);
        dismissProgressDialog();
        toSignInActivity();
    }

    private void sendFileFirebase(StorageReference storageRef, final File file, final String dpname) {
        if (storageRef != null) {
            UploadTask uploadTask = storageRef.putFile(Uri.fromFile(file));
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    recentPhotoUrl = downloadUrl.toString();
                    Log.i(TAG, "file:" + recentPhotoUrl);
                    sendProfileFirebase(dpname, recentPhotoUrl);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                createSnackbar(mainView, getString(R.string.image_picker_error_text));
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                onPhotoReturned(imageFile);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(CreateProfileActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    private void onPhotoReturned(File imageFile) {
        Glide.with(profilePic.getContext()).load(imageFile).into(profilePic);
        selectedProfilePic = imageFile;
    }

    private void bindView() {
        mainView = findViewById(R.id.create_profile_root_view);
        profilePic = (ImageView) findViewById(R.id.profilePic);
        displayNameEt = (EditText) findViewById(R.id.input_update_display_name);
        btnUpdateProfile = (Button) findViewById(R.id.btn_update_profile);
    }
}
