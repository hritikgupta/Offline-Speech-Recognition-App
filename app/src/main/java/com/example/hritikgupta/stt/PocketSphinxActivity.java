package com.example.hritikgupta.stt;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {


    private static final String trigger = "open camera";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    final MediaPlayer mp=new MediaPlayer();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Say \''Open Camera\''");

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);

                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    result.printStackTrace();
                } else {
                    recognizer.startListening(trigger);
                }
            }
        }.execute();
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            return;
        }

        String text = hypothesis.getHypstr();
        if (text.equals(trigger)) {
            Toast.makeText(getApplicationContext(),"Opening camera",Toast.LENGTH_SHORT).show();

            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);

                {
                    if(mp.isPlaying())
                    {
                        mp.stop();
                    }

                    try {
                        mp.reset();
                        AssetFileDescriptor afd;
                        afd = getAssets().openFd("short-notice.mp3");
                        mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                        mp.prepare();
                        mp.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            recognizer.stop();
            recognizer.startListening(trigger);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        //no use
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(Exception error) {
        error.printStackTrace();
    }

    @Override
    public void onTimeout() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setKeywordThreshold(1e-45f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();
        recognizer.addListener(this);
        recognizer.addKeyphraseSearch(trigger, "open camera");
    }
}