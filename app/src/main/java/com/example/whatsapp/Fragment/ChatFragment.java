package com.example.whatsapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsapp.Adapter.UserAdapter;
import com.example.whatsapp.ModelClass.Chat;
import com.example.whatsapp.ModelClass.User;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUser;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<String> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        userList=new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Chat chat=dataSnapshot.getValue(Chat.class);

                    assert chat != null;
                    if (chat.getSender().equals(fuser.getUid())){
                        userList.add(chat.getReceiver());
                    }
                    if (chat.getReceiver().equals(fuser.getUid())){
                        userList.add(chat.getSender());
                    }
                }
                readChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void readChat() {
        mUser=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUser.clear();

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User user=dataSnapshot.getValue(User.class);
                    //Display 1 user from chats
                    for (String id:userList){
                        if(user.getId().equals(id)){
                            if (mUser.size() != 0){
                                ListIterator<User> listIteratorUser = mUser.listIterator();
                                while(listIteratorUser.hasNext()){
                                    User user1 = listIteratorUser.next();
                                    if (!user.getId().equals(user1.getId())){
                                        listIteratorUser.add(user);
                                    }
                                }
//                                for(User user1: mUser){
//                                  if(!user.getId().equals(user1.getId())){
//                                      mUser.add(user);
//                                  }
//                                }
                            }else {
                                mUser.add(user);
                            }
                        }
                    }
                }
                userAdapter= new UserAdapter(getContext(),mUser,true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}