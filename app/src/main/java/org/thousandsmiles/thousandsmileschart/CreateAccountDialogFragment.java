/*
 * (C) Copyright Syd Logan 2021
 * (C) Copyright Thousand Smiles Foundation 2021
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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.thousandsmiles.tscharts_lib.LoginREST;

import me.philio.pinentry.PinEntryView;

public class CreateAccountDialogFragment extends DialogFragment {

    private View m_view = null;
    private Dialog m_dialog = null;
    private EditText m_first = null;
    private EditText m_last = null;
    private EditText m_email = null;
    private EditText m_emailAgain = null;
    private EditText m_password = null;
    private EditText m_passwordAgain = null;
    private PinEntryView m_pin = null;
    private PinEntryView m_pinAgain = null;

    private boolean validateFields(boolean submit) {
        boolean ret = true;

        AccountFieldValidator validator = new AccountFieldValidator();

        String st1;
        String st2;

        st1 = m_first.getText().toString();
        if (submit && st1.length() == 0) {
            m_first.setError(getString(R.string.error_field_required));
            ret = false;
        }

        st1 = m_last.getText().toString();
        if (submit && st1.length() == 0) {
            m_first.setError(getString(R.string.error_field_required));
            ret = false;
        }

        st1 = m_email.getText().toString();
        if (submit && st1.length() == 0) {
            m_email.setError(getString(R.string.error_field_required));
            ret = false;
        }
        st2 = m_emailAgain.getText().toString();
        if (submit && st2.length() == 0) {
            m_emailAgain.setError(getString(R.string.error_field_required));
            ret = false;
        }

        View v = m_dialog.findViewById(R.id.create_account_email_again_checkbox);
        if (validator.isValidEmail(st1) && validator.isValidEmail(st2) && st1.equals(st2)) {
            // show checkbox next to email 2
            v.setVisibility(View.VISIBLE);
        } else {
            // hide checkbox next to email 2
            v.setVisibility(View.GONE);
            if (submit) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), R.string.error_emails_do_not_match, Toast.LENGTH_LONG).show();
                    }
                });
            }
            ret = false;
        }

        st1 = m_password.getText().toString();
        if (submit && st1.length() == 0) {
            m_password.setError(getString(R.string.error_field_required));
            ret = false;
        }
        st2 = m_passwordAgain.getText().toString();
        if (submit && st2.length() == 0) {
            m_passwordAgain.setError(getString(R.string.error_field_required));
            ret = false;
        }

        v = m_dialog.findViewById(R.id.create_account_password_again_checkbox);
        if (validator.isValidPassword(st1) && validator.isValidPassword(st2) && st1.equals(st2)) {
            // show checkbox next to email 2
            v.setVisibility(View.VISIBLE);
        } else {
            // hide checkbox next to email 2
            v.setVisibility(View.GONE);
            if (submit) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), R.string.error_passwords_do_not_match, Toast.LENGTH_LONG).show();
                    }
                });
            }
            ret = false;
        }

        st1 = m_pin.getText().toString();
        st2 = m_pinAgain.getText().toString();

        if (submit && (st1.length() != 4 || st2.length() != 4)) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.msg_please_enter_pin), Toast.LENGTH_SHORT).show();
        }

        v = m_dialog.findViewById(R.id.create_account_pin_again_checkbox);
        if (st1.length() == 4 && st1.equals(st2)) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
            if (submit) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), R.string.error_pins_do_not_match, Toast.LENGTH_LONG).show();
                    }
                });
            }
            ret = false;
        }

        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        m_dialog = this.getDialog();

        m_first = m_dialog.findViewById(R.id.create_account_first_name);
        m_last = m_dialog.findViewById(R.id.create_account_last_name);
        m_email = m_dialog.findViewById(R.id.create_account_email);
        m_email.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        m_emailAgain = m_dialog.findViewById(R.id.create_account_email_again);
        m_emailAgain.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        m_password = m_dialog.findViewById(R.id.create_account_password);
        m_password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        m_passwordAgain = m_dialog.findViewById(R.id.create_account_password_again);
        m_passwordAgain.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        m_pin = m_dialog.findViewById(R.id.create_account_pin);
        m_pin.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        m_pinAgain = m_dialog.findViewById(R.id.create_account_pin_again);
        m_pinAgain.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        m_dialog.findViewById(R.id.create_account_email_again_checkbox).setVisibility(View.GONE);
        m_dialog.findViewById(R.id.create_account_password_again_checkbox).setVisibility(View.GONE);
        m_dialog.findViewById(R.id.create_account_pin_again_checkbox).setVisibility(View.GONE);

        m_dialog.findViewById(R.id.create_account_password_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(getActivity().getString(R.string.title_password_help));
                builder.setMessage(getActivity().getString(R.string.msg_password_requirements));

                builder.setPositiveButton(getActivity().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void createUser(final Dialog dialog, final String first, final String last, final String eMail, final String password, final String pin) {
        new Thread(new Runnable() {
            public void run() {
                final LoginREST x = new LoginREST(getContext());
                final Object lock = x.createUser(first, last, password, eMail, pin);

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
                            getActivity().runOnUiThread(new Runnable() {
                                 public void run() {
                                     dialog.dismiss();
                                     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                     builder.setTitle(getActivity().getString(R.string.title_please_login));
                                     builder.setMessage(getActivity().getString(R.string.msg_please_login));

                                     builder.setPositiveButton(getActivity().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int which) {
                                             dialog.dismiss();
                                         }
                                     });

                                     AlertDialog alert = builder.create();
                                     alert.show();
                                     Toast.makeText(getContext(), R.string.msg_account_successfully, Toast.LENGTH_LONG).show();
                                     EditText tx = getActivity().findViewById(R.id.email);
                                     tx.setText(m_email.getText());
                                 }
                            });
                        } else if (x.getStatus() == 101) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if (x.getStatus() == 400) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if (x.getStatus() == 500) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if (x.getStatus() == 409) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getContext(), R.string.error_user_already_exists, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                };

                thread.start();

            }
        }).start();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = null;
        final Dialog ret;
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        m_view = inflater.inflate(R.layout.signup_dialog, null);

        builder.setView(m_view)
                    // Add action buttons
                    .setPositiveButton(R.string.action_create_account, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }})
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        dialog = builder.create();
        dialog.show();
        ret = dialog;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields(true)) {
                    createUser(ret,
                            m_first.getText().toString(),
                            m_last.getText().toString(),
                            m_email.getText().toString(),
                            m_password.getText().toString(),
                            m_pin.getText().toString());
                }
            }
        });

        ret.setTitle(R.string.title_create_account);
        return ret;
    }
}