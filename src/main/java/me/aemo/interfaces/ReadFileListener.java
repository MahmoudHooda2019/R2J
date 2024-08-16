package me.aemo.interfaces;

public interface ReadFileListener {
    void onSuccess(String content);
    void onError(String error, String errorFrom);
}
