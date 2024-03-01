package com.example.whatsapp.Adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.ModelClass.StatusModal;
import com.example.whatsapp.ModelClass.UserStatusModal;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusView extends RecyclerView.ViewHolder {
    ImageView status_iv;
    TextView nametv,timetv;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    LinearLayout ll_ss;


    public StatusView(@NonNull View itemView) {
        super(itemView);
        ll_ss = itemView.findViewById(R.id.ll_status_item);
        status_iv = itemView.findViewById(R.id.iv_mystatus_item);
        nametv = itemView.findViewById(R.id.namestatus_tv_item);
        timetv = itemView.findViewById(R.id.time_tvstatus_item);
    }

    public void bindStatus(UserStatusModal status) {
        // Get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser != null ? currentUser.getUid() : "";

        // Check if the status belongs to the current user
        if (!status.getUid().equals(currentUserUid)) {
            // Bind data to views
            Glide.with(itemView.getContext()).load(status.getImage()).into(status_iv);
            timetv.setText(status.getTime());
            nametv.setText(status.getUserName());

            // Set the layout to be visible
            ll_ss.setVisibility(View.VISIBLE);
        } else {
            // If the status belongs to the current user, hide the layout
            ll_ss.setVisibility(View.GONE);
        }
    }
}
