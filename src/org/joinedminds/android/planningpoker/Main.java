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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Main login activity.
 */
public class Main extends Activity {

    public static final int DIALOG_CONFIRM_EXIT = 1;

    private EditText serverEdit;
    private EditText userEdit;
    private EditText passwordEdit;
    private ProgressDialog loginProgressDialog;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences(Constants.SHARED_LOGINSETTINGS, Activity.MODE_PRIVATE);
        serverEdit.setText(sharedPreferences.getString(Constants.PREF_KEY_SERVER, Constants.DEFAULT_SERVER));
        userEdit.setText(sharedPreferences.getString(Constants.PREF_KEY_USERNAME, Constants.DEFAULT_USERNAME));
        passwordEdit.setText(sharedPreferences.getString(Constants.PREF_KEY_PASSWORD, Constants.DEFAULT_PASSWORD));

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

    private void startLogin() {

        String server = serverEdit.getText().toString();
        String username = userEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_KEY_SERVER, server);
        editor.putString(Constants.PREF_KEY_USERNAME, username);
        editor.putString(Constants.PREF_KEY_PASSWORD, password);
        editor.commit();

        //TODO Start Activity
        Intent startIntent = new Intent(Main.this, PlanningActivity.class);
        startActivity(startIntent);
    }
}
