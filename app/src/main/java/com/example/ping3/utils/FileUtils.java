package com.example.ping3.utils;

import android.os.Environment;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


public class FileUtils {

    public static File createFileWithExtension(String extension) {
        File path = Environment.getExternalStoragePublicDirectory(
                "Chat");
        File file = new File(path, FirebaseAuth.getInstance().getCurrentUser().getUid() + "-" + Calendar.getInstance()
                .getTimeInMillis() + "." + extension);

        if (!path.exists()) {
            path.mkdirs();
        }
        try {

            Log.w("Chat", "File: " + file.getAbsolutePath());

            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;

    }
}
