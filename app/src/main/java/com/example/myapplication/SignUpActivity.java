package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    ImageView googleAcc;
    private GoogleSignInClient mGoogle;
    FirebaseAuth mAuth;
    private int RC_SIGN_IN = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> userI = new HashMap<>();
    static String e;
    Button button;
    EditText username,pass,em;
    String email,pwd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();


        button = findViewById(R.id.signin);
        username = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        em = findViewById(R.id.email);
        MainActivity.e = em.getText().toString();

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                email = em.getText().toString();
                pwd = pass.getText().toString();

                Log.i("email",email);
                Log.i("name",pwd);

                if(email.isEmpty()){
                    em.setError("Please enter emaill id");
                }
                else if(pwd.isEmpty()){
                    pass.setError("Please enter password");
                }
                else if(email.isEmpty() && pwd.isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Please fill the entries", Toast.LENGTH_SHORT).show();
                }
                else if(!(email.isEmpty() && pwd.isEmpty())){


                mAuth.createUserWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    userI.put("email",em.getText().toString());
                                    userI.put("name",username.getText().toString());
                                    db.collection("user").document(email).set(userI);
                                    Toast.makeText(SignUpActivity.this, "Authentication Successful Please Login",
                                            Toast.LENGTH_SHORT).show();
                                  Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    intent.putExtra("email",user.getEmail());
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();


                                }
                            }
                        });
            }
        }
});
    }
}