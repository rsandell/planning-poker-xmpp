/*
 *  The MIT License
 *
 *  Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.joinedminds.android.planningpoker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.jivesoftware.smack.XMPPException;
import org.joinedminds.android.planningpoker.logic.PlanningListener;
import org.joinedminds.android.planningpoker.logic.PlanningManager;
import org.joinedminds.android.planningpoker.logic.Round;
import org.joinedminds.android.planningpoker.logic.io.ChatSession;

import static org.joinedminds.android.planningpoker.Constants.*;

/**
 * Created: 9/14/11 9:21 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class BeginActivity extends Activity implements PlanningListener {

    public static final int DIALOG_LOGIN_PROGRESS = 2;
    public static final int DIALOG_SEND_INVITE = 4;
    public static final int DIALOG_INVITE_RECEIVED = 5;
    private SharedPreferences preferences;
    private ProgressDialog loginProgressDialog;
    private static final String DIALOG_INVITE_RECEIVED_FROM_USER = "from-user";
    private static final String DIALOG_INVITE_RECEIVED_PARTICIPANTS = "participants";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(SHARED_LOGINSETTINGS, MODE_PRIVATE);
        setContentView(R.layout.startplanning);
        if (!PlanningManager.isInitiated() || !PlanningManager.getInstance().isConnected()) {
            startLogin();
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOGIN_PROGRESS:
                loginProgressDialog = new ProgressDialog(this);
                loginProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loginProgressDialog.setMessage("Logging in...");
                return loginProgressDialog;
            case DIALOG_SEND_INVITE:
                return createInviteDialog();
            case DIALOG_INVITE_RECEIVED:
                return createInviteReceivedDialog();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        switch (id) {
            case DIALOG_INVITE_RECEIVED:
                Resources resources = getResources();
                String from = args.getString(DIALOG_INVITE_RECEIVED_FROM_USER);
                String participants = nlPerItem(args.getStringArray(DIALOG_INVITE_RECEIVED_PARTICIPANTS));
                AlertDialog alert = (AlertDialog)dialog;
                alert.setMessage(resources.getString(R.string.gotInvite, from, participants));
                break;

        }
    }

    private String nlPerItem(String[] array) {
        StringBuilder str = new StringBuilder();
        for (String i : array) {
            str.append(i).append("\n");
        }
        return str.toString().trim();
    }

    private Dialog createInviteReceivedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Someone wants to play")
                .setCancelable(false)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("ACCEPT!");
                        try {
                            PlanningManager.getInstance().acceptInvite();
                        } catch (Exception e) {
                            e.printStackTrace();
                            communicationError(e);
                        }
                        startPlanningActivity();
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("DECLINE");
                        try {
                            PlanningManager.getInstance().declineInvite();
                        } catch (Exception e) {
                            e.printStackTrace();
                            communicationError(e);
                        }
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    private Dialog createInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setText(preferences.getString(Constants.PREF_KEY_LAST_INVITED, ""));
        builder.setMessage("Provide the user names to invite separated by \";\"") //TODO use xmpp roster
                .setCancelable(false)
                .setView(editText)
                .setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String invited = editText.getText().toString();
                        preferences.edit().putString(Constants.PREF_KEY_LAST_INVITED, invited).commit();
                        String[] users = invited.split(";");
                        try {
                            PlanningManager.getInstance().startPlanning(users);
                            startPlanningActivity();
                        } catch (Exception e) {
                            e.printStackTrace();
                            communicationError(e);
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void communicationError(final Exception ex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BeginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void invite(final String fromUser, final String[] participants) {
        final Bundle args = new Bundle();
        args.putString(DIALOG_INVITE_RECEIVED_FROM_USER, fromUser);
        args.putStringArray(DIALOG_INVITE_RECEIVED_PARTICIPANTS, participants);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDialog(DIALOG_INVITE_RECEIVED, args);
            }
        });
    }

    private void startPlanningActivity() {
        Intent startIntent = new Intent(this, PlanningActivity.class);
        startActivity(startIntent);
    }

    @SuppressWarnings("unused")
    public void inviteClick(View v) {
        showDialog(DIALOG_SEND_INVITE);
    }

    private void startLogin() {
        showDialog(DIALOG_LOGIN_PROGRESS);

        String server = preferences.getString(PREF_KEY_SERVER, "");
        String username = preferences.getString(PREF_KEY_USERNAME, "");
        String password = preferences.getString(PREF_KEY_PASSWORD, "");
        int port = preferences.getInt(PREF_KEY_PORT, DEFAULT_PORT);

        new LoginThread(loginHandler,
                server,
                port,
                username,
                password, this).start();
    }

    final Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Exception) {
                final Exception e = (Exception)msg.obj;
                Runnable runnable = new Runnable() {
                    public void run() {
                        Toast.makeText(BeginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loginProgressDialog.dismiss();
                        e.printStackTrace();
                        BeginActivity.this.finish();
                    }
                };
                runOnUiThread(runnable);
            } else if (msg.obj instanceof Boolean && msg.obj == Boolean.TRUE) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        Toast.makeText(BeginActivity.this, "OK", Toast.LENGTH_LONG).show();
                        loginProgressDialog.dismiss();
                    }
                };
                runOnUiThread(runnable);
            }
        }
    };

    @Override
    public void newRound(String fromUser, Round round) {
        //Ignore?
    }

    @Override
    public void cardSelect(String fromUser, String card, Round round) {
        //Ignore
    }

    @Override
    public void inviteResponse(final String fromUser, final boolean accepted, ChatSession.Participants participants) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources res = getResources();
                if (accepted) {
                    Toast.makeText(BeginActivity.this, res.getString(R.string.userAccepted, fromUser), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BeginActivity.this, res.getString(R.string.userDeclined, fromUser), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private static class LoginThread extends Thread {
        private Handler handler;
        private String server;
        private int port;
        private String username;
        private String password;
        private BeginActivity activity;

        public LoginThread(Handler handler, String server, int port, String username, String password, BeginActivity activity) {
            super("Login-Thread");
            this.handler = handler;
            this.server = server;
            this.port = port;
            this.username = username;
            this.password = password;
            this.activity = activity;
        }

        @Override
        public void run() {
            try {
                PlanningManager.initiate(server, port, username, password, activity);
                handler.dispatchMessage(handler.obtainMessage(1, Boolean.TRUE));
            } catch (XMPPException e) {
                Message msg = handler.obtainMessage(1, e);
                handler.dispatchMessage(msg);
            }
        }
    }
}