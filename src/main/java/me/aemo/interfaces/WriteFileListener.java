package me.aemo.interfaces;

public interface WriteFileListener {
    void onSuccess();
    void onError(String error);
}
