package com.example.roadsurface;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class AccelerometrMenu extends AppCompatActivity implements SensorEventListener, View.OnClickListener  {
    Button start, stop, read, clear;
    TextView xText;
    TextView yText;
    TextView zText;
    private Sensor mySensor;
    private SensorManager SM;
    private GpsTracker gpsTracker;
    private TextView tvLatitude, tvLongitude;
    DBHelper dbhelper;
    private Handler mHandler = new Handler();

    private Context context;
    private Observer dataCenter;

    private SensorManager sensorManager;
    private Sensor magnetometerSensor;
    private Sensor accelerometerSensor;
    private Sensor gravitySensor;

    private float[] mAccelerometerReading = new float[3];
    private float[] mMagnetometerReading = new float[3];
    private float[] mGravityReading = new float[3];
    private boolean mAccelerometerDataRecived = false, mMagnetometerDataRecived = false, mGravityDataRecived = false;

    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationAngles = new float[3];

    private float angleOfFixedDevice = 0;
    private boolean calibrated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        start = (Button) findViewById(R.id.start);
        start.setOnClickListener((View.OnClickListener) this);

        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener((View.OnClickListener) this);

        read = (Button) findViewById(R.id.read);
        read.setOnClickListener((View.OnClickListener) this);

        clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener((View.OnClickListener) this);

        dbhelper = new DBHelper(this);

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);
        tvLatitude = (TextView) findViewById(R.id.latitude);
        tvLongitude = (TextView) findViewById(R.id.longitude);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private boolean sensorsInit() {
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            if (sensorManager == null) {
                return false;
            }
            List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
            return true;
        }

    private long lastTimeStamp = 0;
    private long currentTimeStamp = 0;

    public void onSensorChanged(SensorEvent event) {
        compute();
        xText.setText("X: " + event.values[0]);
        yText.setText("Y: " + event.values[1]);
        zText.setText("Z: " + linearAcceleration[2]);
    }

    private float linearAcceleration[] = new float[3];
    private float lastLinearAcceleration[] = new float[3];
    private void compute() {
        if (mGravityReading != null && mMagnetometerReading != null) {
            boolean rotationMatrixResult = SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
            if (rotationMatrixResult) {
                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

                System.arraycopy(linearAcceleration, 0, lastLinearAcceleration, 0, lastLinearAcceleration.length);

                linearAcceleration[0] = (float) (mAccelerometerReading[0] * (Math.cos(mOrientationAngles[2]) * Math.cos(mOrientationAngles[0]) + Math.sin(mOrientationAngles[2]) * Math.sin(mOrientationAngles[1]) * Math.sin(mOrientationAngles[0])) + mAccelerometerReading[1] * (Math.cos(mOrientationAngles[1]) * Math.sin(mOrientationAngles[0])) + mAccelerometerReading[2] * (-Math.sin(mOrientationAngles[2]) * Math.cos(mOrientationAngles[0]) + Math.cos(mOrientationAngles[2]) * Math.sin(mOrientationAngles[1]) * Math.sin(mOrientationAngles[0])));
                linearAcceleration[1] = (float) (mAccelerometerReading[0] * (-Math.cos(mOrientationAngles[2]) * Math.sin(mOrientationAngles[0]) + Math.sin(mOrientationAngles[2]) * Math.sin(mOrientationAngles[1]) * Math.cos(mOrientationAngles[0])) + mAccelerometerReading[1] * (Math.cos(mOrientationAngles[1]) * Math.cos(mOrientationAngles[0])) + mAccelerometerReading[2] * (Math.sin(mOrientationAngles[2]) * Math.sin(mOrientationAngles[0]) + Math.cos(mOrientationAngles[2]) * Math.sin(mOrientationAngles[1]) * Math.cos(mOrientationAngles[0])));
                linearAcceleration[2] = (float) (mAccelerometerReading[0] * (Math.sin(mOrientationAngles[2]) * Math.cos(mOrientationAngles[1])) + mAccelerometerReading[1] * (-Math.sin(mOrientationAngles[1])) + mAccelerometerReading[2] * (Math.cos(mOrientationAngles[2]) * Math.cos(mOrientationAngles[1])));
                

                if (!calibrated) {
                    angleOfFixedDevice = mOrientationAngles[1];
                } else {
                    linearAcceleration[2] = linearAcceleration[2] / (float) Math.acos(mOrientationAngles[1] - angleOfFixedDevice);
                }

                Log.d("TIMESTAMP", "AccelerationData_timestamp: " + currentTimeStamp);
                AccelerationData accelerationData = new AccelerationData(linearAcceleration[2], currentTimeStamp);
                dataCenter.handleData(accelerationData);

                mAccelerometerDataRecived = false;
                mGravityDataRecived = false;
                mMagnetometerDataRecived = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

        }

    public void getLocation(View view) {
        gpsTracker = new GpsTracker(AccelerometrMenu.this);
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            tvLatitude.setText(String.valueOf(latitude));
            tvLongitude.setText(String.valueOf(longitude));

        } else {
            gpsTracker.showSettingsAlert();
        }
    }
    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String x = xText.getText().toString();
        String y = yText.getText().toString();
        String z = zText.getText().toString();


        switch (v.getId()) {
            case R.id.start:
                mToastRunnable.run();
                break;

            case R.id.stop:
                mHandler.removeCallbacks(mToastRunnable);
                break;

            case R.id.read:
                Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                    int zIndex = cursor.getColumnIndex(DBHelper.KEY_Z);
                    int xIndex = cursor.getColumnIndex(DBHelper.KEY_X);
                    int yIndex = cursor.getColumnIndex(DBHelper.KEY_Y);
                    do {
                        Log.d("mLog", "ID = " + cursor.getInt(idIndex) +
                                ", x = " + cursor.getString(xIndex) +
                                ", y = " + cursor.getString(yIndex) +
                                ", z = " + cursor.getString(zIndex));
                    } while (cursor.moveToNext());
                } else
                    Log.d("mLog", "0 rows");
                cursor.close();
                break;

            case R.id.clear:
                database.delete(DBHelper.TABLE_CONTACTS, null, null);
                break;
        }
        dbhelper.close();
    }

    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            SQLiteDatabase database = dbhelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            String x = xText.getText().toString();
            String y = yText.getText().toString();
            String z = zText.getText().toString();
            contentValues.put(DBHelper.KEY_X, x);
            contentValues.put(DBHelper.KEY_Y, y);
            contentValues.put(DBHelper.KEY_Z, z);
            database.insert(DBHelper.TABLE_CONTACTS, null, contentValues);
            mHandler.postDelayed(this, 2000);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}