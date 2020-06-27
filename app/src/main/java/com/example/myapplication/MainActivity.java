package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String email,pwd;
    EditText emailid;
    EditText password;
    Button signin;
    Button signup;
    FirebaseAuth mAuth;
    static  Uri profile;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static Map<String,Object> userInfo = new HashMap<>();
    EditText username;
    Map<String,Object> userI = new HashMap<>();
    Map<String,Object> name = new HashMap<>();
    ImageView googleAcc;
    private GoogleSignInClient mGoogle;
    private int RC_SIGN_IN = 1;
    static String e;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

       /* if(user!=null){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }*/

        name.put("name","sahil");

        mAuth = FirebaseAuth.getInstance();
        emailid = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        username = findViewById(R.id.username);
       // db.collection("l").add(name);


        signin = (Button) findViewById(R.id.login);
        signup = (Button) findViewById(R.id.signup);
        signin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailid.getText().toString();
                pwd = password.getText().toString();

                if(email.isEmpty()){
                    emailid.setError("Please enter emaill id");
                }
                else if(pwd.isEmpty()){
                    password.setError("Please enter password");
                }
                else if(email.isEmpty() && pwd.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please fill the entries", Toast.LENGTH_SHORT).show();
                }
                else if(!(email.isEmpty() && pwd.isEmpty())){

                    mAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!(task.isSuccessful())){
                                Toast.makeText(MainActivity.this, "Entries Not Valid  \n Try Sign Up", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user != null){
                                    email = user.getEmail();
                                    userI.put("email",email);
                                    userI.put("name",username.getText().toString());
                                    Log.i("user", String.valueOf(userI));
                                }
                                db.collection("user").document(email).set(userI, SetOptions.merge());
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.putExtra("email",user.getEmail());
                                e = user.getEmail();
                                startActivity(intent);
                            }
                        }
                    });
                }

            }
        });
        signup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

        googleAcc = findViewById(R.id.googleAcc);
        googleAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogle = GoogleSignIn.getClient(MainActivity.this, gso);

                googleAcc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();

                    }
                });


            }
        });
      /*  signup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                email = emailid.getText().toString();
                pwd = password.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(MainActivity.this, "Authentication Successful.",
                                            Toast.LENGTH_SHORT).show();
                                        if(user.getDisplayName() == null) {
                                            userInfo.put("email",user.getEmail());
                                            //db.collection("user").add(userInfo);
                                            Intent intent = new Intent(MainActivity.this, UserNameActivity.class);
                                            startActivity(intent);
                                        }
                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();


                                }
                            }
                        });
            }
        });*/


    }

    private void signIn(){

        Intent intent = mGoogle.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignResult(task);
        }
    }

    private void handleSignResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            e = acc.getEmail();
            userI.put("name",acc.getDisplayName());
            userI.put("email",acc.getEmail());
            db.collection("user").document(acc.getEmail()).set(userI);
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void FirebaseGoogleAuth(final GoogleSignInAccount acct){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){


                    FirebaseUser user = mAuth.getCurrentUser();
                    firebaseUpdate(user);
                }
                else{
                    Toast.makeText(MainActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    void firebaseUpdate(FirebaseUser fuser){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null){
            Toast.makeText(MainActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();

            userI.put("name",account.getDisplayName());
            userI.put("email",account.getEmail());
            db.collection("user").document(account.getEmail()).set(userI);
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            intent.putExtra("email",account.getEmail());
            startActivity(intent);
        }

    }

}

