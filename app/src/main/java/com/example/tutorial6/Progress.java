package com.example.tutorial6;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.ProgressBar;
import android.os.Bundle;
import android.os.Handler;
import java.util.Random;

import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.lang.Math;

public class Progress extends AppCompatActivity {
    Handler handler =  new Handler();
    Runnable runnable;
    EditText time;
    LineChart mpLineChart;
    Runnable DataUpdate;
    String numSteps = "";
    long sessionStartTime;
    private static final long TIME_DIFFERENCE = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
    float x = 0.0F, y = 0.0F, z = 0.0F, xPrev = 0.0F, yPrev = 0.0F, zPrev = 0.0F;
    List<Float> xRest = new ArrayList<>();
    List<Float> yRest = new ArrayList<>();
    List<Float> zRest = new ArrayList<>();
    float xSum = 0.0F, ySum = 0.0F, zSum = 0.0F;
    double t = 0;
    float N = 0.0F;
    int estimatedNumSteps = 0;
    int estimatedCaloriesBurned = 0;
    private final Handler mHandlar = new Handler();
    public String[] btDataRow;
    int targetSteps = 10000; // Example target number of steps
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        Button BackButton = (Button) findViewById(R.id.StopButton);
        TextView estimatedNumStepsTextView = (TextView) findViewById(R.id.Steps);
        TextView estimatedCaloriesBurnedTextView = (TextView) findViewById(R.id.Calories);
        time = (EditText) findViewById(R.id.Time);
        ProgressBar stepsprogressBar = findViewById(R.id.StepsprogressBar);
        ProgressBar caloriesprogressBar = findViewById(R.id.CaloriesprogressBar);
        TextView CaloriesPercentage = (TextView) findViewById(R.id.caloriesPercentage);
        TextView stepsPercentage = (TextView) findViewById(R.id.stepsPercentage);

        stepsprogressBar.setMax(100);
        caloriesprogressBar.setMax(100);

        Intent intent = getIntent();
        float weight = intent.getFloatExtra("weight",0);
        int height = intent.getIntExtra("age", 0);
        int caloriesTarget = intent.getIntExtra("caloriestarget", 0);
        int stepsTarget = (int) ((caloriesTarget - (0.57 * weight) - (0.415 * height)) / 0.032);



        BackButton.setOnClickListener(v -> ClickBack());
/*
            getSupportFragmentManager().addOnBackStackChangedListener(this);
            if (ContextCompat.checkSelfPermission(ProgressActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ProgressActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0); }

            if (savedInstanceState == null)
                getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
            else
                onBackStackChanged();
*/
        startThread();

        sessionStartTime = System.currentTimeMillis(); //+ TIME_DIFFERENCE;

        // Update the time initially
        updateTime();

        // Schedule automatic time updates every second
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000);
            }
        }, 1000);



        DataUpdate = new Runnable() {
            @SuppressLint({"SdCardPath", "SetTextI18n"})
            @Override
            public void run() {

                N = (float) Math.pow(x*x+y*y+z*z, 0.5);

                if(t < 1.0) { xRest.add(x); yRest.add(y); zRest.add(z);} // Initial data collection
                else {

                    // update the values in rest lists to re-calculate the coordinates of rest state
                    xRest.remove(0);
                    xRest.add(x);
                    yRest.remove(0);
                    yRest.add(y);
                    zRest.remove(0);
                    zRest.add(z);

                    xSum = ySum = zSum = 0;
                    for (int i = 0; i < xRest.size(); i++) {
                        xSum += xRest.get(i);
                        ySum += yRest.get(i);
                        zSum += zRest.get(i);
                    }

                    float xNormalized = x - xSum / xRest.size();
                    float yNormalized = y - ySum / yRest.size();
                    float zNormalized = z - zSum / zRest.size();

                    float N_normalized = (float) Math.pow(xNormalized * xNormalized + yNormalized * yNormalized + zNormalized * zNormalized, 0.5);
                    //Log.d("Normalized State", "normalized state coordinates = (" + xNormalized + ", " + yNormalized + ", " + zNormalized + "). N_normalized = " + N_normalized);

                    // check every 0.5 seconds if step was done and update relevant fields if so
                    float threshold = 2.0F; // in rest, N_normalized ~ 0.05 m/sec^2
                    if (( (t - (int)t)==0 || (t - (int)t)==0.5) && N_normalized > threshold) {
                        estimatedNumSteps += 1;
                        estimatedNumStepsTextView.setText("Estimated Number of Steps: " + estimatedNumSteps);
                        estimatedCaloriesBurned += (0.57 * weight) + (0.415 * height) + (0.032 * estimatedNumSteps);
                        estimatedCaloriesBurnedTextView.setText("Estimated Number of Steps: " + estimatedCaloriesBurned);
                        float stepPercentage = (estimatedNumSteps / stepsTarget) * 100;
                        float caloriesPercentage = (estimatedCaloriesBurned / caloriesTarget) * 100;
                        String percentageStepsText = stepPercentage + "%";
                        stepsPercentage.setText(percentageStepsText);
                        String percentageCaloriesText = caloriesPercentage + "%";
                        CaloriesPercentage.setText(percentageCaloriesText);
                        stepsprogressBar.setProgress((int) stepPercentage);
                        caloriesprogressBar.setProgress((int) caloriesPercentage);

                    }
                }

                xPrev = x;
                yPrev = y;
                zPrev = z;
                //try {x = Float.parseFloat(btDataRow[1]);} catch (Exception e) {x = xPrev;}
                //try {y = Float.parseFloat(btDataRow[2]);} catch (Exception e) {y = yPrev;}
                //try {z = Float.parseFloat(btDataRow[3]);} catch (Exception e) {z = zPrev;}
                Random rand = new Random();
                try {x = rand.nextFloat();} catch (Exception e) {x = xPrev;}
                try {y = rand.nextFloat();} catch (Exception e) {y = yPrev;}
                try {z = rand.nextFloat();} catch (Exception e) {z = zPrev;}
                t += 0.02; t = Math.round(t * 100) / 100.0;
                //Log.d("BlueToothDataStream", "t = " + t + ", " + "x = " + x + ", " + "y = " + y + ", " + "z = " + z + ", " + "N = " + N);
                mHandlar.postDelayed(this, 20); }
        };
        handler.postDelayed(DataUpdate,20);
