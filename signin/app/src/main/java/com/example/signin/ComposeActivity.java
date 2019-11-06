package com.example.signin;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ComposeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    int counter = 0;
    TextToSpeech mTTS;
    TextView mSubject;
    Button mButtonSpeak;
    ImageButton VoiceButton;
    TextView mRecipient;
    TextView mComposedEmail;
    EditText mChoice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        //mButtonSpeak = findViewById(R.id.speakid);
        mSubject = findViewById(R.id.subjectid);
        mChoice = findViewById(R.id.choiceid);
        VoiceButton = findViewById(R.id.micid);
        mRecipient = findViewById(R.id.recipientid);
        mComposedEmail = findViewById(R.id.emailid);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH); //check if error
                    welcome();
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                     //   mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
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

    private void read() {  //reads the text
        String text = mComposedEmail.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void welcome() {
        String wel = "You are now in compose message. Enter recipient and subject and start composing your email. If you want to go back say exit.";
        mTTS.speak(wel, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data && counter == 0) {
                    //get text from voice input
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                    mRecipient.setText(result.get(0));
                    counter++;
                    listen();
                } else if (resultCode == RESULT_OK && null != data && counter == 1) {
                    //get text from voice input
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                    mSubject.setText(result.get(0));
                    counter++;
                    listen();
                } else if (resultCode == RESULT_OK && null != data && counter == 2) {
                    //get text from voice input
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                    mComposedEmail.setText(result.get(0));
                    counter++;
                    handleResult();

                }

                else if(resultCode == RESULT_OK && null != data && counter == 3){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //SET TEXT TO TEXT VIEW
                    mChoice.setText(result.get(0));
                }

                break;
            }

        }


        validate(mChoice.getText().toString());
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

    private void validate(String Choice) {
        if ((Choice.equals("Listen")) || (Choice.equals("listen"))) {
            read();
        } else if ((Choice.equals("Send")) || (Choice.equals("send"))) {
            sendEmail();
            // Intent emailIntent = new Intent(Intent.ACTION_SEND);
        } else if ((Choice.equals("Cancel")) || (Choice.equals("cancel"))) {
            System.exit(0);
        }

    }
    private void handleResult(){
        String wel = "You have composed your email. Do you want to listen to it, send it or cancel";
        if(mTTS.speak(wel, TextToSpeech.QUEUE_FLUSH, null)==1) {
            listen();
        }
    }
    public class Config {
        public static final String EMAIL ="computerproject714@gmail.com";
        public static final String PASSWORD ="computerproject1";
    }

    public class SendMail extends AsyncTask<Void,Void,Void> {

        //Declaring Variables
        private Context context;
        private Session session;

        //Information to send email
        private String email;
        private String subject;
        private String message;

        //Progressdialog to show while sending email
        private ProgressDialog progressDialog;

        //Class Constructor
        public SendMail(Context context, String email, String subject, String message){
            //Initializing variables
            this.context = context;
            this.email = email;
            this.subject = subject;
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Showing progress dialog while sending email
            progressDialog = ProgressDialog.show(context,"Sending message","Please wait...",false,false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Dismissing the progress dialog
            progressDialog.dismiss();
            //Showing a success message
            Toast.makeText(context,"Message Sent",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Creating properties
            Properties props = new Properties();

            //Configuring properties for gmail
            //If you are not using gmail you may need to change the values
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                        }
                    });

            try {
                //Creating MimeMessage object
                MimeMessage mm = new MimeMessage(session);

                //Setting sender address
                mm.setFrom(new InternetAddress(Config.EMAIL));
                //Adding receiver
                mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                //Adding subject
                mm.setSubject(subject);
                //Adding message
                mm.setText(message);

                //Sending email
                Transport.send(mm);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private void sendEmail() {
        //Getting content for email
        String email = mRecipient.getText().toString().trim()+"@gmail.com";
        email = email.toLowerCase();
        email = email.replaceAll("\\s+","");
        String subject = mSubject.getText().toString().trim();
        String message = mComposedEmail.getText().toString().trim();

        //Creating SendMail object
        SendMail sm = new SendMail(this, email, subject, message);

        //Executing sendmail to send email
        sm.execute();
    }

}