package com.example.whatsapp.Activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.ModelClass.StatusModal;
import com.example.whatsapp.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.firestore.DocumentReference;
        import com.google.firebase.firestore.DocumentSnapshot;
        import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

        import java.util.ArrayList;
        import java.util.List;

        import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStatusActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    List<String> image;
    List<String> uid;
    List<String> delete;
    List<String> caption;
    List<String> time;
    private List<StatusModal> statusModalsList; // Add this list

    FirebaseFirestore db=FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    StatusModal modal;
    StoriesProgressView storiesProgressView;
    String userid;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference statusref,laststatus;
    int counter=0;
    ImageView s_iv,useriv;
    TextView tvname,captionTV,timetv;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    StatusModal statusModal;

    private View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);

        storiesProgressView=findViewById(R.id.stories);
        s_iv=findViewById(R.id.iv_image_show);
        useriv=findViewById(R.id.iv_user_show);
        tvname=findViewById(R.id.tv_uname_ss);
        captionTV=findViewById(R.id.story_cap_tv);
        timetv=findViewById(R.id.tv_time_ss);
        playerView = findViewById(R.id.playerView);

        laststatus=database.getReference("laststatus");
        modal=new StatusModal();

        View reverse=findViewById(R.id.viewNext);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        reverse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return false;
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        View skip=findViewById(R.id.viewPrevious);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        skip.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return true; // consume the long click event
            }
        });

        Bundle extras=getIntent().getExtras();
        if (extras!=null){
            userid=extras.getString("uid");
        }else {

        }
        statusref=database.getReference("Status").child(userid);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            getStories(userid);
        }catch (Exception e){
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getStories(String userid) {
        image=new ArrayList<>();
        uid=new ArrayList<>();
        delete=new ArrayList<>();
        caption=new ArrayList<>();
        time=new ArrayList<>();

        statusref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image.clear();
                uid.clear();
                delete.clear();
                caption.clear();
                time.clear();
                statusModalsList = new ArrayList<>(); // Initialize the list

                StatusModal statusModal = null;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    statusModal = snapshot1.getValue(StatusModal.class);

                    long timecurrent = System.currentTimeMillis();

                    image.add(statusModal.getImage());
                    uid.add(statusModal.getUid());
                    time.add(statusModal.getTime());
                    delete.add(statusModal.getDelete());
                    caption.add(statusModal.getCaption());

                    statusModalsList.add(statusModal); // Add the statusModal to the list
                }
                if (!statusModalsList.isEmpty()) {
                    statusModal = statusModalsList.get(counter); // Set the current statusModal
                    storiesProgressView.setStoriesCount(image.size());
                    storiesProgressView.setStoriesListener(ShowStatusActivity.this);

                    long videoDuration = 5000L;
                    storiesProgressView.setStoryDuration(videoDuration);
                    storiesProgressView.startStories(counter);

                    if ("image".equals(statusModal.getType())) {
                        s_iv.setVisibility(View.VISIBLE);
                        playerView.setVisibility(View.GONE);
                        Glide.with(getApplicationContext()).load(statusModal.getImage()).into(s_iv);
                    } else if ("video".equals(statusModal.getType())) {
                        s_iv.setVisibility(View.GONE);
                        playerView.setVisibility(View.VISIBLE);
                        playVideo(statusModal.getVideo());
                    }

                    captionTV.setText(statusModal.getCaption());
                    fetchuserinfo(statusModal.getUid());
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void playVideo(String videoUrl) {
        // Release any existing player
        releasePlayer();

        // Create a new ExoPlayer instance
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setUseController(false);
        playerView.setPlayer(player);

        // Prepare the MediaItem and start playing
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);

        // Add an EventListener to detect when the video is ready to play
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    // Video is ready, start StoriesProgressView
                    storiesProgressView.startStories(counter);
                }else if (state == Player.STATE_ENDED) {
                    // Video playback ended, move to the next status
                    onNext();
                }
            }
        });
        // Prepare the player and set playWhenReady to true
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private void fetchuserinfo(String s) {
        documentReference=db.collection("Users").document(s);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Handle successful document retrieval
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Document exists, extract data
                                String name = document.getString("name");
                                String url = document.getString("url");

                                if (url.equals("")) {
                                    tvname.setText(name);
                                    useriv.setImageResource(R.drawable.man);
                                } else {
                                    tvname.setText(name);
                                    Glide.with(getApplicationContext()).load(url).into(useriv);
                                }
                            } else {
                                // Document doesn't exist
                                Toast.makeText(ShowStatusActivity.this, "No profile data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle task failure
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseFirestoreException) {
                                FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) exception;
                                // Log or handle the exception details
                                Log.e(TAG, "Firestore exception: " + firestoreException.getCode());
                            }

                            // Check if the device is offline
                            if (!isNetworkAvailable()) {
                                Toast.makeText(ShowStatusActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    private boolean isNetworkAvailable() {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    }
                });
    }

    public void onNext() {
        if ((counter + 1) < image.size()) {
            // Increment the counter first
            counter++;

            // Check if the counter is within the bounds
            if (counter < image.size()) {
                // Release the player before playing the next video
                releasePlayer();

                // Load the next status
                statusModal = statusModalsList.get(counter); // Set the current statusModal

                if ("image".equals(statusModal.getType())) {
                    // If it's an image, display it in ImageView
                    s_iv.setVisibility(View.VISIBLE);
                    playerView.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(image.get(counter)).into(s_iv);
                } else if ("video".equals(statusModal.getType())) {
                    // If it's a video, display it in VideoView
                    s_iv.setVisibility(View.GONE);
                    playerView.setVisibility(View.VISIBLE);
                    playVideo(statusModal.getVideo());
                }

                captionTV.setText(caption.get(counter));

                // Load user information for the next status
                fetchuserinfo(uid.get(counter));
            }
        } else {
            // All stories are completed
            finish();
        }
    }

    @Override
    public void onPrev() {
        if ((counter - 1) >= 0) {
            // Decrement the counter first
            counter--;



            // Check if the counter is within the bounds
            if (counter < image.size()) {
                // Release the player before playing the previous video
                releasePlayer();
                statusModal = statusModalsList.get(counter); // Set the current statusModal
                // Load the previous status
                if ("image".equals(statusModal.getType())) {
                    // If it's an image, display it in ImageView
                    s_iv.setVisibility(View.VISIBLE);
                    playerView.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(image.get(counter)).into(s_iv);
                } else if ("video".equals(statusModal.getType())) {
                    // If it's a video, display it in VideoView
                    s_iv.setVisibility(View.GONE);
                    playerView.setVisibility(View.VISIBLE);
                    playVideo(statusModal.getVideo());
                }

                captionTV.setText(caption.get(counter));

                // Load user information for the previous status
                fetchuserinfo(uid.get(counter));
            }
        }
    }
    private void releasePlayer() {
        if (player != null) {
            // Stop and release the player
            player.stop();
            player.release();
            player = null;
        }
    }

    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    public void onComplete() {
        // Check if there are more stories
        if ((counter + 1) < image.size()) {
            // Increment the counter
            counter++;

            // Release the player before playing the next video
            releasePlayer();

            // Load the next status
            if ("image".equals(statusModal.getType())) {
                // If it's an image, display it in ImageView
                s_iv.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(image.get(counter)).into(s_iv);
            } else if ("video".equals(statusModal.getType())) {
                // If it's a video, display it in VideoView
                s_iv.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);
                playVideo(statusModal.getVideo());
            }

            captionTV.setText(caption.get(counter));

            // Load user information for the next status
            fetchuserinfo(uid.get(counter));
        } else {
            // All stories are completed
            finish();
        }
    }
}