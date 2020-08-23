package com.nnoboa.duchesschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.nnoboa.duchesschat.activity.ChatActivity;
import com.nnoboa.duchesschat.activity.PrivateChatActivity;
import com.nnoboa.duchesschat.adapters.UsersAdapter;
import com.nnoboa.duchesschat.model.Users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String ANONYMOUS = "Anonymous";
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    ListView userList;
    UsersAdapter usersAdapter;
    ChildEventListener childEventListener;
    FirebaseStorage firebaseStorage;
    DatabaseReference databaseReference;
    String USERS_CHILD_KEY = "users";
    int RC_SIGN_IN =100;
    String mUsername;
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                OnSignedInInitialize(user.getDisplayName());
            }else{
                OnSignOutCleanUp();
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.MicrosoftBuilder().build()
                );

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setTheme(R.style.LoginTheme)
                                .setLogo(R.drawable.ic_logo)

                        .build(), RC_SIGN_IN
                );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDatabase = firebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        try{
        databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS_CHILD_KEY);
        }catch (AndroidRuntimeException e){
            firebaseAuth.addAuthStateListener(authStateListener);
        }
        userList = findViewById(R.id.user_list);
        final List<Users> users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, R.layout.users_view,users);
        userList.setAdapter(usersAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent chatIntent = new Intent(MainActivity.this, PrivateChatActivity.class);
                String friendUID = users.get(position).getmUID();
                String friendName = users.get(position).getmUserDisplayName();
                chatIntent.putExtra("friendUID",friendUID);
                chatIntent.putExtra("friendName",friendName);
                startActivity(chatIntent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                assert firebaseUser != null;
                final Users users = new Users(firebaseUser.getDisplayName(),firebaseUser.getPhotoUrl(),firebaseUser.getUid());
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                DatabaseReference user = root.child(USERS_CHILD_KEY);
                user.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(firebaseUser.getUid()).exists()){
                            Log.d("MainActivity","User Exists");
                        }else {
                            databaseReference.child(firebaseUser.getUid()).push().setValue(users);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Toast.makeText(MainActivity.this, "Logged In As "+firebaseAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
            }else if ( resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this, firebaseAuth.getCurrentUser().getDisplayName()+" Signed Out", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        }catch (AndroidRuntimeException e){
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(childEventListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        detachDatabaseListener();
        usersAdapter.clear();
    }

    private void detachDatabaseListener() {
        if(childEventListener != null){
            databaseReference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    private void OnSignedInInitialize(String username){
        mUsername = username;
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if(childEventListener == null){
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Users users = null;
                    if (dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()) != dataSnapshot.getChildren().iterator().next()) {
                        users = dataSnapshot.getChildren().iterator().next().getValue(Users.class);
                    }
                    usersAdapter.add(users);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        databaseReference.addChildEventListener(childEventListener);
    }

    private void OnSignOutCleanUp(){
        mUsername = ANONYMOUS;
        usersAdapter.clear();
    }

}