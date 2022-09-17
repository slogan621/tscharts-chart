/*
 * (C) Copyright Syd Logan 2017-2021
 * (C) Copyright Thousand Smiles Foundation 2017-2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmileschart;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.LoginREST;
import org.thousandsmiles.tscharts_lib.ShowProgress;

import me.philio.pinentry.PinEntryView;

public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);//Menu Resource, Menu
        return true;
    }

    private void showHelp()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_enter_username_then_either_password_or_pin)));
        alertDialogBuilder.setPositiveButton(R.string.button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionSingleton s = SessionSingleton.getInstance();
        s.setContext(getApplicationContext());
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.customImeActionId || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mCreateAccountButton = (Button) findViewById(R.id.create_account_button);
        mCreateAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthDialogFragment rtc = new AuthDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable("type", AuthDialogFragment.AuthDialogType.AUTH_DIALOG_CREATE_ACCOUNT);
                rtc.setArguments(args);
                rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.action_create_account));
            }
        });

        Button mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        mResetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthDialogFragment rtc = new AuthDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable("type", AuthDialogFragment.AuthDialogType.AUTH_DIALOG_CHANGE_PASSWORD);
                rtc.setArguments(args);
                rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.action_reset_password));
            }
        });

        Button mResetPINButton = (Button) findViewById(R.id.reset_pin_button);
        mResetPINButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthDialogFragment rtc = new AuthDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable("type", AuthDialogFragment.AuthDialogType.AUTH_DIALOG_CHANGE_PIN);
                rtc.setArguments(args);
                rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.action_reset_pin));
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        final boolean isValidPin;
        boolean isValidPassword;
        boolean isValidUser;

        final String email = mEmailView.getText().toString();
        View focusView = null;

        if (email.length() > 0) {
            isValidUser = true;
        } else {
            isValidUser = false;
            focusView = mEmailView;
        }

        PinEntryView pinEntryView = (PinEntryView) this.findViewById(R.id.pin_entry_simple);

        final String pin = pinEntryView.getText().toString();

        if (pin.length() == 4) {
            isValidPin = true;
        } else {
            isValidPin = false;
        }

        // Store values at the time of the login attempt.

        final String password = mPasswordView.getText().toString();

        boolean cancel = false;

        if (password.length() > 0) {
            isValidPassword = true;
        } else {
            isValidPassword = false;
            if (isValidUser && !isValidPin) {
                focusView = mPasswordView;
            } else if (isValidUser) {
                focusView = pinEntryView;
            }
        }

        if (!isValidUser || (!isValidPassword && !isValidPin)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            new Thread(new Runnable() {
                public void run() {
                    final Object lock;
                    final LoginREST x = new LoginREST(getApplicationContext());
                    if (isValidPin) {  // favor pin over password
                        lock = x.signIn(email, null, pin);
                    } else {
                        lock = x.signIn(email, password, null);
                    }

                    Thread thread = new Thread(){
                        public void run() {
                            synchronized (lock) {
                                // we loop here in case of race conditions or spurious interrupts
                                while (true) {
                                    try {
                                        lock.wait();
                                        break;
                                    } catch (InterruptedException e) {
                                        continue;
                                    }
                                }
                            }

                            if (x.getStatus() == 200) {
                                Intent i = new Intent(LoginActivity.this, StationSelectorActivity.class);
                                startActivity(i);
                                finish();
                                return;
                            } else if (x.getStatus() == 101) {
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if (x.getStatus() == 400) {
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if (x.getStatus() == 500) {
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if (x.getStatus() == 403 || x.getStatus() == 404) {
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.error_invalid_username_or_password, Toast.LENGTH_LONG).show();
                                }
                                });

                            } else {
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        String msg;
                                        msg = String.format("%s %d", R.string.error_unknown, x.getStatus());
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                            Intent i = new Intent(LoginActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                            return;
                        }
                    };

                    thread.start();

                }
            }).start();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */

    private void showProgress(final boolean show) {
        ShowProgress progress = new ShowProgress();
        progress.showProgress(this, mLoginFormView, mProgressView, show);
    }
}

