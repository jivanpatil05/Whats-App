package com.example.whatsapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Activity.VideoPlayingActivity;
import com.example.whatsapp.ModelClass.Chat;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    private int playingPosition = -1;



    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    //share media
    MediaPlayer mediaPlayer;
    public boolean check;


    private boolean isPlaying = false;
    private int lastPlayedPosition = -1;

    public  MessageAdapter(Context mContext,List<Chat> mChat,String imageurl){
        this.mChat=mChat;
        this.mContext=mContext;
        this.imageurl=imageurl;
        mediaPlayer = MediaPlayerClass.getMediaPlayer();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Chat chat=mChat.get(position);
        holder.show_message.setText(chat.getMessage());

        if (imageurl.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }


        //share media
        if (chat.getType().equals("text")) {
            holder.show_message.setText(chat.getMessage());
            holder.show_message.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.RacordView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
        }
        else if (chat.getType().equals("image")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.RacordView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);

            holder.cardView.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mChat.get(position).getMessage()).into(holder.imageView);

        }else if (chat.getType().equals("Audio")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.RacordView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);

            holder.cardView1.setVisibility(View.VISIBLE);
            holder.playingaudio.setImageResource(R.drawable.playaudio);
            holder.playingaudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (playingPosition == position) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            holder.playingaudio.setImageResource(R.drawable.playaudio);
                        } else {
                            mediaPlayer.start();
                            holder.playingaudio.setImageResource(R.drawable.paus);
                        }
                    } else {
                        // Stop playback for the previous item
                        stopPlaybackForPreviousItem();

                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(mChat.get(position).getMessage());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playingPosition = position;
                            holder.playingaudio.setImageResource(R.drawable.paus);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    holder.playingaudio.setImageResource(R.drawable.playaudio);
                                    playingPosition = -1;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            if (playingPosition == position) {
                holder.playingaudio.setImageResource(R.drawable.paus);
            } else {
                holder.playingaudio.setImageResource(R.drawable.playaudio);
            }
        } else if (chat.getType().equals("Video")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.RacordView.setVisibility(View.GONE);


            holder.videoView.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .asBitmap()
                    .load(mChat.get(position).getMessage())
                    .into(holder.vImg);

            holder.videoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, VideoPlayingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("videouri", String.valueOf(mChat.get(position).getMessage()));
                    mContext.startActivity(intent);
                }
            });
        }else if(chat.getType().equals("Racord")){
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);

            holder.RacordView.setVisibility(View.VISIBLE);
            holder.playingRacord.setImageResource(R.drawable.playaudio);

            if (playingPosition == position) {
                // If it's the playing position, update the UI accordingly
                if (mediaPlayer.isPlaying()) {
                    holder.playingRacord.setImageResource(R.drawable.paus);

                } else {
                    holder.playingRacord.setImageResource(R.drawable.playaudio);
                }
            } else {
                // If it's not the playing position, show the play icon
                holder.playingRacord.setImageResource(R.drawable.playaudio);
            }

            holder.playingRacord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (playingPosition == position) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            holder.playingRacord.setImageResource(R.drawable.playaudio);
                            holder.recordtime.setText("0:00");
                        } else {
                            mediaPlayer.start();
                            holder.playingRacord.setImageResource(R.drawable.paus);
                        }
                    } else {
                        // Stop playback for the previous item
                        stopPlaybackForPreviousItem();

                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(mChat.get(position).getMessage());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playingPosition = position;
                            holder.playingRacord.setImageResource(R.drawable.paus);
                            updateRecordTime(holder.recordtime, mediaPlayer.getDuration());
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    holder.playingRacord.setImageResource(R.drawable.playaudio);
                                    playingPosition = -1;
                                    holder.recordtime.setVisibility(View.GONE);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                private void updateRecordTime(TextView recordTimeTextView, int totalDuration) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        int minutes = (currentPosition / 1000) / 60;
                        int seconds = (currentPosition / 1000) % 60;

                        int totalMinutes = (totalDuration / 1000) / 60;
                        int totalSeconds = (totalDuration / 1000) % 60;
                        String formattedTime = String.format("%d:%02d / %d:%02d", minutes, seconds, totalMinutes, totalSeconds);

                        // Update the UI on the main thread
                        if(playingPosition==position){
                            /* recordTimeTextView.post(() ->*/ recordTimeTextView.setText(formattedTime);
                            recordTimeTextView.setVisibility(View.VISIBLE);
                            recordTimeTextView.postDelayed(() -> updateRecordTime(recordTimeTextView, totalDuration), 1000);


                        }else
                        {
                            recordTimeTextView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
        //share media
        if (position==mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            }else {
                holder.txt_seen.setText("Delivered");
            }
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }
    private void stopPlaybackForPreviousItem() {
        if (playingPosition != -1 && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            notifyItemChanged(playingPosition);
        }
    }
    @Override
    public int getItemCount() {
        return mChat.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        //share media
        ImageView imageView, playingaudio, vImg, playingRacord;
        CardView cardView;
        CardView cardView1;
        CardView videoView;
        CardView RacordView;
        ImageView videoPlay;
        TextView recordtime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            //share media
            imageView = itemView.findViewById(R.id.imageviewsendreceive);
            cardView = itemView.findViewById(R.id.cardimage);
            cardView1 = itemView.findViewById(R.id.cardaudio);
            RacordView = itemView.findViewById(R.id.cardRacord);
            playingaudio = itemView.findViewById(R.id.playingaudio);
            playingRacord = itemView.findViewById(R.id.playingracord);
            recordtime= itemView.findViewById(R.id.racordtiming);
            videoPlay = itemView.findViewById(R.id.playbtn);
            View videoLayout = itemView.findViewById(R.id.cardvideo);
            vImg = videoLayout.findViewById(R.id.videoviewtextview);
            videoView = itemView.findViewById(R.id.cardvideo);
        }
    }
    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return  MSG_TYPE_LEFT;
        }
    }

}



