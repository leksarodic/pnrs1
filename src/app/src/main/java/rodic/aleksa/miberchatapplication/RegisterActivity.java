package rodic.aleksa.miberchatapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private Button mRegister;
    private EditText mUsername, mPassword, mEmail;
    private EditText mFirstname, mLastname;
    private DatePicker mDate;

    public boolean usernameEntered, passwordEntered, emailEntered;

    // Database
    SQLHelper sqlHelper;

    // HTTP usage
    private HTTPHelper httpHelper;
    private Handler handler;

    // FIXME: Skipped X frames! The application may be doing too much work on its main thread.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegister = findViewById(R.id.register);
        mDate = findViewById(R.id.date);

        mUsername = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        mEmail = findViewById(R.id.email);

        mFirstname = findViewById(R.id.firstname);
        mLastname = findViewById(R.id.lastname);

        usernameEntered = false;
        passwordEntered = false;
        emailEntered = false;

        // Database usage
        sqlHelper = new SQLHelper(this, Constants.APPLICATION_DATABASE, null, 1);

        // HTTP
        handler = new Handler();
        httpHelper = new HTTPHelper();

        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Validation, enable register button or not
                usernameEntered = (charSequence.length() >= Constants.USERNAME_LENGTH);

                // Clear error triggered with taken username
                mUsername.setError(null);

                // Change button enable/disable status if username, password and email are entered
                if (usernameEntered && passwordEntered && emailEntered) {
                    mRegister.setEnabled(true);
                } else {
                    mRegister.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordEntered = charSequence.length() >= Constants.PASSWORD_LENGTH;

                // Change button enable/disable status if username, password and email are entered
                if (usernameEntered && passwordEntered && emailEntered) {
                    mRegister.setEnabled(true);
                } else {
                    mRegister.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                // Real email validation
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()) {
                    emailEntered = true;
                } else {
                    emailEntered = false;
                }

                // Change button enable/disable status if username, password and email are entered
                if (usernameEntered && passwordEntered && emailEntered) {
                    mRegister.setEnabled(true);
                } else {
                    mRegister.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Deny people to chose some old dates. Also they need to be 18+ to register.
        Calendar legalAge = Calendar.getInstance();
        legalAge.add(Calendar.YEAR, -18);
        mDate.setMaxDate(legalAge.getTimeInMillis());  // this might be problematic because of onCreate

        Calendar denyOldPeople = Calendar.getInstance();
        denyOldPeople.set(1940, 1, 1);
        mDate.setMinDate(denyOldPeople.getTimeInMillis());  // this might be problematic because of onCreate

        mRegister = findViewById(R.id.register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent main = new Intent(RegisterActivity.this, MainActivity.class);

                // Inserting contact
                String username, firstname, lastname;

                username = mUsername.getText().toString();

                // If user not entered first and last name, make it for them using username, for database
                if (mFirstname.length() >= Constants.FIRSTNAME_LENGTH) {
                    firstname = mFirstname.getText().toString();
                } else {
                    firstname = "first_" + username;
                }

                if (mLastname.length() >= Constants.LASTNAME_LENGTH) {
                    lastname = mLastname.getText().toString();
                } else {
                    lastname = "last_" + username;
                }

                Contact contact = new Contact(username, firstname, lastname);

                // HTTP post
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("username", mUsername.getText().toString());
                            jsonObject.put("password", mPassword.getText().toString());
                            jsonObject.put("email", mEmail.getText().toString());

                            final ServerRespond serverRespond = httpHelper.postJSONObject(Constants.HTTP_REGISTER, jsonObject, null);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (serverRespond.getCode() == 200) {
                                        Toast.makeText(RegisterActivity.this, R.string.successfully_registered, Toast.LENGTH_SHORT).show();
                                        startActivity(main);
                                        finish();  // closing register activity
                                    } else if (serverRespond.getCode() == 400){
                                        Toast.makeText(RegisterActivity.this, R.string.wrong_body_parameters, Toast.LENGTH_SHORT).show();
                                    } else if (serverRespond.getCode() == 409) {
                                        Toast.makeText(RegisterActivity.this, R.string.username_taken_register, Toast.LENGTH_SHORT).show();
                                        mUsername.setError(getText(R.string.username_taken_register));
                                    }
                                }
                            });

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
