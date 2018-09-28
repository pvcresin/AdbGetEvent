package com.resin.adbgetevent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    Thread th;
    boolean isAlive = false;
    int dx, dy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isAlive = true;
        adbCommand("adb", "shell", "getevent", "-lt", "/dev/input/event5"); // event5 -> mouse
    }

    private void adbCommand(String... command) {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            final Process proc = processBuilder.start();

            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream iStream = proc.getInputStream();
                    InputStreamReader isReader = new InputStreamReader(iStream);
                    BufferedReader bufferedReader = new BufferedReader(isReader);

                    while (isAlive) {
                        String line = null;

                        try {
                            line = bufferedReader.readLine();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (line == null) return;

                        String[] results = line.split("    ");

                        if (results.length == 6) {
                            String xy = results[1].substring(3);
                            int d = (int)Long.parseLong(results[5], 16);

                            if (xy.equals("REL_X")) {
                                dx = d;
                                Log.d(TAG, "X: " + d);
                            } else if (xy.equals("REL_Y")) {
                                dy = d;
                                Log.d(TAG, "Y: " + d);
                            }
                        }
                    }
                }
            });

            Toast.makeText(MainActivity.this, "started", Toast.LENGTH_SHORT).show();
            th.start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        isAlive = false;
        super.onDestroy();
    }
}
