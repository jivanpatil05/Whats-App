package com.example.whatsapp.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {

    static final String USER_NODE = "Users";


    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        return false;
    }


    public static DatabaseReference currentUserDetails() {
        return FirebaseDatabase.getInstance().getReference(USER_NODE).child(currentUserId());
    }

      public static DatabaseReference allUserReference() {
        return FirebaseDatabase.getInstance().getReference().child(USER_NODE);
    }


}

