package com.example.appa;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fullstory.FS;
import com.fullstory.FSOnReadyListener;
import com.fullstory.FSSessionData;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity implements FSOnReadyListener {
    private static final String target_app = "com.example.appb";

    // For testing KeyStore
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TEXT_TO_ENCRYPT = String.valueOf(R.string.text_to_encrypt);

    private EnCryptor encryptor;
    private DeCryptor decryptor;

    @SuppressLint("NonConstantResourceId")

    private TextView decryptedTextView;
    private TextView encryptedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decryptedTextView = (TextView)findViewById(R.id.DecryptedTextView);
        encryptedTextView = (TextView)findViewById(R.id.EncryptedTextView);

        // Check for incoming Intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        TextView intentTextView = (TextView)findViewById(R.id.IntentTextView);
        String intentText = "No email in incoming intent.";

        if (email != null) {
            intentText = String.format("Intent from %s:\n%s", target_app, email);
        }

        intentTextView.setText(intentText);

        // For logging the session url
        FS.setReadyListener(this);

        // For testing KeyStore
        encryptor = new EnCryptor();
        try {
            decryptor = new DeCryptor();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            e.printStackTrace();

        }

    }

    private void deccryptText() {
        try {
            decryptedTextView.setText(decryptor
                    .decryptData(TEXT_TO_ENCRYPT, encryptor.getEncryption(), encryptor.getIv()));
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException |
                KeyStoreException | NoSuchPaddingException | NoSuchProviderException |
                IOException | InvalidKeyException e) {
            Log.e(TAG, "decryptData() called with: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private void encryptText() {
        try {
            final byte[] encryptedText = encryptor
                    .encryptText(TEXT_TO_ENCRYPT, String.valueOf(R.string.text_to_encrypt));
            encryptedTextView.setText(Base64.encodeToString(encryptedText, Base64.DEFAULT));
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | NoSuchProviderException |
                KeyStoreException | IOException | NoSuchPaddingException | InvalidKeyException e) {
            Log.e(TAG, "onClick() called with: " + e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException | SignatureException |
                IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    // For logging FullStory Session URL
    @Override
    public void onReady(FSSessionData sessionData) {
        String fsUrl = sessionData.getCurrentSessionURL();
        Log.i("MainActivity", "Session URL:  " + fsUrl);
    }

    // For KeyStore
    @SuppressLint("NonConstantResourceId")
    public void cryptoClick(View view) {
        final int id = view.getId();
        final Map<String, String> emptyMap = Collections.emptyMap();

        switch (id) {
            case R.id.EncryptButton:
                FS.event("Encrypting Text", emptyMap);
                Log.i("MainActivity", "Encrypting text...");
                encryptText();
                break;
            case R.id.DecryptButton:
                FS.event("Decrypting Text", emptyMap);
                Log.i("MainActivity", "Decrypting text...");
                deccryptText();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
    }

    // For Intent
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