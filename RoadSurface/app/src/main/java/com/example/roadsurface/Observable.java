package com.example.roadsurface;

public interface Observable {
    void setObserver(Observer observer);
    void removeObserver(Observer observer);
}
