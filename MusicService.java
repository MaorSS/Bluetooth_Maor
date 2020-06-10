package com.example.bluetooth_maor;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer; // מכין אובייקט מסוג מדיה פלייר כדי שנוכל להפעיל שיר


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = MediaPlayer.create(this, R.raw.idf); // אני חושב שקונטקסט במובן הזה אומר על מי או איפה יתבצע השיר והפרמטר השני אומר לנו באיזה שיר הוא ישתמש
        mediaPlayer.setLooping(true); // יהיה לופים כל עוד לא נעצור את השיר
        mediaPlayer.start(); // השיר יתחיל


        return START_STICKY; // ימשיך להריץ את הפונקציה במידה ויש מספיק זיכרון והיא קרסה 
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop(); // מפסיק את המוזיקה
    }
}
