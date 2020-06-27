package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    File imageFile;
    Map<String, Object> call = new HashMap<>();
    Map<String, Object> msg = new HashMap<>();
    LocationManager locationManager;
    LocationListener locationListener;
    TextView email;
    TextView name;
    String nameS, emailS, docId;
    ImageView imageView;
    Cursor managedCursor = null;
    ExecutorService service = Executors.newFixedThreadPool(10);
    GetLocation getLocation = new GetLocation();
    Button signOut;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogle;
    Toolbar toolbar;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onPause() {
        super.onPause();
        //service.shutdown();
        Log.i("st", String.valueOf(service.isShutdown()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("st", String.valueOf(service.isShutdown()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        docId = MainActivity.e;

        Log.i("emial",docId);

        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Mobile  Device  Management" + "</font>"));




        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        getLocation.start();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogle = GoogleSignIn.getClient(LoginActivity.this,gso);

        //signOut = findViewById(R.id.button3);
        //Retrieving user data
        db.collection("user").document(docId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            nameS = documentSnapshot.getString("name");
                            emailS = documentSnapshot.getString("email");
                            name.setText("Employee Name : " + nameS);
                            email.setText("Employee email : " + emailS);
                        } else {
                            Toast.makeText(LoginActivity.this, "Found Nothing ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Cannot Fetch Details", Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT > 23) {


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            else if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED){
                service.execute( new Sms(this));
                service.execute(new GetCall(this,LoginActivity.this));
                getLocation.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkLocation();
                    }
                });
                //getLocation.start();
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else{
            service.execute( new Sms(this));
            service.execute(new GetCall(this,LoginActivity.this));

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                service.execute( new Sms(this));
                service.execute(new GetCall(this,LoginActivity.this));
                getLocation.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkLocation();
                    }
                });

            }
            /*else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] != PackageManager.PERMISSION_GRANTED){
                service.execute(new GetCall(this,LoginActivity.this));

            }
            else if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] != PackageManager.PERMISSION_GRANTED){
                service.execute( new Sms(this));

            }
            else if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){

                getLocation.start();

            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                service.execute( new Sms(this));
                service.execute(new GetCall(this,LoginActivity.this));
            }
            else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                service.execute(new GetCall(this,LoginActivity.this));
                getLocation.start();
            }
            else if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                getLocation.start();
                service.execute( new Sms(this));

            }*/
            else{
                Toast.makeText(this, "No Permission Granted", Toast.LENGTH_SHORT).show();

            }
        }
        else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }
            }
        }
    }


    public void checkLocation() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onProviderDisabled(String provider) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };


        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    updateLocationInfo(location);
                }
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }


    public void updateLocationInfo(final Location location) {

        Geocoder geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            String add = "Could not find Address";
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList != null) {
                Log.i("placeInfo", addressList.get(0).toString());
                add = "";
                if (addressList.get(0).getSubThoroughfare() != null)
                    add += addressList.get(0).getSubThoroughfare() + " ";
                if (addressList.get(0).getThoroughfare() != null)
                    add += addressList.get(0).getThoroughfare() + " ";
                if (addressList.get(0).getLocality() != null)
                    add += addressList.get(0).getLocality() + " ";
                if (addressList.get(0).getPostalCode() != null)
                    add += addressList.get(0).getPostalCode() + " ";
                if (addressList.get(0).getCountryName() != null)
                    add += addressList.get(0).getCountryName() + " ";
            }
            Map<String, Object> locate = new HashMap<>();
            locate.put("latitude", location.getLatitude());
            locate.put("longitude", location.getLongitude());
            locate.put("address", add);
            db.collection("user").document(docId).collection("Location").add(locate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.logout:
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Are you sure")
                        .setMessage("Do you want to delete")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mGoogle.signOut();
                                mAuth.signOut();
                                service.shutdown();
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("NO", null)
                        .show();

                return true;
            default:
                return false;
        }
    }


}