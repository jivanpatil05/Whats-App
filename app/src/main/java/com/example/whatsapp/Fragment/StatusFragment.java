package com.example.whatsapp.Fragment;
        import static android.app.Activity.RESULT_OK;

        import android.Manifest;
        import android.app.Activity;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.core.content.FileProvider;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import android.os.Environment;
        import android.provider.MediaStore;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.bumptech.glide.Glide;
        import com.example.whatsapp.Activity.ShowStatusActivity;
        import com.example.whatsapp.Activity.UploadImageActivity;
        import com.example.whatsapp.Activity.VideoActivity;
        import com.example.whatsapp.Adapter.StatusView;
        import com.example.whatsapp.Activity.ImageActivity;
        import com.example.whatsapp.ModelClass.UserStatusModal;
        import com.example.whatsapp.R;
        import com.firebase.ui.database.FirebaseRecyclerAdapter;
        import com.firebase.ui.database.FirebaseRecyclerOptions;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import com.gun0912.tedpermission.PermissionListener;
        import com.gun0912.tedpermission.normal.TedPermission;

        import java.io.File;
        import java.util.List;
        import javax.annotation.Nullable;

public class StatusFragment extends Fragment implements View.OnClickListener  {


    TextView taptoaddtv,mystatustv;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference statusref,chatlist;
    String uid,url,time,delete;
    ImageView iv_mystatus;
    private Uri imageuri;
    RecyclerView recyclerView;
    ImageView photo,galary;
    CardView cardStatus1;
    LinearLayout show_status;
    private String currentphotopath;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        taptoaddtv=getActivity().findViewById(R.id.taptoadd_tv);
        mystatustv=getActivity().findViewById(R.id.mystatus_tv);
        iv_mystatus=getActivity().findViewById(R.id.iv_mystatus);
        recyclerView=getActivity().findViewById(R.id.rv_f3);
        photo=getActivity().findViewById(R.id.gallery);
        galary=getActivity().findViewById(R.id.camera);
        show_status=getActivity().findViewById(R.id.show_status);


        statusref=database.getReference("laststatus");

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        uid=user.getUid();


        taptoaddtv.setOnClickListener(this);
        mystatustv.setOnClickListener(this);

        statusref.keepSynced(true);

        chatlist=database.getReference("Chats").child(uid);
        checkpermission();
        fetchstatus();

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera();
            }
        });
        galary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickimages();
            }
        });
        iv_mystatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click on iv_mystatus
                openCurrentUserStatus();
            }

        });
        mystatustv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCurrentUserStatus();
            }
        });
        taptoaddtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCurrentUserStatus();
            }
        });
    }

    private void openCurrentUserStatus() {
        // Retrieve the current user's status information
        statusref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get the status information
                    String url = snapshot.child("image").getValue(String.class);
                    String time = snapshot.child("time").getValue(String.class);
                    String delete = snapshot.child("delete").getValue(String.class);

                    // Start a new activity to show the current user's status
                    Intent intent = new Intent(getActivity(), ShowStatusActivity.class);
                    intent.putExtra("uid", uid);  // Pass the current user's UID
                    startActivity(intent);
                } else {
                    // Handle the case where no status exists for the current user
                    Toast.makeText(getActivity(), "No status available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchstatus() {
        statusref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    url=snapshot.child("image").getValue().toString();
                    time=snapshot.child("time").getValue().toString();
                    delete=snapshot.child("delete").getValue().toString();

                    Glide.with(getContext()).load(url).into(iv_mystatus);
                    taptoaddtv.setText(time);
                    iv_mystatus.setPadding(0,0,0,0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkpermission() {
        PermissionListener permissionlistener = new PermissionListener(){

            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.CAMERA)
                .check();

    }


    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.taptoadd_tv:
//                opencameraBs();
//            case R.id.mystatus_tv:
//                opencameraBs();
//        }
    }


//    private void opencameraBs() {
//        final Dialog dialog=new Dialog(getActivity());
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.camera_bs);
//
//        TextView opencamera=dialog.findViewById(R.id.open_cameratv);
//        TextView opengallery=dialog.findViewById(R.id.open_gallerytv);
//
//        opencamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//              camera();
//            }
//        });
//        opengallery.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pickimages();
//            }
//        });
//
//        dialog.show();
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
//        dialog.getWindow().setGravity(Gravity.BOTTOM);
//    }

    public void camera(){
        String filename = "photo";
        File storagedirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imagefile = File.createTempFile(filename, "jpg", storagedirectory);
            currentphotopath = imagefile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(getActivity(),
                    "com.example.WhatsApp.fileprovider", imagefile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, 1);

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickimages() {
        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/* video/*");
        startActivityForResult(intent,1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
                // The image is captured successfully, start UploadImageActivity
                Intent uploadImageIntent = new Intent(getActivity(), UploadImageActivity.class);
                uploadImageIntent.putExtra("PHOTO_PATH", currentphotopath);
                startActivity(uploadImageIntent);
            } else {
                // Handle other results if needed
            }

            if (requestCode == 1 || resultCode ==RESULT_OK || data!=null || data.getData() !=null) {
                imageuri=data.getData();

                if (imageuri.toString().contains("image")){
                    String url=imageuri.toString();
                    Intent intent=new Intent(getActivity(), ImageActivity.class);
                    intent.putExtra("u",url);
                    startActivity(intent);
                }else if (imageuri.toString().contains("video")) {
                    // Handle video
                    String videoUrl = imageuri.toString();
                    Intent intent = new Intent(getActivity(), VideoActivity.class);
                    intent.putExtra("ur", videoUrl);
                    startActivity(intent);
                }
            }

        }catch (Exception e){

        }

    }
    @Override
    public void onStart() {
        super.onStart();
        DatabaseReference lastStatusRef = database.getReference("laststatus");
        FirebaseRecyclerOptions<UserStatusModal> options = new FirebaseRecyclerOptions.Builder<UserStatusModal>()
                .setQuery(lastStatusRef, UserStatusModal.class)
                .build();

        FirebaseRecyclerAdapter<UserStatusModal, StatusView> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<UserStatusModal, StatusView>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull StatusView holder, int position, @NonNull UserStatusModal model) {
                        // String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        holder.bindStatus(model);

                        // Check if the status belongs to the current logged-in user
                        //  if (!model.getUid().equals(currentUserUid)) {
                        ImageView imageView=holder.itemView.findViewById(R.id.iv_mystatus_item);
                        TextView nametv = holder.itemView.findViewById(R.id.namestatus_tv_item);

                        // Set the user name from the model
                        String uid = model.getUid();
                        fetchAndSetUsername(uid, nametv);

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ShowStatusActivity.class);
                                intent.putExtra("uid", model.getUid());
                                startActivity(intent);
                            }
                        });


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ShowStatusActivity.class);
                                intent.putExtra("uid", model.getUid());
                                startActivity(intent);
                            }
                        });
                        // }
                    }


                    @NonNull
                    @Override
                    public StatusView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view=LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.status_item,parent,false);

                        return new StatusView(view);
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void fetchAndSetUsername(String uid, TextView nametv) {
        DatabaseReference usersRef = database.getReference("Users").child(uid).child("username");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.getValue(String.class);
                    nametv.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}