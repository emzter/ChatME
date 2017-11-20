//package com.emz.chatme;
//
//import android.app.Fragment;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.text.format.DateFormat;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//
//import com.emz.chatme.Adapter.ChatFirebaseAdapter;
//import com.emz.chatme.Adapter.ClickListenerChatFirebase;
//import com.emz.chatme.Model.ChatModel;
//import com.emz.chatme.Model.FileModel;
//import com.emz.chatme.Model.Location;
//import com.emz.chatme.Model.UserModel;
//import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlacePicker;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.io.File;
//import java.util.Calendar;
//import java.util.Date;
//
//import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
//import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
//
//import static com.emz.chatme.Util.UIUpdateClass.createSnackbar;
//import static com.emz.chatme.Util.Util.FOLDER_STORAGE_IMG;
//import static com.emz.chatme.Util.Util.URL_STORAGE_REFERENCE;
//import static com.emz.chatme.Util.Util.convertString;
//import static com.emz.chatme.Util.Util.verifyConnectivity;
//
//public class ChatRoomFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ClickListenerChatFirebase {
//
//    private static final String TAG = "ChatRoomFragment";
//    public static final String MESSAGES_CHILD = "messages";
//    private static final int IMAGE_GALLERY_REQUEST = 1;
//    private static final int IMAGE_CAMERA_REQUEST = 2;
//    private static final int PLACE_PICKER_REQUEST = 3;
//
//    private Button sendButton;
//    private ProgressBar progressBar;
//    private RecyclerView messageRecyclerView;
//    private LinearLayoutManager linearLayoutManager;
//    private EmojiconEditText messageEditText;
//    private EmojIconActions emojIcon;
//    private View rootView;
//
//    private UserModel user;
//    private File filePathImageCamera;
//
//    private FirebaseAuth mFirebaseAuth;
//    private FirebaseUser mFirebaseUser;
//    private GoogleApiClient mGoogleApiClient;
//    private DatabaseReference mFirebaseDatabaseReference;
//    FirebaseStorage storage = FirebaseStorage.getInstance();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_chat_room);
//
//        if (!verifyConnectivity(this)) {
//            createSnackbar(rootView, "Please connect the internet");
//        } else {
//            bindView();
//            authCheck();
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .enableAutoManage(this, this)
//                    .addApi(Auth.GOOGLE_SIGN_IN_API)
//                    .build();
//        }
//
//        messageEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.toString().trim().length() > 0) {
//                    sendButton.setEnabled(true);
//                } else {
//                    sendButton.setEnabled(false);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.sendButton:
//                sendMessageFirebase();
//                break;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        StorageReference storageRef = storage.getReferenceFromUrl(URL_STORAGE_REFERENCE).child(FOLDER_STORAGE_IMG);
//
//        if (requestCode == IMAGE_GALLERY_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Uri selectImageUri = data.getData();
//                if (selectImageUri != null) {
//                    sendFileFirebase(storageRef, selectImageUri);
//                }
//            }
//        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                if (filePathImageCamera != null) {
//                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName() + "_camera");
//                    sendFileFirebase(imageCameraRef, filePathImageCamera);
//                }
//            }
//        } else if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlacePicker.getPlace(this, data);
//                if (place != null) {
//                    LatLng latLng = place.getLatLng();
//                    Location location = new Location(latLng.latitude + "", latLng.longitude + "");
//                    ChatModel chat = new ChatModel(user, Calendar.getInstance().getTime().getTime() + "", location);
//                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
//                }
//            }
//        }
//    }
//
//    private void sendMessageFirebase() {
//        ChatModel chat = new ChatModel(user, convertString(messageEditText), Calendar.getInstance().getTime().getTime() + "", null);
//        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
//        messageEditText.setText(null);
//    }
//
//    private void sendFileFirebase(StorageReference storageRef, final Uri file) {
//        if (storageRef != null) {
//            final String name = DateFormat.format("dd-MM-yyyy_hhmmss", new Date()).toString();
//            StorageReference imageRef = storageRef.child(name + "_gallery");
//            UploadTask uploadTask = imageRef.putFile(file);
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Log.i(TAG, "onSuccess sendFileFirebase");
//                    Uri downloadUri = taskSnapshot.getDownloadUrl();
//                    FileModel fileTarget = new FileModel("img", downloadUri.toString(), name, "");
//                    ChatModel chat = new ChatModel(user, "", Calendar.getInstance().getTime().getTime() + "", fileTarget);
//                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
//                }
//            });
//        }
//    }
//
//    private void sendFileFirebase(StorageReference storageRef, final File file) {
//        if(storageRef != null){
//            UploadTask uploadTask = storageRef.putFile(Uri.fromFile(file));
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Log.i(TAG, "onSuccess sendFileFirebase");
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                    FileModel fileTarget = new FileModel("img", downloadUrl.toString(), file.getName(), file.length() + "");
//                    ChatModel chat = new ChatModel(user, "", Calendar.getInstance().getTime().getTime() + "", fileTarget);
//                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
//                }
//            });
//        }
//    }
//
////    private void locationPlacesIntent() {
////        try {
////            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
////            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
////        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
////            e.printStackTrace();
////        }
////    }
////
////    private void photoCameraIntent() {
////        String nomeFoto = DateFormat.format("dd-MM-yyyy_hhmmss", new Date()).toString();
////        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto + "camera.jpg");
////        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePathImageCamera));
////        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
////    }
//
////    private void photoGalleryIntent() {
////        Intent intent = new Intent();
////        intent.setType("image/*");
////        intent.setAction(Intent.ACTION_GET_CONTENT);
////        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
////    }
//
//    private void toSignInActivity() {
//        startActivity(new Intent(ChatRoomFragment.this, SignInActivity.class));
//        finish();
//    }
//
//    public void authCheck() {
//        mFirebaseAuth = FirebaseAuth.getInstance();
//        mFirebaseUser = mFirebaseAuth.getCurrentUser();
//        if (mFirebaseUser == null) {
//            toSignInActivity();
//        } else {
//            user = new UserModel(mFirebaseUser.getUid());
//            getMessageFirebase();
//        }
//    }
//
//    private void getMessageFirebase() {
//        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
//        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference.child(MESSAGES_CHILD), user.getId(), this);
//        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                int friendlyMessageCount = firebaseAdapter.getItemCount();
//                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (friendlyMessageCount - 1) &&
//                                lastVisiblePosition == (positionStart - 1))) {
//                    messageRecyclerView.scrollToPosition(positionStart);
//                }
//                progressBar.setVisibility(progressBar.INVISIBLE);
//            }
//        });
//        messageRecyclerView.setLayoutManager(linearLayoutManager);
//        messageRecyclerView.setAdapter(firebaseAdapter);
//    }
//
//    private void bindView() {
//        sendButton = (Button) findViewById(R.id.sendButton);
//        sendButton.setEnabled(false);
//        sendButton.setOnClickListener(this);
//        messageEditText = (EmojiconEditText) findViewById(R.id.messageEditText);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        messageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
//        linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setStackFromEnd(true);
//        messageRecyclerView.setLayoutManager(linearLayoutManager);
//        rootView = findViewById(R.id.chat_room_root_view);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
////                onActionSettingClicked();
//                break;
//            case R.id.action_logout:
//                onActionLogoutClicked();
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void onActionLogoutClicked() {
//        mFirebaseAuth.signOut();
//        toSignInActivity();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.d(TAG, "onConnectionFailed:" + connectionResult);
//    }
//
//    @Override
//    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {
//        //nothing
//    }
//
//    @Override
//    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
//        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude, longitude, latitude, longitude);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//        startActivity(intent);
//    }
//}
