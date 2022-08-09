package com.dhyun.portal.voice.client.microphone;

import com.dhyun.portal.voice.client.MicrophoneException;

public interface Microphone {

    void open() throws MicrophoneException;

    void start();

    void stop();

    void close();

    boolean isOpen();

    boolean isStarted();

    int available();

    short[] read();

}
