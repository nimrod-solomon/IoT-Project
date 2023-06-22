package com.example.tutorial6;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    String userWeight = "";
    String userHeight = "";
    String userCaloriesTarget = "";
    @SuppressLint("SdCardPath") String dataDirPath = "/sdcard/csv_dir";
    String dataFileName = "project_data";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the data file
        /*File dir = new File(dataDirPath);
        if (!dir.exists()) { dir.mkdir(); }
        String csvFilePath = dataDirPath + "/" + dataFileName + ".csv";
        try {
            File file = new File(csvFilePath);
            boolean fileExists = file.exists();
            FileWriter writer = new FileWriter(file, true);
            if(!fileExists) writer.append("SessionTime,StepsCount,CaloriesCount\n");

            // todo: these lines create 50 random records.
            //  should be deleted sometime and be replaced with real data lines
            for (int i = 0; i < 50; i++) {
                String randomDate = randPastWeekDate();
                Random random = new Random();
                int randomNumSteps = random.nextInt(1001);
                int randomNumCalories = randomNumSteps / 25 ;
                String line = randomDate + "," + randomNumSteps + "," + randomNumCalories + "\n";
                writer.write(line); }
            writer.flush();
            writer.close(); }
        catch (IOException e) { Log.d("Debug", Objects.requireNonNull(e.getMessage())); }*/

        // Set buttons and text boxes
        Button buttonStartPractice = findViewById(R.id.startPracticeButton);
        EditText editTextUserWeight = findViewById(R.id.weightEditText);
        TextWatcher textWatcherUserWeight = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userWeight = editTextUserWeight.getText().toString();
                Log.d("Debug", "userWeight = " + userWeight); }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }
            @Override
            public void afterTextChanged(Editable s) {  } };
        editTextUserWeight.addTextChangedListener(textWatcherUserWeight);
        EditText editTextUserHeight = findViewById(R.id.heightEditText);
        TextWatcher textWatcherUserHeight = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userHeight = editTextUserHeight.getText().toString();
                Log.d("Debug", "userHeight = " + userHeight); }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }
            @Override
            public void afterTextChanged(Editable s) {  } };
        editTextUserHeight.addTextChangedListener(textWatcherUserHeight);
        EditText editTextUserCaloriesTarget = findViewById(R.id.goalEditText);
        TextWatcher textWatcherUserCaloriesTarget = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userCaloriesTarget = editTextUserCaloriesTarget.getText().toString();
                Log.d("Debug", "userCaloriesTarget = " + userCaloriesTarget); }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }
            @Override
            public void afterTextChanged(Editable s) {  } };
        editTextUserCaloriesTarget.addTextChangedListener(textWatcherUserCaloriesTarget);

        // Set bar chart
        BarChart barChart = findViewById(R.id.barchart);
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);
        LegendEntry l1 = new LegendEntry("Steps", Legend.LegendForm.CIRCLE,10f,2f,null, Color.BLUE);
        LegendEntry l2 = new LegendEntry("Calories", Legend.LegendForm.CIRCLE,10f,2f,null, Color.CYAN);
        legend.setCustom(new LegendEntry[]{l1,l2});
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE); colors.add(Color.CYAN); colors.add(Color.BLUE);
        colors.add(Color.CYAN); colors.add(Color.BLUE); colors.add(Color.CYAN);
        colors.add(Color.BLUE); colors.add(Color.CYAN); colors.add(Color.BLUE);
        colors.add(Color.CYAN); colors.add(Color.BLUE); colors.add(Color.CYAN);
        colors.add(Color.BLUE); colors.add(Color.CYAN);

        // read data from data file to bar plot
        @SuppressLint("SdCardPath") ArrayList<String[]> csvProjectData = CsvRead();
        ArrayList<ArrayList<Integer>> steps = new ArrayList<>();
        ArrayList<ArrayList<Integer>> calories = new ArrayList<>();
        for (int i = 0; i < 7; i ++) {
            steps.add(new ArrayList<>());
            calories.add(new ArrayList<>()); }
        ArrayList<String> dates = new ArrayList<>();
        for (String[] row : csvProjectData){
            if (row[0].equals("SessionTime")) continue;
            String date = row[0].split(" ")[0];
            if (!dates.contains(date)) { dates.add(date);} }
        Collections.sort(dates);
        while (dates.size() > 7) {dates.remove(0);}
        for (String[] row : csvProjectData){
            if (row[0].equals("SessionTime")) continue;
            String date = row[0].split(" ")[0];
            int numSteps = Integer.parseInt(row[1]);
            int numCalories = Integer.parseInt(row[2]);
            int index = dates.indexOf(date);
            if (index > -1) {
                steps.get(index).add(numSteps);
                calories.get(index).add(numCalories); } }
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 21; i += 3) {
            int sumSteps = 0; for (int number : steps.get(i / 3)) { sumSteps += number; }
            entries.add(new BarEntry(i, sumSteps));
            int sumCalories = 0; for (int number : calories.get(i / 3)) { sumCalories += number; }
            entries.add(new BarEntry(i + 1, sumCalories)); }
        BarDataSet set  = new BarDataSet(entries, "BarDataSet");
        set.setColors(colors);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(8f);
        BarData data = new BarData(set);
        data.setBarWidth(0.9f);
        barChart.setData(data);
        String[] xAxisLables = new String[]{
                dates.get(0).substring(0,5), "", "", dates.get(1).substring(0,5), "", "",
                dates.get(2).substring(0,5), "", "", dates.get(3).substring(0,5), "", "",
                dates.get(4).substring(0,5), "", "", dates.get(5).substring(0,5), "", "",
                dates.get(6).substring(0,5), "", ""};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLables));
        xAxis.setGridColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.invalidate();
        buttonStartPractice.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { ClickStartSession(); } });

    }

    private void onBackStackChanged() { }


    private void ClickStartSession() {
        Intent intent = new Intent(this, Progress.class);
        intent.putExtra("userSettings", userHeight + ", " + userWeight + ", " + userCaloriesTarget);
        Log.d("Debug", "data sent");
        startActivity(intent); }

    private ArrayList<String[]> CsvRead() {
        ArrayList<String[]> csvData = new ArrayList<>();
        try {
            @SuppressLint("SdCardPath") File file = new File("/sdcard/csv_dir/project_data.csv");
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine = reader.readNext();
            while ((nextLine) != null) { csvData.add(nextLine); nextLine = reader.readNext(); } }
        catch (Exception e) { Log.d("Debug", Objects.requireNonNull(e.getMessage())); }
        return csvData; }

    private String randPastWeekDate() {
        Date now = new Date();  // Get the current date
        Date startOfWeek = new Date(now.getTime() - (7 * 24 * 60 * 60 * 1000));  // Get the start of the past week
        Date endOfWeek = new Date(now.getTime());  // Get the end of the past week
        // Generate a random number between the start and end dates
        Random random = new Random();
        int randomDay = random.nextInt((int) (endOfWeek.getTime() - startOfWeek.getTime()));
        Date randomDate = new Date(startOfWeek.getTime() + randomDay);  // Get the random date
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return dateFormat.format(randomDate); }


}


