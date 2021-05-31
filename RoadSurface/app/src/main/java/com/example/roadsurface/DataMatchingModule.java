package com.example.roadsurface;

import android.util.Log;
import java.util.ArrayList;
import java.util.Random;


public class DataMatchingModule implements Observer, Observable {

    private boolean mLocationDataReceived = false;
    private boolean mAccelerationDataReceived = false;
    private ArrayList<LocationData> locationData = new ArrayList<>();
    LocationData lastLocation;
    private ArrayList<AccelerationData> accelerationData = new ArrayList<>();

    long lastUpdateTimeForFake = -1;
    int fakeVibrationLvl = 0;
    int secondsToFakeUpdateRoadStatus = 0;
    @Override
    public void handleData(Object data) {
        if(data instanceof LocationData) {
            mLocationDataReceived = true;
            //locationData.add((LocationData) data);
            lastLocation = (LocationData) data;
            //System.out.println("Location data");
            //System.out.println(locationData.get(locationData.size() - 1).lat + " " + locationData.get(locationData.size() - 1).lon);
        } else if(data instanceof AccelerationData) {
            mAccelerationDataReceived = true;
            //accelerationData.add((AccelerationData) data);
            Random random = new Random();
            int r = random.nextInt(100);
            double vibration = 0;
            vibration = random.nextDouble() + 3;
            vibration = random.nextDouble();

            if(lastUpdateTimeForFake == -1) {
                lastUpdateTimeForFake = System.currentTimeMillis() / 1000;
                secondsToFakeUpdateRoadStatus = random.nextInt(15);
                fakeVibrationLvl = random.nextInt(3);
            }
            if((System.currentTimeMillis() / 1000) - lastUpdateTimeForFake > secondsToFakeUpdateRoadStatus ) {
                lastUpdateTimeForFake = System.currentTimeMillis() / 1000;
                fakeVibrationLvl = random.nextInt(3);
                secondsToFakeUpdateRoadStatus = random.nextInt(15);
            }

            switch (fakeVibrationLvl) {
                case 0:
                    vibration = random.nextDouble();
                    break;
                case 1:
                    vibration = 1 + random.nextDouble() + random.nextDouble();
                    break;
                default:
                    vibration = 3 + random.nextDouble() + random.nextInt(5);
                    break;
            }


            //Fake:
            AccelerationData vb = (AccelerationData) data;
            AccelerationData vbFake = new AccelerationData(vibration, ((AccelerationData) data).time);
            accelerationData.add(vb);
            Log.d("TIMESTAMP", "accelerationData_add_timestamp: " + vb.time);

        } else if(data instanceof ErrorData) {
            System.out.println(data.toString());
        } else {
            System.out.println("Unknown data");
        }
    }

    private double getAvgVibration(ArrayList<AccelerationData> vibration) {
        if(vibration.size() == 0)
            return 0;

        double sum = 0;
        for (int i = 0; i < vibration.size(); i++) {
            sum += vibration.get(i).vibration;
        }
        return sum / vibration.size();
    }

    private boolean isDataReady() {
        if (mLocationDataReceived && mAccelerationDataReceived) {
            mLocationDataReceived = false;
            mAccelerationDataReceived = false;
            return true;
        }
        else
            return false;
    }

    @Override
    public void setObserver(Observer observer) {

    }

    @Override
    public void removeObserver(Observer observer) {

    }
}
