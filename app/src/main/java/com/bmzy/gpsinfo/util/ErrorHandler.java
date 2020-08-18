package com.bmzy.gpsinfo.util;

import android.os.Environment;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

public class ErrorHandler implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler mDefault;

    public ErrorHandler(UncaughtExceptionHandler mDefault) {
        this.mDefault = mDefault;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        writeRhrowable(ex);

        if (mDefault != null) {
            mDefault.uncaughtException(thread, ex);
        }
    }

    public static void writeRhrowable(Throwable ex) {
        try {
            String filepath = Environment.getExternalStorageDirectory() + "/error2.0.txt";
            PrintWriter writer = new PrintWriter(new FileWriter(filepath));
            ex.printStackTrace(writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
