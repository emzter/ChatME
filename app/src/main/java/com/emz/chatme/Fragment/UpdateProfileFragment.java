package com.emz.chatme.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.emz.chatme.Model.UserModel;
import com.emz.chatme.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.emz.chatme.Util.UIUpdateClass.createSnackbar;
import static com.emz.chatme.Util.Util.FOLDER_STORAGE_PROFILE_IMG;
import static com.emz.chatme.Util.Util.URL_STORAGE_REFERENCE;

public class UpdateProfileFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    private ImageView profilePic;
    private EditText displayNameEt;
    private Button btnUpdateProfile;
    private View mainView;

    private static final String TAG = "UpdateProfileActivity";
    public static final String USERS_CHILD = "users";

    private String photoUrl;
    private String recentPhotoUrl;
    private File selectedProfilePic;

    private FirebaseUser currentUser;
    private UserModel user;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_update_profile, container, false);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        currentUser = mFirebaseAuth.getCurrentUser();
        bindView(rootView);
        retrieveUserData();

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyImage.openChooserWithGallery(UpdateProfileFragment.this, getString(R.string.select_images), 0);
            }
        });

        return rootView;
    }

    private void retrieveUserData() {
        DatabaseReference ref = mFirebaseDatabaseReference.child(USERS_CHILD).child(currentUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserModel.class);
                Glide.with(profilePic.getContext()).load(user.getProfilePic().toString()).into(profilePic);

                displayNameEt.setText(user.getName());
                photoUrl = user.getProfilePic();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        String displayName;
        displayName = displayNameEt.getText().toString();
        if (selectedProfilePic != null) {
            StorageReference storageRef = storage.getReferenceFromUrl(URL_STORAGE_REFERENCE).child(FOLDER_STORAGE_PROFILE_IMG).child(selectedProfilePic.getName().toString());
            sendFileFirebase(storageRef, selectedProfilePic, displayName);

        }else{
            sendProfileFirebase(displayName, photoUrl);
            createSnackbar(mainView, getString(R.string.profile_updated));
        }
    }

    private void sendProfileFirebase(String dpname, String picture) {
        Map<String, Object> userUpdates;
        UserModel updatedUser = new UserModel(user.getProvider().toString(), user.getId(), user.getEmail(), dpname, picture);
        userUpdates = updatedUser.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + user.getId(), userUpdates);
        mFirebaseDatabaseReference.updateChildren(childUpdates);
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
                    createSnackbar(mainView, "Picture And Profile Updated");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
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
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    private void onPhotoReturned(File imageFile) {
        Glide.with(profilePic.getContext()).load(imageFile).into(profilePic);
        selectedProfilePic = imageFile;
    }

    private void bindView(View rootView) {
        mainView = rootView.findViewById(R.id.update_profile_root_view);
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        displayNameEt = (EditText) rootView.findViewById(R.id.input_update_display_name);
        btnUpdateProfile = (Button) rootView.findViewById(R.id.btn_update_profile);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