/*
            BackButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    mHandlar.removeCallbacks(DataUpdate);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        saveToCsv("/sdcard/csv_dir/","data_file");
                        mHandlar.removeCallbacks(DataUpdate); }

                }});
*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the callback when the activity is destroyed to prevent memory leaks
        handler.removeCallbacks(runnable);
    }

    private void updateTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - sessionStartTime;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String elapsedTimeFormatted = sdf.format(new Date(elapsedTime));
        time.setText(elapsedTimeFormatted);

    }

    public void startThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    btDataRow = TerminalFragment.getDataRow();
                    // Sleep for 0.02 seconds.
                    try { Thread.sleep(20); } catch (InterruptedException e) { }
                }
            }
        };
        thread.start();
    }

    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return true;
    }

    private void ClickBack(){ finish(); }

    private ArrayList<Entry> dataValues1() {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        dataVals.add(new Entry(0.0F,0.0F));
        return dataVals; }

    private void saveToCsv(String dirPath, String filename) {
        if (numSteps.equals("")) {
            Toast.makeText(this, "Error: number of steps empty", Toast.LENGTH_SHORT).show();
            return; }
        else if (filename.equals("")) {
            Toast.makeText(this, "Error: file name empty", Toast.LENGTH_SHORT).show();
            return; }
        File dir = new File(dirPath);
        if (!dir.exists()) { dir.mkdir(); Log.d("error saveToCsv", "dir exists");}
        Log.d("error saveToCsv", "dir exists");
        String csvFilePath = dirPath + filename + ".csv";
        Log.d("error saveToCsv", "csvFilePath = " + csvFilePath);

        try {
            // todo: tomer to tell me how to append rows to files and not re-create them every time
            File file = new File(csvFilePath);
            FileWriter writer = new FileWriter(file);
            writer.append("TIME:"); writer.append(",");  writer.append("ESTIMATED NUMBER OF STEPS:");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();
            String formattedDateAndTime = simpleDateFormat.format(date);
            writer.append(formattedDateAndTime);writer.append(","); writer.append(Integer.toString(estimatedNumSteps));
            writer.flush();
            writer.close();

            // Show a success message or perform other actions
            Toast.makeText(this, "data saved successfully", Toast.LENGTH_SHORT).show(); }
        catch (IOException e) {
            e.printStackTrace();
            Log.d("error", e.getMessage());
            // Show an error message or perform error handling
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show(); }

        //RESET
        LineData data = mpLineChart.getData();
        ILineDataSet set_x = data.getDataSetByIndex(0);
        ILineDataSet set_y = data.getDataSetByIndex(1);
        ILineDataSet set_z = data.getDataSetByIndex(2);
        ILineDataSet set_N = data.getDataSetByIndex(3);
        set_x.clear();
        set_y.clear();
        set_z.clear();
        set_N.clear();
        mpLineChart.invalidate();
        t = 0;
        estimatedNumSteps = 0;
        estimatedCaloriesBurned = 0;
        //TextView estimatedNumStepsTextView = (TextView) findViewById(R.id.estimated_num_steps_textView);
        //estimatedNumStepsTextView.setText("Estimated Number of Steps: " + estimatedNumSteps);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}