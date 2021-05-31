package com.example.roadsurface;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class AccelerationModule implements Observable, SensorEventListener {

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

    public AccelerationModule(Context context) {
        this.context = context;
    }

    @Override
    public void setObserver(Observer observer) {
        this.dataCenter = observer;
    }

    public void startMonitoring() {
        if(!sensorsInit())
            dataCenter.handleData(new ErrorData(ErrorData.ErrorTypes.AccelerationError));
        else
            System.out.println("Acceleration initialization successful");

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
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {
                System.arraycopy(sensorEvent.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
                mAccelerometerDataRecived = true;
                currentTimeStamp = sensorEvent.timestamp;
                Log.d("TIMESTAMP", "Current_timestamp: " + sensorEvent.timestamp);
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                System.arraycopy(sensorEvent.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
                mMagnetometerDataRecived = true;
            }
            case Sensor.TYPE_GRAVITY: {
                System.arraycopy(sensorEvent.values, 0, mGravityReading, 0, mGravityReading.length);
                mGravityDataRecived = true;
            }
        }

        if(mAccelerometerDataRecived && mGravityDataRecived && mMagnetometerDataRecived) {
            compute();
        }
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

                if(!calibrated) {
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

    @Override
    public void removeObserver(Observer observer) {
    }

    public void stopMonitoring() {
        sensorManager.unregisterListener(this);
        /*magnetometerSensor = null;
        accelerometerSensor = null;
        gravitySensor = null;
        mAccelerometerReading = null;
        mMagnetometerReading = null;
        mGravityReading = null;
        mRotationMatrix = null;
        mOrientationAngles = null;*/
    }
}