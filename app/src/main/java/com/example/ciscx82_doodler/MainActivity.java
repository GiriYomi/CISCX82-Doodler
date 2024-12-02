package com.example.ciscx82_doodler;


import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends Activity {
    private DoodleView doodleView;
    private int currentBrushSize = 10;
    private int currentOpacity = 255; // Default fully opaque

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doodleView = findViewById(R.id.doodleView);

        // Clear Button
        Button clearButton = findViewById(R.id.btnClear);
        clearButton.setOnClickListener(view -> doodleView.clearCanvas());

        // Brush Size
        SeekBar brushSizeBar = findViewById(R.id.brushSizeBar);
        brushSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBrushSize = progress;
                doodleView.setBrushSize(currentBrushSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar opacityBar = findViewById(R.id.opacityBar);
        opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentOpacity = progress;
                doodleView.setBrushOpacity(currentOpacity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Color Picker
        Button colorPickerButton = findViewById(R.id.colorPicker);
        colorPickerButton.setOnClickListener(view -> showColorPickerDialog());
    }

    private void showColorPickerDialog() {
        final int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.BLACK};

        // Create the GridView for colors
        GridView gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setVerticalSpacing(10);
        gridView.setHorizontalSpacing(10);
        gridView.setPadding(10, 10, 10, 10);

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return colors.length;
            }

            @Override
            public Object getItem(int position) {
                return colors[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View colorBox = new View(MainActivity.this);
                colorBox.setLayoutParams(new ViewGroup.LayoutParams(100, 100)); // Make boxes smaller
                colorBox.setBackgroundColor(colors[position]);
                return colorBox;
            }
        });

        AlertDialog colorDialog = new AlertDialog.Builder(this)
                .setTitle("Choose Brush Color")
                .setView(gridView)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            doodleView.setBrushColor(colors[position]); // Set the selected color
            Toast.makeText(MainActivity.this, "Color changed!", Toast.LENGTH_SHORT).show();
            colorDialog.dismiss();
        });

        colorDialog.show();
    }

}
