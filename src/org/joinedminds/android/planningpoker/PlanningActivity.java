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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import static org.joinedminds.android.planningpoker.Constants.*;
import static org.joinedminds.android.planningpoker.Constants.CARD_VALUES;

/**
 * Created: 9/13/11 8:25 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlanningActivity extends Activity {

    public static final int DIALOG_LOGIN_PROGRESS = 2;
    public static final int OPTION_INVITE_TO_PLAN = 3;

    private SharedPreferences preferences;
    private ProgressDialog loginProgressDialog;
    private GridView gridView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(SHARED_LOGINSETTINGS, MODE_PRIVATE);

        setContentView(R.layout.cardgrid);
        gridView = (GridView)findViewById(R.id.cardgrid);
        gridView.setAdapter(new ArrayAdapter<String>(this, R.layout.card, CARD_VALUES));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view instanceof TextView) {
                    cardSelected(((TextView)view).getText().toString());
                }
            }
        });

        startLogin();
    }

    private void cardSelected(String s) {
        Toast.makeText(this, "Card Selected: " + s, Toast.LENGTH_SHORT).show();
        System.out.println("Card Selected: " + s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, OPTION_INVITE_TO_PLAN, Menu.NONE, R.string.optionInviteToPlan);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOGIN_PROGRESS:
                loginProgressDialog = new ProgressDialog(this);
                loginProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loginProgressDialog.setMessage("logging in...");
                return loginProgressDialog;
        }
        return null;
    }

    private void startLogin() {
        showDialog(DIALOG_LOGIN_PROGRESS);

        String server = preferences.getString(PREF_KEY_SERVER, "");
        String username = preferences.getString(PREF_KEY_USERNAME, "");
        String password = preferences.getString(PREF_KEY_PASSWORD, "");

        new LoginThread(loginHandler,
                server,
                username,
                password).start();
    }

    final Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Exception) {
                final Exception e = (Exception)msg.obj;
                Runnable runnable = new Runnable() {
                    public void run() {
                        Toast.makeText(PlanningActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loginProgressDialog.dismiss();
                        PlanningActivity.this.finish();
                    }
                };
                runOnUiThread(runnable);
            } else if (msg.obj instanceof Connection) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        Toast.makeText(PlanningActivity.this, "OK", Toast.LENGTH_LONG).show();
                        loginProgressDialog.dismiss();
                    }
                };
                runOnUiThread(runnable);
            }
        }
    };

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