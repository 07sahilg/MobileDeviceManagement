package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserNameActivity extends AppCompatActivity {

    EditText username;
    Button button;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static Map<String,Object> userI = new HashMap<>();
    String email;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        username = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String e = user.getEmail();
        if(user != null){
            email = user.getEmail();
            userI.put("email",email);
            Log.i("user", String.valueOf(username.getText()));
        }
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userI.put("name",username.getText().toString());
                db.collection("user").document(e).set(userI, SetOptions.merge());
                Intent intent = new Intent(UserNameActivity.this,LoginActivity.class);
                startActivity(intent);

            }
        });
    }
}
