package org.joinedminds.android.planningpoker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.jivesoftware.smack.XMPPException;
import org.joinedminds.android.planningpoker.logic.PlanningListener;
import org.joinedminds.android.planningpoker.logic.PlanningManager;

import java.io.IOException;

import static org.joinedminds.android.planningpoker.Constants.*;

/**
 * Created: 9/14/11 9:21 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class BeginActivity extends Activity implements PlanningListener {

    public static final int DIALOG_LOGIN_PROGRESS = 2;
    public static final int DIALOG_INVITE = 4;
    private SharedPreferences preferences;
    private ProgressDialog loginProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(SHARED_LOGINSETTINGS, MODE_PRIVATE);
        setContentView(R.layout.startplanning);
        startLogin();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOGIN_PROGRESS:
                loginProgressDialog = new ProgressDialog(this);
                loginProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loginProgressDialog.setMessage("Logging in...");
                return loginProgressDialog;
            case DIALOG_INVITE:
                return createInviteDialog();
        }
        return null;
    }

    private Dialog createInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setMessage("Provide the user names to invite separated by \";\"") //TODO use xmpp roster
                .setCancelable(false)
                .setView(editText)
                .setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String[] users = editText.getText().toString().split(";");
                        try {
                            PlanningManager.getInstance().startPlanning(users);
                            Intent startIntent = new Intent(BeginActivity.this, PlanningActivity.class);
                            startActivity(startIntent);
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

    public void inviteClick(View v) {
        showDialog(DIALOG_INVITE);
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
    public void communicationError(Exception ex) {
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void invite(String fromUser, String[] participants) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void newRound(String fromUser) {
        //Ignore?
    }

    @Override
    public void cardSelect(String fromUser, String card) {
        //Ignore
    }

    @Override
    public void inviteResponse(String fromUser, boolean accepted) {
        //To change body of implemented methods use File | Settings | File Templates.
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