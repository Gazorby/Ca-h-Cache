package com.mimi.cachecache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FindActivity extends AppCompatActivity {
    private final int idLenght = 7;
    private String gameId;
    private SessionManager session;
    private final String fireBaseGameCollectionName = "Game";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        TextView gameIdInput = (TextView)findViewById(R.id.game_id);
        gameIdInput.setText(shortId(idLenght));
        gameId = gameIdInput.getText().toString();
        session = new SessionManager(getApplicationContext());
        session.createLoginSession(UserCategory.HIDDEN.toString());
    }

    /**
     * Generate short, unique, and human readable id
     */
    private String shortId(int length) {
        Random _random = new Random();
        char[] _base62chars =
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
                        .toCharArray();
        StringBuilder sb = new StringBuilder(length);

        for (int i=0; i<length; i++)
            sb.append(_base62chars[_random.nextInt(36)]);

        return sb.toString();
    }

    /**
     * Start Game
     * @param view
     */
    public void startFind(View view) {
        Log.i("info", gameId);
        DocumentReference doc = db.collection(fireBaseGameCollectionName).document(gameId);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                // if game data doesn't already exist in firebase, add it
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Map finder = new HashMap<String, String>();
                        finder.put("finder", session.getUserDetails().get("userId"));
                        db.collection(fireBaseGameCollectionName).document(gameId).set(finder);
                    } else {
                    }
                } else {
                }
            }
        });
    }
}


