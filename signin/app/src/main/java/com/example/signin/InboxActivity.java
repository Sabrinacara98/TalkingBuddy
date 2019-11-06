package com.example.signin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;


public class InboxActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    int counter = 0;
    public TextToSpeech mTTS;
    Button mButtonSpeak;
    ImageButton VoiceButton;
    TextView mComposedEmail;
    TextView mChoice ;
    TextView mChoice2 ;
    private Message [] messages;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Mail inbox =  new Mail();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        mChoice = findViewById(R.id.choiceid);
        //mChoice2 = findViewById(R.id.choice2id);
        VoiceButton = findViewById(R.id.micid);
        //mButtonSpeak = findViewById(R.id.speakid);

        try {
             messages = inbox.execute().get();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH); //check if error
                    speak("You are now in inbox.");

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        //mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }

                try {
                    int length = messages.length;
                    numberOfMessages(length);
                    for (int i = 0; i < length; i++) {
                        Message message = messages[i];
                        String sabrina = message.getFrom()[0].toString();
                        String yakup = message.getSubject();
                        getSubject(i + 1, sabrina, yakup);
                    }

                    if (mTTS.speak("Would you like to open any email or exit the application? If you want to open, specify the number of the email!", TextToSpeech.QUEUE_ADD, null) == 1) {
                        listen();
                    }

                }
                catch (Exception e){

                }

            }

        });

        VoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }

        });
    }

    public void speak(String string){
        mTTS.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void read() {  //reads the text
        String text = mComposedEmail.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    private void listen() {
        //Intent to show speech to text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data && counter <= 1) {
                    //get text from voice input
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                   //mChoice.setText(result.get(0));
                    counter++;
                    validate_choice(result.get(0), messages);
                }
               /* if (resultCode == RESULT_OK && null != data && counter == 1){
                    ArrayList<String> result1 = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                    //mChoice2.setText(result1.get(0));
                    //int result2 = Integer.parseInt(result1.get(0));
                    validate_number(result1.get(0), messages);
                    counter++ ;
                }*/
                break;
            }
        }



    }


    private void validate_choice(String Choice, Message [] messages) {
        String action;
        int email_num;
        String[] splitted = Choice.split("\\s+");
        if(splitted.length == 1){
            if ((Choice.equals("Exit")) || (Choice.equals("exit"))) {
                System.exit(0);
            }
            else if(Choice.equals("Yes") || Choice.equals("yes")){
                mTTS.speak("You are replying", TextToSpeech.QUEUE_ADD, null);
                Intent compose_intent = new Intent(InboxActivity.this, ComposeActivity.class);
                startActivity(compose_intent);
            }
            else if(Choice.equals("No") || Choice.equals("no")){
                System.exit(0);
            }
        }

        else if(splitted.length == 2){
            action = splitted[0];

            if ((action.equals("Open")) || (action.equals("open"))) {
                System.out.println("IN OPEN EMAIL ");
                mTTS.speak("Opening email "+ splitted[1], TextToSpeech.QUEUE_ADD, null);

                try{
                    email_num = getNumFromString(splitted[1]);
                    String content = messages[email_num-1].getContent().toString();
                    mTTS.speak(content, TextToSpeech.QUEUE_ADD, null);
                //   listen();
                }
                catch (Exception e){
                }
                if(mTTS.speak("Would you like to reply? Say Yes or No.", TextToSpeech.QUEUE_ADD, null)==1) {
                    listen();
                }
            }
        }
    }

    void numberOfMessages(int length){
        String len = "You have " + length + " messages. ";
        mTTS.speak(len, TextToSpeech.QUEUE_ADD, null);

    }
    void getSubject(int i, String sabrina, String yakup){
        String sender = " Email Number "+ i + " from " + sabrina + " with subject " + yakup;
        mTTS.speak(sender, TextToSpeech.QUEUE_ADD, null);
    }

    public class Mail extends AsyncTask<Void,Void,Message[]> {

        Message[] messages;

        @Override
        public Message[] doInBackground(Void... voids) {
            try {

                String host = "pop.gmail.com";// change accordingly
                String mailStoreType = "pop3";
                String user = "computerproject714@gmail.com";// change accordingly
                String password = "computerproject1";// change accordingly
                //create properties field
                Properties properties = new Properties();

                properties.put("mail.pop3.host", host);
                properties.put("mail.pop3.port", "995");
                properties.put("mail.pop3.starttls.enable", "true");
                Session emailSession = Session.getDefaultInstance(properties);

                //create the POP3 store object and connect with the pop server
                Store store = emailSession.getStore("pop3s");

                store.connect(host, user, password);

                //create the folder object and open it
                Folder emailFolder = store.getFolder("INBOX");
                emailFolder.open(Folder.READ_ONLY);

                // retrieve the messages from the folder in an array and print it
                messages = emailFolder.getMessages();
                System.out.println("messages.length---" + messages.length);
                for (int i = 0, n = messages.length; i < n; i++) {
                    Message message = messages[i];
                    System.out.println("---------------------------------");
                    System.out.println("Email Number " + (i + 1));
                    System.out.println("Subject: " + message.getSubject());
                    System.out.println("From: " + message.getFrom()[0]);
                    System.out.println("Text: " + message.getContent().toString());
                    }

                //close the store and folder objects
                emailFolder.close(false);
                store.close();

            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return messages;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            //do nothing
        }

    }

    int getNumFromString(String myString){
        String toReturn;
        switch(myString){
            case "one":     toReturn = "1";
                break;
            case "two":
            case "too":
            case "to" :     toReturn = "2";
                break;
            case "three":   toReturn = "3";
                break;
            case "four":
            case "for":     toReturn = "4";
                break;
            case "five":    toReturn = "5";
                break;
            case "six":     toReturn = "6";
                break;
            case "seven":   toReturn = "7";
                break;
            case "eight":   toReturn = "8";
                break;
            case "nine":    toReturn = "9";
                break;
            case "ten":     toReturn = "10";
                break;
            default:        toReturn = "0";
                break;

        }
       return Integer.parseInt(toReturn);

    }

}
