package com.example.whatsapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Adapter.MessageAdapter;
import com.example.whatsapp.ModelClass.Chat;
import com.example.whatsapp.ModelClass.User;
import com.example.whatsapp.R;
import com.example.whatsapp.Service.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SendMessageActivity extends AppCompatActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1000;
    private long recordingDurationMillis = 0;


    String otherUser = "";
    String userid;
    CircleImageView profile_image;
    TextView username;
    FirebaseUser Login_User;
    DatabaseReference reference;
    ImageView btn_send, media_access;
    EditText text_send;
    ImageView recoder;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;
    Intent intent;
    ValueEventListener seenListener;
    TextView racordtimingP;
    MediaPlayer mediaPlayer;
    LinearLayout linearLayout, R_linearLayout;
    TextView imagesending, audiosending, videosending;
    Uri imageuri;


    ImageView Recordsend, Recorddelete;

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 100;
    private boolean isRecording = false;
    private String audioFilePath = null;
    private MediaRecorder mediaRecorder;
    private AlertDialog mediaAlertDialog;
    private CountDownTimer countDownTimer;
    private long recordingStartTime;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener((view) -> {
            startActivity(new Intent(SendMessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        mediaPlayer = new MediaPlayer();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        media_access = findViewById(R.id.all_file);
        intent = getIntent();


        userid = intent.getStringExtra("userid");
        otherUser = intent.getStringExtra("otherUserToken");


        Login_User = FirebaseAuth.getInstance().getCurrentUser();
        linearLayout = findViewById(R.id.bottomLayout);
        R_linearLayout = findViewById(R.id.RecordLayout);
        Recordsend = findViewById(R.id.RecordSend);
        Recorddelete = findViewById(R.id.RecordDelet);
        racordtimingP = findViewById(R.id.racordtimingP);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    SendMessage(Login_User.getUid(), userid, msg);
                } else {
                    Toast.makeText(SendMessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");

            }
        });

        //recorder
        recoder = findViewById(R.id.recorder);
        recoder.setOnClickListener(v -> {
            if (!isRecording) {
                if (!isRecording) {
                    startRecording();
                    countDownTimer.start();
                } else {
                    stopRecording();
                }
            }
        });

        if (userid != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        username.setText(user.getUsername());

                        if (user.getImageURL().equals("default")) {
                            profile_image.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                        }
                        readMessage(Login_User.getUid(), userid, user.getImageURL());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            // Handle the case when userid is null
        }



        seenMessage(userid);


        media_access.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SendMessageActivity.this);
                View myview = getLayoutInflater().inflate(R.layout.menulayout, null, false);
                alert.setView(myview);
                imagesending = myview.findViewById(R.id.imagesending);
                audiosending = myview.findViewById(R.id.audiosending);
                videosending = myview.findViewById(R.id.videosending);

                imagesending.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 100);
                        Toast.makeText(SendMessageActivity.this, "Opening Images", Toast.LENGTH_SHORT).show();
                    }
                });
                audiosending.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent1, 1000);
                        Toast.makeText(SendMessageActivity.this, "Opening Audio", Toast.LENGTH_SHORT).show();
                    }
                });
                videosending.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent1, 10000);
                        Toast.makeText(SendMessageActivity.this, "Opening Video", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.setNegativeButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mediaAlertDialog.dismiss();
                    }
                });
                mediaAlertDialog = alert.create();
                mediaAlertDialog.show();
            }
        });
    }




    private void SendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("type", "text");

        reference.child("Chats").push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendNotification(message);
                    }
                });

    }
    void sendNotification(String message) {
        FirebaseUtil.currentUserDetails().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User currentUser = snapshot.getValue(User.class);
                    try {
                        JSONObject jsonObject = new JSONObject();

                        JSONObject notificationObj = new JSONObject();

                        notificationObj.put("title", currentUser.getUsername());
                        notificationObj.put("body", message);

                        JSONObject dataObj = new JSONObject();
                        dataObj.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        jsonObject.put("notification", notificationObj);
                        jsonObject.put("data", dataObj);
                        jsonObject.put("to", otherUser);

                        callApi(jsonObject);
                    } catch (Exception e) {
                        Toast.makeText(SendMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SendMessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAhfIa1_s:APA91bG-4R12zTq8iwswUHmPbpJOBbfxhUOPmsCpnh05VO8lSYe9vxEXf7SHjYlG9LCn-TamzZ-_r8hEkcl70-NNkithpsb3_QMcO0-TUbMS1KjS9Vah03898Ni9gT8gE2q5wLt4KD9x")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(SendMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }
        });

    }
    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getReceiver() != null && chat.getSender() != null) {
                            if (chat.getReceiver().equals(Login_User.getUid()) && chat.getSender().equals(userid)) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isseen", true);
                                dataSnapshot.getRef().updateChildren(hashMap);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readMessage(final String myid, final String userid, final String imageurl) {
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if (chat != null && chat.getReceiver() != null && chat.getSender() != null) {
                        if ((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) ||
                                (chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                            mChat.add(chat);
                        }
                        messageAdapter = new MessageAdapter(getApplicationContext(), mChat, imageurl);
                        recyclerView.setAdapter(messageAdapter);
                        messageAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void startRecording() {
        linearLayout.setVisibility(View.GONE);
        R_linearLayout.setVisibility(View.VISIBLE);

        Recordsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                linearLayout.setVisibility(View.VISIBLE);
                R_linearLayout.setVisibility(View.GONE);
            }
        });

        Recorddelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRecording();
                linearLayout.setVisibility(View.VISIBLE);
                R_linearLayout.setVisibility(View.GONE);

            }
        });

        // Check audio recording permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }


        // Prepare the file path for audio recording
        audioFilePath = getExternalFilesDir("audio_recordings").getAbsolutePath() + "/audio_recording.3gp";

        // Create a MediaRecorder instance and configure it
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //recordtime.setText(mediaPlayer.getDuration());

        try {
            // Start recording
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            recoder.setImageResource(R.drawable.mutef);
            recordingStartTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
        recordingStartTime = System.currentTimeMillis();
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedTime = System.currentTimeMillis() - recordingStartTime;
                updateTimer(elapsedTime);
            }

            @Override
            public void onFinish() {
                // Handle the timer finish if needed
            }
        };
        //countDownTimer.start();
    }
    private void cancelRecording() {
        if (mediaRecorder != null) {
            // Stop recording and release resources
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            recoder.setImageResource(R.drawable.voicef);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // Delete the recorded file
            File file = new File(audioFilePath);
            if (file.exists()) {
                file.delete();
            }

            Toast.makeText(this, "Recording canceled", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopRecording() {
        if (mediaRecorder != null) {
            // Stop recording
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            recoder.setImageResource(R.drawable.voicef);
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            recordingDurationMillis = System.currentTimeMillis() - recordingStartTime;
            uploadAudio();
        }
    }
    private void updateTimer(long elapsedTime) {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        racordtimingP.setText(timeString);

    }
    private void uploadAudio() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.audiofile);
        progressDialog.setMessage("Audio Sending To : " + username);
        progressDialog.setTitle("Sending Audio.....");
        progressDialog.show();

        // Generate a unique filename for the audio file
        String time = String.valueOf(System.currentTimeMillis());
        String audioFile = "ChatAudio/" + "Audio" + time;

        // Create a reference to the Firebase Storage location
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(audioFile);

        // Upload the audio file
        storageReference.putFile(Uri.fromFile(new File(audioFilePath)))
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded file
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnCompleteListener(uriTask1 -> {
                        if (uriTask1.isSuccessful()) {
                            String downloadAudio = uriTask1.getResult().toString();

                            // Save the audio message in the database
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", Login_User.getUid());
                            hashMap.put("receiver", userid);
                            hashMap.put("message", downloadAudio);
                            hashMap.put("type", "Racord");
                            hashMap.put("isseen", false);

                            databaseReference1.child("Chats").push().setValue(hashMap);

                            // Dismiss the progress dialog
                            progressDialog.dismiss();

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SendMessageActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(SendMessageActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    // Show upload progress
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploading " + (int) progress + "%");
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageuri = data.getData();
            try {
                setUploadImages(imageuri);


            } catch (IOException e) {
                // throw new RuntimeException(e);
                e.printStackTrace();
            }
        } else if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            if (checkReadExternalStoragePermission()) {
                imageuri = data.getData();
                if (imageuri != null) {
                    setUploadAudio(imageuri);
                } else {

                    Toast.makeText(this, "No audio file selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Request the permission
                requestReadExternalStoragePermission();
            }

        } else if (requestCode == 10000 && resultCode == RESULT_OK && data != null) {
            imageuri = data.getData();
            if (imageuri != null) {
                setUploadVideo(imageuri);
            } else {
                Toast.makeText(this, "No video file selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a valid file", Toast.LENGTH_SHORT).show();
        }
    }

    public void setUploadImages(Uri uri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Image will be Sending");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(R.drawable.sendf);
        progressDialog.setMessage("Image Sending To  :" + username);
        progressDialog.show();
        String timestamp = "" + System.currentTimeMillis();
        String FileNameAndPath = "Chatimages/" + "post" + timestamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FileNameAndPath);
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();


                        uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadurl = task.getResult().toString();
                                    reference = FirebaseDatabase.getInstance().getReference();
                                    HashMap<String, Object> hashMap1 = new HashMap<>();
                                    hashMap1.put("sender", Login_User.getUid());
                                    hashMap1.put("receiver", userid);
                                    hashMap1.put("message", downloadurl);
                                    hashMap1.put("type", "image");
                                    hashMap1.put("isseen", false);

                                    reference.child("Chats").push().setValue(hashMap1);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            mediaAlertDialog.dismiss();
                                        }
                                    }, 300);
                                } else {
                                    Exception exception = task.getException();
                                    if (exception != null) {
                                        exception.printStackTrace();
                                        Toast.makeText(SendMessageActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        mediaAlertDialog.dismiss();
                    }
                });
    }

    public void setUploadAudio(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.audiofile);
        progressDialog.setMessage("Audio Sending To : " + username);
        progressDialog.setTitle("Sending Audio.....");
        progressDialog.show();
        String time = "" + System.currentTimeMillis();
        String AudioFile = "ChatAudio/" + "Audio" + time;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(AudioFile);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String download_audio = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {

                    Toast.makeText(SendMessageActivity.this, "Sending a Audio", Toast.LENGTH_SHORT).show();
                    reference = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", Login_User.getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("message", download_audio);
                    hashMap.put("type", "Audio");
                    hashMap.put("isseen", false);

                    reference.child("Chats").push().setValue(hashMap);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            mediaAlertDialog.dismiss();
                        }
                    }, 300);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SendMessageActivity.this, "SomeThing Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setUploadVideo(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(SendMessageActivity.this);
        progressDialog.setIcon(R.drawable.video);
        progressDialog.setMessage("Video Sending To : " + username);
        progressDialog.setTitle("Sending Video.....");
        progressDialog.show();
        String time = "" + System.currentTimeMillis();
        String VedioFile = "ChatVideo/" + "Video" + time;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(VedioFile);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadvideo = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    Toast.makeText(SendMessageActivity.this, "Sending a Video", Toast.LENGTH_SHORT).show();
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", Login_User.getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("message", downloadvideo);
                    hashMap.put("type", "Video");
                    hashMap.put("isseen", false);

                    databaseReference1.child("Chats").push().setValue(hashMap);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            mediaAlertDialog.dismiss();
                        }
                    }, 300);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SendMessageActivity.this, "Something Happen Wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkReadExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try uploading audio again
                if (imageuri != null) {
                    setUploadAudio(imageuri);
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot upload audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(Login_User.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }


}


