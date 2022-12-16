package rodic.aleksa.miberchatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button mLogin, mRegister;
    private EditText mUsername, mPassword;

    private boolean usernameEntered, passwordEntered;

    // Zadatak 3. Database
    SQLHelper sqlHelper;
    SharedPreferences sharedPreferences;

    // Zadatak 4. HTTP
    private HTTPHelper httpHelper;
    private Handler handler;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEntered = false;
        passwordEntered = false;

        mUsername = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);

        // Database usage
        sqlHelper = new SQLHelper(this, Constants.APPLICATION_DATABASE, null, 1);

        // SharedPreferences usage
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFF, Context.MODE_PRIVATE);

        // HTTP
        handler = new Handler();
        httpHelper = new HTTPHelper();

        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= Constants.USERNAME_LENGTH) {
                    usernameEntered = true;
                } else {
                    usernameEntered = false;
                }

                // Reset error if accrued, entered unknown username
                mUsername.setError(null);

                // If username and password are valid enable login button
                if (usernameEntered && passwordEntered) {
                    mLogin.setEnabled(true);
                } else {
                    mLogin.setEnabled(false);
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
                if (charSequence.length() >= Constants.PASSWORD_LENGTH) {
                    passwordEntered = true;
                } else {
                    passwordEntered = false;
                }

                // If username and password are valid enable login button
                if (usernameEntered && passwordEntered) {
                    mLogin.setEnabled(true);
                } else {
                    mLogin.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mLogin = findViewById(R.id.login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String username = mUsername.getText().toString();

                final Intent contacts = new Intent(MainActivity.this, ContactsActivity.class);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("username", mUsername.getText().toString());
                            jsonObject.put("password", mPassword.getText().toString());

                            final ServerRespond serverRespond = httpHelper.postJSONObject(Constants.HTTP_LOGIN, jsonObject, null);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    if (serverRespond.getCode() == 200) {
                                        Toast.makeText(MainActivity.this, R.string.successfully_login, Toast.LENGTH_SHORT).show();

                                        // Transfer username via Bundle
                                        Bundle transferUsername = new Bundle();
                                        transferUsername.putString(Constants.BUNDLE_KEY_USERNAME, username);
                                        contacts.putExtras(transferUsername);

                                        // Adding session id in sharedPreferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(Constants.SHARED_PREFF_USER_ID, serverRespond.getSessionId());
                                        editor.putString(Constants.SHARED_PREFF_LOGED_USERNAME, username);
                                        editor.apply();

                                        startActivity(contacts);
                                        finish();  // closing register activity
                                    } else if (serverRespond.getCode() == 400){
                                        Toast.makeText(MainActivity.this, R.string.wrong_body_parameters, Toast.LENGTH_SHORT).show();
                                    } else if (serverRespond.getCode() == 404){
                                        Toast.makeText(MainActivity.this, R.string.user_is_not_registered_or_invalid_password, Toast.LENGTH_SHORT).show();
                                    } else if (serverRespond.getCode() == 409){
                                        Toast.makeText(MainActivity.this, R.string.user_already_exists, Toast.LENGTH_SHORT).show();
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

        mRegister = findViewById(R.id.register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(MainActivity.this, RegisterActivity.class);

                startActivity(register);
            }
        });

    }
}
