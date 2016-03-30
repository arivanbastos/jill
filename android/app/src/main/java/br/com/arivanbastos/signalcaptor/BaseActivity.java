package br.com.arivanbastos.signalcaptor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class BaseActivity extends ActionBarActivity
{
    public static Activity currentActivity = null;
    public static Activity root = null;

    public BaseActivity()
    {
        super();
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void onResume() {
        super.onResume();

        init();
    }

    protected void init()
    {
        currentActivity = this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right_enter, R.anim.left_to_right_exit);
    }

    protected void startActivity(Class viewClass, HashMap<String, Object> parameters) {
        Intent intent = new Intent(this, viewClass);
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                Object sourceValue = parameters.get(key);

                if (sourceValue instanceof Serializable) {
                    Serializable value = (Serializable) parameters.get(key);
                    intent.putExtra(key, value);
                } else {
                    String sValue = sourceValue.toString();
                    intent.putExtra(key, sValue);
                }
            }
        }
        startActivity(intent);
        overridePendingTransition(R.anim.right_to_left_enter, R.anim.right_to_left_exit);
    }

    public TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }


    public void beep()
    {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
