package com.daniyalak.stepcounterkotlin_androidfitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class StepCounter extends AppCompatActivity {
    TextView steps;
    TextView distance;
    TextView calories;
    TextView result;
    Button reset;
    SharedPreferences sharedPreferences;
    String url = "https://naivebayespdmanu.herokuapp.com/predict";

    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT= "weight";

    public double MagnitudePrevious = 0;
    public Integer stepCount = 0;
    private RequestQueue requestQueue;
    int i=0;
    final static double walkingFactor = 0.57;
    static double CaloriesBurnedPerMile;
    static double strip;
    static double stepCountMile;
    static double conversationFactor;
    static double CaloriesBurned, distanc;
    public void timeDelay(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {}
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        steps = (TextView) findViewById(R.id.TV_STEPS);
        distance = (TextView) findViewById(R.id.TV_DISTANCE);
        calories = (TextView) findViewById(R.id.TV_CALORIES);
        result = (TextView) findViewById(R.id.result);
        reset = (Button) findViewById(R.id.reset);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        String hg = sharedPreferences.getString(KEY_HEIGHT,null);
        String wg = sharedPreferences.getString(KEY_WEIGHT,null);
        double hght = Double.parseDouble(hg);
        double wght = Double.parseDouble(wg);
        CaloriesBurnedPerMile = walkingFactor * (wght * 2.2);
        strip = hght * 0.415;
        stepCountMile = 160934.4 / strip;
        conversationFactor = CaloriesBurnedPerMile / stepCountMile;
        requestQueue = Volley.newRequestQueue(StepCounter.this);
        SensorEventListener stepDetector = new SensorEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                final float acc_x = sensorEvent.values[0];
                final float acc_y = sensorEvent.values[1];
                final float acc_z = sensorEvent.values[2];
                System.out.println("acc_x =" + acc_x);
                System.out.println("acc_y =" + acc_y);
                System.out.println("acc_z =" + acc_z);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    System.out.println("Data Terklasifikasi");
                                    JSONObject jsonObject = new JSONObject(response);
                                    String data = jsonObject.getString("placement");
                                    if (data.equals("0")) {
                                        result.setText("WALKING");
                                    } else if (data.equals("1")) {
                                        result.setText("RUNNING");
                                    } else {
                                        result.setText("UNKNOWN");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(StepCounter.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("acc_x", String.valueOf(acc_x));
                        params.put("acc_y", String.valueOf(acc_y));
                        params.put("acc_z", String.valueOf(acc_z));
                        System.out.println("Data Terkirim API");
                        return params;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(StepCounter.this);
                queue.add(stringRequest);
                if (sensorEvent!= null){
                    float x_acceleration = sensorEvent.values[0];
                    float y_acceleration = sensorEvent.values[1];
                    float z_acceleration = sensorEvent.values[2];
                    double Magnitude = Math.sqrt(x_acceleration*x_acceleration + y_acceleration*y_acceleration + z_acceleration*z_acceleration);
                    double MagnitudeDelta = Magnitude - MagnitudePrevious;
                    MagnitudePrevious = Magnitude;
                    if (MagnitudeDelta > 5){
                        stepCount++;
                    }
                    CaloriesBurned = stepCount * conversationFactor;
                    distanc = (stepCount * strip) / 100000;
                    distance.setText(String.format("%.3f",distanc));
                    calories.setText(String.format("%.3f",CaloriesBurned));
                    steps.setText(stepCount.toString());
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCount = 0;
                CaloriesBurned = 0.0;
                distanc = 0.0;

            }
        });
    }
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("stepCount", 0);
    }
}
