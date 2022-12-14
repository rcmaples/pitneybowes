package com.example.appa;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fullstory.FS;
import com.fullstory.FSOnReadyListener;
import com.fullstory.FSSessionData;

public class MainActivity extends AppCompatActivity implements FSOnReadyListener {
    private final String target_app = "com.example.appb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        TextView intentTextView = (TextView)findViewById(R.id.IntentTextView);
        String intentText = "No email in incoming intent.";

        if (email != null) {
            intentText = String.format("Intent from %s:\n%s", target_app, email);
        }

        intentTextView.setText(intentText);

        FS.setReadyListener(this);

    }

    @Override
    public void onReady(FSSessionData sessionData) {
        String fsUrl = sessionData.getCurrentSessionURL();
        Log.i("MainActivity", "Session URL:  " + fsUrl);
    }

    public void handleTap(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder
                .setTitle("Open App B")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes",(DialogInterface.OnClickListener) (dialog, which) -> {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(target_app);
                    if (launchIntent != null) {
                        launchIntent
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("email","foo@bar.com");
                        startActivity(launchIntent);
                    } else {
                        Toast
                                .makeText(
                                        MainActivity.this,
                                        String.format("requested package does not exist (%s)", target_app),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}