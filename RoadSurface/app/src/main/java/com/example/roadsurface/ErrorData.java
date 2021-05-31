package com.example.roadsurface;

/**
 * Created by Serge on 26.11.2017.
 */

public class ErrorData {
    public enum ErrorTypes {LocationError, AccelerationError, InternetError};
    private static final String[] errorsMsg = {"Can't get access to Location Service", "Can't get access to Acceleration Service", "Can't get access to Internet Service"};
    private ErrorTypes errorType;

    public ErrorData(ErrorTypes errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        switch (errorType) {
            case LocationError:
                return errorsMsg[0];
            case AccelerationError:
                return errorsMsg[1];
            case InternetError:
                return errorsMsg[2];
            default:
                return "Something wrong";
        }
    }
}
