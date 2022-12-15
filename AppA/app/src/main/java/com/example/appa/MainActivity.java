package com.example.appa;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

    @SuppressLint("NonConstantResourceId")

    private String TARGET_APP;
    private String TAG;
    private String TEXT_TO_ENCRYPT;

    private EnCryptor encryptor;
    private DeCryptor decryptor;

    private TextView decryptedTextView;
    private TextView encryptedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TARGET_APP = getResources().getString(R.string.target_app);
        TAG = getResources().getString(R.string.tag);
        TEXT_TO_ENCRYPT = getResources().getString(R.string.text_to_encrypt);
        String INTENT_TEXT = getResources().getString(R.string.no_incoming_intent);

        decryptedTextView = findViewById(R.id.DecryptedTextView);
        encryptedTextView = findViewById(R.id.EncryptedTextView);

        // Check for incoming Intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        TextView intentTextView = findViewById(R.id.IntentTextView);

        if (email != null) {
            INTENT_TEXT = String.format("Intent from %s:\n%s", TARGET_APP, email);
        }

        intentTextView.setText(INTENT_TEXT);

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

    private void decryptText() {
        try {
            decryptedTextView.setText(decryptor
                    .decryptData(TEXT_TO_ENCRYPT, encryptor.getEncryption(), encryptor.getIv()));
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException |
                KeyStoreException | NoSuchPaddingException | NoSuchProviderException |
                IOException | InvalidKeyException e) {
            Log.e(TAG, "decryptData() called with: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | IllegalArgumentException e) {
            Log.e(TAG, "decryptData() called with: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void encryptText() {
        try {
            final byte[] encryptedText = encryptor
                    .encryptText(TEXT_TO_ENCRYPT, getResources().getString(R.string.text_to_encrypt));
            encryptedTextView.setText(Base64.encodeToString(encryptedText, Base64.DEFAULT));
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | NoSuchProviderException |
                KeyStoreException | IOException | NoSuchPaddingException | InvalidKeyException e) {
            Log.e(TAG, "onClick() called with: " + e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException | SignatureException | IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
            Log.e(TAG, "onClick() called with: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // For logging FullStory Session URL
    @Override
    public void onReady(FSSessionData sessionData) {
        String fsUrl = sessionData.getCurrentSessionURL();
        Log.i(TAG, "Session URL:  " + fsUrl);
    }

    // For KeyStore
    @SuppressLint("NonConstantResourceId")
    public void cryptoClick(View view) {
        final int id = view.getId();
        final Map<String, String> emptyMap = Collections.emptyMap();

        switch (id) {
            case R.id.EncryptButton:
                FS.event("Encrypting Text", emptyMap);
                Log.i(TAG, "Encrypting text...");
                encryptText();
                break;
            case R.id.DecryptButton:
                FS.event("Decrypting Text", emptyMap);
                Log.i(TAG, "Decrypting text...");
                decryptText();
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(TARGET_APP);
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
                                        String.format("requested package does not exist (%s)", TARGET_APP),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}