package com.emz.chatme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.emz.chatme.Fragment.ChatRoomFragment;
import com.emz.chatme.Fragment.FriendListFragment;
import com.emz.chatme.Fragment.UpdateProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private DatabaseReference mFirebaseDatabaseReference;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[]{
                    new FriendListFragment(),
                    new ChatRoomFragment(),
                    new UpdateProfileFragment(),
//                    new SettingsFragment()
            };
            private final String[] mFragmentNames = new String[]{
                    getString(R.string.header_friends),
                    getString(R.string.chat_header),
                    getString(R.string.header_update_profile),
//                    getString(R.string.header_settings)
            };

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        mViewPager = (ViewPager) findViewById(R.id.container);

        mAuth = FirebaseAuth.getInstance();

        authCheck();

        progressBar = (ProgressBar) findViewById(R.id.main_activity_progressBar);
        progressBar.setVisibility(progressBar.INVISIBLE);
    }

    public void bindView() {
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public void authCheck() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            toSignInActivity();
        } else {
            String userId = currentUser.getUid();
            checkNewUser(userId);
        }
    }

    private void checkNewUser(String userId) {
        DatabaseReference ref = mFirebaseDatabaseReference.child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bindView();
                }else {
                    toCreateProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toCreateProfileActivity() {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        startActivity(intent);
        finish();
    }


    public void toSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
///               onActionSettingClicked();
//                break;
            case R.id.action_logout:
                onActionLogoutClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onActionLogoutClicked() {
        mAuth.signOut();
        toSignInActivity();
    }
}
