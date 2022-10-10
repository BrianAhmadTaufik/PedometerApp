package com.daniyalak.stepcounterkotlin_androidfitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText name,height,username,password,weight;
    Button RegisterButton;
    SharedPreferences sharedPreferences;
    DBHelper myDB;

    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT= "weight";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBarColor();

        name = (EditText) findViewById(R.id.name);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        username = (EditText)findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        RegisterButton = (Button) findViewById(R.id.RegisterButton);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);
        myDB = new DBHelper(this);

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor =sharedPreferences.edit();
                editor.putString(KEY_HEIGHT,height.getText().toString());
                editor.putString(KEY_WEIGHT,weight.getText().toString());
                editor.apply();

                String user = username.getText().toString();
                String pass = password.getText().toString();
                String hght = height.getText().toString();
                String wght = weight.getText().toString();
                String nama = name.getText().toString();

                if(user.equals("") || pass.equals("") || hght.equals("") || wght.equals("") || nama.equals("")){
                    Toast.makeText(RegisterActivity.this, "Fill all the fields.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Boolean usercheckResult = myDB.checkusername(user);
                    if(usercheckResult == false){
                        Boolean regResult = myDB.insertData(user,pass,hght,wght,nama);
                        if(regResult == true) {
                            if (regResult == true) {
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else {
                        Toast.makeText(RegisterActivity.this,"User already Exits.\n Please Sign In", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}