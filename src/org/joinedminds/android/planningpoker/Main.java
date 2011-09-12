/*
 * The MIT License
 *
 * Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.joinedminds.android.planningpoker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * Main login activity.
 */
public class Main extends Activity {

    public static final int DIALOG_CONFIRM_EXIT = 1;

    private EditText serverEdit;
    private EditText userEdit;
    private EditText passwordEdit;
    private ProgressDialog loginProgressDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        serverEdit = (EditText)findViewById(R.id.serverEdit);
        userEdit = (EditText)findViewById(R.id.userEdit);
        passwordEdit = (EditText)findViewById(R.id.passwordEdit);

        Button btn = (Button)findViewById(R.id.exitBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(DIALOG_CONFIRM_EXIT);
            }
        });
        btn = (Button)findViewById(R.id.loginBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startLogin();
            }
        });

    }

    private void startLogin() {
        loginProgressDialog = ProgressDialog.show(this, "", "logging in...", true);
        new LoginThread(loginHandler,
                serverEdit.getText().toString(),
                userEdit.getText().toString(),
                passwordEdit.getText().toString()).start();
    }

    final Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Exception) {
                Exception e = (Exception)msg.obj;
                Toast.makeText(Main.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loginProgressDialog.dismiss();
            } else if (msg.obj instanceof Connection) {
                Toast.makeText(Main.this, "OK", Toast.LENGTH_LONG);
                loginProgressDialog.dismiss();
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONFIRM_EXIT:
                return createConfirmExitDialog();
        }
        return null;
    }

    private Dialog createConfirmExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.exitAreYouSure)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Main.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private static class LoginThread extends Thread {
        private Handler handler;
        private String server;
        private String username;
        private String password;

        public LoginThread(Handler handler, String server, String username, String password) {
            super("Login-Thread");
            this.handler = handler;
            this.server = server;
            this.username = username;
            this.password = password;
        }

        @Override
        public void run() {
            Connection conn = new XMPPConnection(server);
            try {
                conn.connect();
                conn.login(username, password, "planning-poker-xmpp");
                handler.dispatchMessage(handler.obtainMessage(1, conn));
            } catch (XMPPException e) {
                Message msg = handler.obtainMessage(1, e);
                handler.dispatchMessage(msg);
            }
        }
    }
}
