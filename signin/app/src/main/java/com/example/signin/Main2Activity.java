package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;


public class Main2Activity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    Button sign_out;
    private TextToSpeech mTTS;
    private TextView textView;
    private Button mButtonSpeak;
    private ImageButton VoiceButton;
    private EditText mChoice;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //sign_out = findViewById(R.id.log_out);
        //mButtonSpeak = findViewById(R.id.button_speak);
        textView = findViewById(R.id.enter_text);
        mChoice = findViewById(R.id.choiceid);
        VoiceButton = findViewById(R.id.voiceBtn);

        VoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }

        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("164440649285-bnlj0r0scdhn0rpl7un2gbbk94cmenki.apps.googleusercontent.com")
                .requestServerAuthCode("164440649285-bnlj0r0scdhn0rpl7un2gbbk94cmenki.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(Main2Activity.this);
        if (acct != null) {

            mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

                @Override
                public void onInit(int status) {

                    if (status == TextToSpeech.SUCCESS) {
                        int result = mTTS.setLanguage(Locale.ENGLISH); //check if error
                        welcome();
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported");
                        } else {
                            //mButtonSpeak.setEnabled(true);
                        }
                    } else {
                        Log.e("TTS", "Initialization failed");
                    }
                }

            });

        }

    }

    private void welcome () {
        String wel = "You are logged in! Would you like to go to inbox, compose new message or exit the application?. Tap the screen to answer.";
        mTTS.speak(wel, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy () {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data && counter == 0) {
                    //get text from voice input
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                    mChoice.setText(result.get(0));
                    counter++;
                }
                break;
            }

        }
        validate(mChoice.getText().toString());
    }


    private void listen () {
        //Intent to show speech to text dialog
        android.content.Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to me.");

        try {
            //If no error available, show dialog
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            //if error take measures
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void validate(String Choice){
        if((Choice.equals("Inbox")) ||  (Choice.equals("inbox"))){
            Intent inbox_intent = new Intent(Main2Activity.this, InboxActivity.class);
            startActivity(inbox_intent);
        }

        else if((Choice.equals("Compose")) ||  (Choice.equals("compose"))){
            Intent compose_intent = new Intent(Main2Activity.this, ComposeActivity.class);
            startActivity(compose_intent);
        }
        else if ((Choice.equals("Exit")) ||(Choice.equals("exit"))) {
            signOut();
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Main2Activity.this,"Successfully signed out",Toast.LENGTH_SHORT).show();
                        String finish = "You have signed out.";
                        mTTS.speak(finish, TextToSpeech.QUEUE_ADD, null);
                        while(mTTS.isSpeaking()) {
                            //DO NOTHING
                        }
                        startActivity(new Intent(Main2Activity.this, MainActivity.class));
                        finish();


                    }
                });

}
}
