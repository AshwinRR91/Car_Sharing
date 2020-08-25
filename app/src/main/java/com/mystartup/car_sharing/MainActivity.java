package com.mystartup.car_sharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private RadioButton signUpLoginDriver,signUpLoginPassenger,oneTimeLoginDriver,oneTimeLoginPassenger;
    private Button loginSignUpButton, oneTimeLogin;
    private enum STATE{ SIGN_UP, LOGIN};
    private STATE state;
    private EditText username,password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        state = STATE.SIGN_UP;
        username = findViewById(R.id.user_name);
        password = findViewById(R.id.password);
        signUpLoginDriver = findViewById(R.id.signup_login_driver);
        signUpLoginPassenger = findViewById(R.id.signup_login_passenger);
        loginSignUpButton = findViewById(R.id.signup_login);
        oneTimeLoginDriver =findViewById(R.id.one_time_driver);
        oneTimeLoginPassenger = findViewById(R.id.one_time_passenger);
        oneTimeLogin = findViewById(R.id.one_time_login);
        loginSignUpButton.setOnClickListener(this);
        oneTimeLogin.setOnClickListener(this);
        if(ParseUser.getCurrentUser()!=null){
            switchActivity();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(state == STATE.SIGN_UP){
            loginSignUpButton.setText("LOGIN");
            state = STATE.LOGIN;
            item.setTitle("LOGIN");
        }
        else{
            loginSignUpButton.setText("SIGN UP");
            state = STATE.SIGN_UP;
            item.setTitle("SIGN_UP");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.signup_login:
                if(state==STATE.SIGN_UP){
                    if(signUpLoginDriver.isChecked()|| signUpLoginPassenger.isChecked()){
                        ParseUser parseUser = new ParseUser();
                        if(signUpLoginDriver.isChecked()){
                            parseUser.put("As","Driver");
                        }
                        else if(signUpLoginPassenger.isChecked()) parseUser.put("As","Passenger");
                        parseUser.setUsername(username.getText().toString());
                        parseUser.setPassword(password.getText().toString());
                        parseUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e!=null){
                                    Toast.makeText(MainActivity.this,"Sign up error"+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }
                    else{
                        Toast.makeText(MainActivity.this,"Please select the option",Toast.LENGTH_LONG).show();
                    }
                }
                else if(state == STATE.LOGIN){
                    ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(e==null){
                                switchActivity();
                            }

                        }
                    });
                }
                break;
            case R.id.one_time_login:
                if(oneTimeLoginDriver.isChecked()|| oneTimeLoginPassenger.isChecked()){
                    if(oneTimeLoginDriver.isChecked()){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user!=null && e == null){
                                    user.put("As","Driver");
                                    switchActivity();
                                }
                                else {
                                    Toast.makeText(MainActivity.this,"OneLogin error"+e.getMessage(),Toast.LENGTH_LONG).show();
                                } };}); }
                    else if( oneTimeLoginPassenger.isChecked()){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user!=null && e == null){
                                    user.put("As","Passenger");
                                    switchActivity();
                                }
                                else {
                                    Toast.makeText(MainActivity.this,"OneLogin error"+e.getMessage(),Toast.LENGTH_LONG).show();
                                } }}); } }
                break;
                }

        }
        private void switchActivity(){
        if(ParseUser.getCurrentUser().get("As").equals("Driver")){
          Intent intent = new Intent(MainActivity.this,DriverActivity.class);
          startActivity(intent);
        }
           else if(ParseUser.getCurrentUser().get("As").equals("Passenger")){
            Intent intent = new Intent(MainActivity.this,PassengerActivity.class);
            startActivity(intent);
           }
        }
    }
