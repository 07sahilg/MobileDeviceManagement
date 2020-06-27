package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GetCall extends Thread {

    Cursor managedCursor = null;
    Context context;
    Activity activity;
    Map<String, Object> call = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String  docId;


    GetCall(Context context,Activity activity){
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void run() {
        super.run();
        docId = MainActivity.e;

        StringBuffer sb = new StringBuffer();

        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Calendar calendar = Calendar.getInstance();

        calendar.set(2020, Calendar.JUNE, 3);
        String fromDate = String.valueOf(calendar.getTimeInMillis());
        calendar.set(2020, Calendar.JUNE, 6);
        String toDate = String.valueOf(calendar.getTimeInMillis());
        String[] whereValue = {fromDate, toDate};

        if(Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALL_LOG},1);
            }
            else{
                managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                        android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);
            }
        }
        else{
            managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                    android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);
        }
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int id = managedCursor.getColumnIndex(CallLog.Calls._ID);


        while (managedCursor.moveToNext()) {

            String callDate = managedCursor.getString(managedCursor
                    .getColumnIndex(android.provider.CallLog.Calls.DATE));
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "dd-MMM-yyyy HH:mm");
            String dateString = formatter.format(new Date(Long
                    .parseLong(callDate)));

            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String userName = managedCursor.getString(name);
            String callDuration = managedCursor.getString(duration);
            String ID = managedCursor.getString(id);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            try {
                call.put("Phone Number",phNumber);
                call.put("Date",dateString);
                call.put("Duration",callDuration);
                call.put("ID",ID);
                if(userName == null) {
                    userName = phNumber;
                }
                call.put("Name",userName);
                Map<String,Object> details = new HashMap<>();
                details.put(ID,call);
                    db.collection("user").document(docId).collection("Call Log").document(userName).set(details, SetOptions.merge());
                  /*  db.collection("user").document(docId).collection("Call Log")
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.i("details", String.valueOf(document.getData()));
                                }
                            }
                        }
                    });*/
              //  db.collection("user").document(docId).collection("Call Log").document(ID).set;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        managedCursor.close();

    }
}
