package com.example.tutorial6;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    String userWeight = "";
    String userHeight = "";
    String userCaloriesTarget = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            public void afterTextChanged(Editable s) {  }
        };
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
            public void afterTextChanged(Editable s) {  }
        };
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
            public void afterTextChanged(Editable s) {  }
        };
        editTextUserCaloriesTarget.addTextChangedListener(textWatcherUserCaloriesTarget);

        BarChart barChart = findViewById(R.id.barchart);
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);
        LegendEntry l1 = new LegendEntry("Steps", Legend.LegendForm.CIRCLE,10f,2f,null, Color.BLUE);
        LegendEntry l2 = new LegendEntry("Calories", Legend.LegendForm.CIRCLE,10f,2f,null, Color.CYAN);
        legend.setCustom(new LegendEntry[]{l1,l2});

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        @SuppressLint("SdCardPath") ArrayList<String[]> csvData = CsvRead("/sdcard/csv_dir/data.csv");


        ArrayList<Integer> steps = new ArrayList<>();
        ArrayList<Double> calories = new ArrayList<>();

        for (int i = csvData.size() - 7; i < csvData.size(); i++){
            try {
                steps.add(Integer.parseInt(csvData.get(i)[1]));
                calories.add(Double.parseDouble(csvData.get(i)[2])); }
            catch (Exception e) {Log.d("Debug", Objects.requireNonNull(e.getMessage()));}
        }

        List<BarEntry> entries = new ArrayList<>();
        for (float i = 0f; i < 21f; i += 3f){
            int intI = (int) i;
            entries.add(new BarEntry(intI, steps.get(intI / 3)));
            entries.add(new BarEntry(intI+1, calories.get(intI / 3).intValue())); }

        BarDataSet set  = new BarDataSet(entries, "BarDataSet");
        set.setColors(colors);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(8f);
        BarData data = new BarData(set);
        data.setBarWidth(0.9f);
        barChart.setData(data);
        String[] xAxisLables = new String[]{"Sun.", "", "", "Mon.", "", "",
                "Tue.", "", "", "Wed.", "", "", "Thu.", "", "", "Fri.", "", "", "Sat.","", ""};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLables));
        xAxis.setGridColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);

        barChart.invalidate();

        buttonStartPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickStartSession();
            }
        });
    }

    private void ClickStartSession() {
        Intent intent = new Intent(this, Progress.class);
        intent.putExtra("userSettings", userHeight.toString() + ", " + userWeight.toString() + ", " + userCaloriesTarget.toString());
        Log.d("Debug", "data sent");
        startActivity(intent);
    }

    private ArrayList<String[]> CsvRead(String filename) {
        ArrayList<String[]> csvData = new ArrayList<>();
        try {
            File file = new File(filename);
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine = reader.readNext();
            while ((nextLine) != null) {
                csvData.add(nextLine);
                nextLine = reader.readNext();
                }
        }
        catch (Exception e) { Log.d("Debug", e.getMessage()); }
        return csvData;
    }
}


