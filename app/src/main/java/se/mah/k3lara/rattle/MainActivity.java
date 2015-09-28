package se.mah.k3lara.rattle;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ValueEventListener {

    private static Firebase myFirebaseRef;
    private static final String TAG = "MainActivity";
    private static String id = "11000";
    private SensorManager mSensorManager;
    private Sensor mAccelerolmeter;
    private static int triggervalue = 7;
    private static float maxvalue = triggervalue; //used to norm the output
    private static Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get vibrator
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        /*Get sensors*/
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerolmeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        /*Firebase*/
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://blinding-heat-7399.firebaseio.com/"+id);
        myFirebaseRef.child("rattle").addValueEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerolmeter, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    //Accelerometer stuff
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float meanValue = Math.abs((event.values[0] + event.values[1] + event.values[2])/3);
            if (meanValue >triggervalue){
                if (maxvalue < meanValue){
                    maxvalue = meanValue;
                }
                float vibrateTime = 100*((meanValue-triggervalue)/(maxvalue-triggervalue)); // 0-100ms
                myFirebaseRef.child("rattle").setValue(Math.round(vibrateTime)); //send
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //Firebase stuff
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        try {
            int i = Integer.parseInt(dataSnapshot.getValue().toString());
            mVibrator.vibrate(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
