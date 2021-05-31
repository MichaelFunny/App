package com.example.roadsurface;


public class AccelerationData {
    public long time;
    public double vibration;

    AccelerationData(double vibration) {
        this.vibration = vibration;
        this.time = 0;
    }


    public AccelerationData(double vibration, long time) {
        this.vibration = vibration;
        this.time = time;
    }

    public boolean correctVibrationValue(double correctingValue) {
        if(vibration - correctingValue > 0)
            this.vibration -= correctingValue;
        else
            this.vibration = 0;

        return true;
    }
}
