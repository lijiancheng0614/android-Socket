package com.example.android.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends ActionBarActivity {
    public final MyHandler mHandler = new MyHandler(this);
    public TextView mLogTextView = null;
    public EditText mServerAddressEditText = null;
    public TextView mRequestEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mLogTextView = (TextView) findViewById(R.id.mainLogTextView);
        mServerAddressEditText = (EditText) findViewById(R.id.mainServerAddressEditText);
        mRequestEditText = (TextView) findViewById(R.id.mainRequestEditText);
        findViewById(R.id.mainRequestButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                message.what = 0;
                mHandler.sendMessage(message);
            }
        });
    }

    public static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        public void requestString(MainActivity activity) {
            Socket socket = null;
            try {
                String address = activity.mServerAddressEditText.getText().toString();
                String[] parts = address.split(":");
                socket = new Socket(InetAddress.getByName(parts[0]), Integer.parseInt(parts[1]));
                activity.mLogTextView.setText(activity.mLogTextView.getText() +
                        "Connecting " + address);
                // request message
                String request = activity.mRequestEditText.getText().toString() + "\n";
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.write(request);
                activity.mLogTextView.setText(activity.mLogTextView.getText() +
                        "Request: " + request);
                out.flush();

                // receive message
                socket.setSoTimeout(5000);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                activity.mLogTextView.setText(activity.mLogTextView.getText() +
                        "Received: " + response + "\n");

                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        requestString(activity);
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
