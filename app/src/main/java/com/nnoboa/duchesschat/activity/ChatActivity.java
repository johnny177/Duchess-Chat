package com.nnoboa.duchesschat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nnoboa.duchesschat.R;
import com.nnoboa.duchesschat.adapters.MessagesAdapter;
import com.nnoboa.duchesschat.model.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private String MESSAGES_CHILD = "messages";

    private String PRIVATE_MESSAGES_CHILD = "private_messages";
    private String PRIVATE_MESSAGES_STORAGE_CHILD = "private_chat_photos";

    private String MYUID_CHILD;

    private String FRIEND_UID_CHILD;

    private FirebaseAuth mFirebaseAuth;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;

    private FirebaseStorage mFirebaseStorage;

    private DatabaseReference mDatabaseReference;
    private StorageReference mPhotoStorageReference;

    private ChildEventListener mChildEventListener;
    private final String TAG = "MainActivity";


    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private static final int RC_PHOTO_PICKER =2;

    private ListView mMessagesList;

    private MessagesAdapter mMessagesAdapter;

    private EditText mMessagesEditText;

    private Button mSendButton;

    private ImageButton mPhotoPickerButton;
    private ProgressBar mProgressBar;
    private String mUsername;
    String friendName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();


        final String friendUID = intent.getExtras().getString("friendUID");
        friendName = intent.getExtras().getString("friendName");

        getSupportActionBar().setTitle(friendName);

        mUsername = "Anonymous";
        final int RC_SIGN_IN =1;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(MESSAGES_CHILD).child(PRIVATE_MESSAGES_CHILD);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mFirebaseStorage.getReference().child(PRIVATE_MESSAGES_STORAGE_CHILD);
        FRIEND_UID_CHILD = friendUID;

        mProgressBar = findViewById(R.id.progressBar);
        mMessagesList = findViewById(R.id.messagesListView);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessagesEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        List<Messages> privateMessages = new ArrayList<>();
        mMessagesAdapter = new MessagesAdapter(this,R.layout.messages_view,privateMessages);
        mMessagesList.setAdapter(mMessagesAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent," Complete Action Using "), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessagesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessagesEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                mUsername = user.getDisplayName();
                Messages messages = new Messages(mMessagesEditText.getText().toString(),mUsername,user.getUid(),null, Calendar.getInstance().getTimeInMillis());
                mDatabaseReference.child(MYUID_CHILD).child(FRIEND_UID_CHILD).push().setValue(messages);

                mMessagesEditText.setText("");
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    OnSignedInInitialize(user.getDisplayName());
                    MYUID_CHILD = user.getUid();
                }else{
                    OnSingOutCleanUp();

                    //sign out user
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.MicrosoftBuilder().build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setTheme(R.style.LoginTheme)
                                    .setLogo(R.drawable.ic_logo)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }


    private void attachDatabaseReadListener(){
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d(TAG, dataSnapshot+"");
//                    Messages friendlyMessage = dataSnapshot.getValue(Messages.class);
//                    mMessagesAdapter.add(friendlyMessage);
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
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }
    private void detachDatabaseListener(){
        if(mChildEventListener != null){
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mChildEventListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseListener();
        mMessagesAdapter.clear();
    }


    private void OnSignedInInitialize(String username){
        mUsername = username;
        attachDatabaseReadListener();

    }

    private void OnSingOutCleanUp(){
        mUsername = "Anonymous";
        mMessagesAdapter.clear();

    }
}