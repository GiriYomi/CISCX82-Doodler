package com.example.ciscx82_doodler;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity; // Updated import
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity { // Changed to extend AppCompatActivity
    private DoodleView doodleView;
    private int currentBrushSize = 10;
    private int currentOpacity = 255; // Default fully opaque
    private static final int PERMISSION_REQUEST_CODE = 1;

    // ActivityResultLauncher for loading image
    private ActivityResultLauncher<Intent> loadImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doodleView = findViewById(R.id.doodleView);

        // Clear Button
        Button clearButton = findViewById(R.id.btnClear);
        clearButton.setOnClickListener(view -> doodleView.clearCanvas());

        // Save Button
        Button saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(view -> {
            if (checkPermission()) {
                saveDoodle();
            } else {
                requestPermission();
            }
        });

        // Load Button
        Button loadButton = findViewById(R.id.btnLoad);
        loadButton.setOnClickListener(view -> {
            if (checkReadPermission()) {
                loadImage();
            } else {
                requestReadPermission();
            }
        });

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

        // Opacity
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

        // Initialize the ActivityResultLauncher for loading images
        loadImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            doodleView.loadBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
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

    // Method to save the doodle
    private void saveDoodle() {
        Bitmap bitmap = doodleView.getBitmap();

        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "Doodle_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Doodles");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                // For Android 9 and below
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Doodles";
                File file = new File(imagesDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String fileName = "Doodle_" + System.currentTimeMillis() + ".png";
                File image = new File(file, fileName);
                fos = new FileOutputStream(image);
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Doodle saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving doodle: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Method to check storage permission
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Permission is automatically granted on Android 10 and above
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Method to request storage permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0){
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(writeAccepted){
                    saveDoodle();
                }else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == READ_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean readAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (readAccepted) {
                    loadImage();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Load Image Method
    private void loadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        loadImageLauncher.launch(intent);
    }

    // Check Read Permission
    private static final int READ_PERMISSION_REQUEST_CODE = 2;

    private boolean checkReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Read permission is not needed for loading images
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestReadPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST_CODE);
    }
}
