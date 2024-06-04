package com.yogie.anemiaapps.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yogie.anemiaapps.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class activity_input_manual extends AppCompatActivity {
    private PreviewView viewFinder;
    private boolean useFrontCamera = true;
    private ImageView imageView;
    private ImageCapture imageCapture;
    private File photoFile;
    private FirebaseStorage storage;

    private TextView jadwal, waktu, waktu_berjalan, waktu_capture;

    private Runnable runnable;
    private Handler handler = new Handler();
    MaterialButton backButton, captureButton, changeCameraButton, save, cancel;

    String fileName;

    loadingDialog loading;
    UserPref userPref;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    long curentTimeInMilis;
    String tanggal;

    String timeCatch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_manual);

        viewFinder = findViewById(R.id.viewFinder);
        backButton = findViewById(R.id.backButton);
        captureButton = findViewById(R.id.image_capture_button);
        imageView = findViewById(R.id.overlay);
        waktu_capture = findViewById(R.id.tv_waktu_minum_obat);

        captureButton = findViewById(R.id.image_capture_button);

        loading = new loadingDialog(this);
        userPref = new UserPref(this);
        loading.setMessage("Menyimpan data..");

        save = findViewById(R.id.save_button);
        cancel = findViewById(R.id.take_again);

        jadwal = findViewById(R.id.tv_jadwal);
        waktu = findViewById(R.id.tv_waktu);

        waktu_berjalan = findViewById(R.id.tv_waktu_berjalan);

        handler.post(runnable);

        Bundle extras = getIntent().getExtras();
        requestCameraPermission();

        if(extras != null){
            tanggal = extras.getString("tanggal");
            jadwal.setText(extras.getString("waktu"));
            waktu.setText(extras.getString("jam"));
        }

        showTimePickerDialog();

        cancel.setOnClickListener(v -> {
            imageView.setVisibility(View.GONE);
            viewFinder.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.VISIBLE);
            save.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            changeCameraButton.setVisibility(View.VISIBLE);



            handler.post(runnable);
            waktu_capture.setVisibility(View.GONE);

            if (photoFile != null && photoFile.exists()) {
                photoFile.delete();
            }

        });

        captureButton.setOnClickListener(v -> {
            takePicture();

        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        save.setOnClickListener(v->{
            uploadToFireStorage();
        });

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, useFrontCamera);
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors
            }
        }, ContextCompat.getMainExecutor(this));

        changeCameraButton = findViewById(R.id.change_camera_button);

        changeCameraButton.setOnClickListener(v -> {
            useFrontCamera = !useFrontCamera;
            Snackbar.make(viewFinder, "Mengganti kamera!", Snackbar.LENGTH_SHORT).show();

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider, useFrontCamera);
                } catch (ExecutionException | InterruptedException e) {
                    // Handle any errors
                }
            }, ContextCompat.getMainExecutor(this));
        });

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider, boolean useFrontCamera) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(useFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);


        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);


    }

    void takePicture(){

        this.fileName = new SimpleDateFormat("dd-MM-yy-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg";
        photoFile = new File(getCacheDir(), this.fileName);

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                imageView.setImageURI(savedUri);

                imageView.setVisibility(View.VISIBLE);
                viewFinder.setVisibility(View.GONE);

                captureButton.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                changeCameraButton.setVisibility(View.GONE);

                handler.removeCallbacks(runnable);

                waktu_berjalan.setVisibility(View.GONE);
                waktu_capture.setVisibility(View.VISIBLE);
                waktu_capture.setText("Waktu minum obat "+waktu_berjalan.getText().toString());
                timeCatch = waktu_berjalan.getText().toString();

                compressImageFile(photoFile);

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraXApp", "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private File getOutputDirectory() {
        File mediaDir = getExternalMediaDirs()[0];
        if (mediaDir != null) {

            mediaDir = new File(mediaDir, getResources().getString(R.string.app_name));
            mediaDir.mkdirs();

        }
        return mediaDir != null ? mediaDir : getFilesDir();
    }

    private void uploadToFireStorage(){
        // Upload foto ke Firebase Storage
        storage = FirebaseStorage.getInstance();
        loading.setMessage("Mengupload foto..");
        loading.show();
        // Simpan data ke Firestore
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child("images/"+userPref.getUserData().get("documentId").toString()+"/"+fileName);

        photoRef.putFile(Uri.fromFile(photoFile)).addOnSuccessListener(taskSnapshot -> {
            saveData();
        }).addOnFailureListener(e -> {
            loading.dismiss();
            Toast.makeText(this, "Gagal upload foto", Toast.LENGTH_SHORT).show();
        });

    }

    private void saveData(){
        // Simpan data ke Firestore
        loading.setMessage("Menyimpan data..");
        Map<String,Object> data = new HashMap<>();
        data.put("waktu", waktu_capture.getText().toString());
        data.put("status", "sudah");
        data.put("foto", fileName);
        data.put("jadwal", jadwal.getText().toString());

        String tgl_now = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(curentTimeInMilis)+"_"+waktu.getText().toString();
        data.put("created_at_day", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(curentTimeInMilis));
        data.put("created_at_time", timeCatch);
        data.put("created_at", curentTimeInMilis);


        db.collection("users").document(userPref.getUserData().get("documentId").toString()).collection("minum_obat").document(tgl_now).set(data).addOnSuccessListener(documentReference -> {

            loading.dismiss();
            Toast.makeText(this, "Berhasil menyimpan data", Toast.LENGTH_SHORT).show();
            finish();

        }).addOnFailureListener(e -> {

            loading.dismiss();
            Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hentikan Runnable saat Activity dihancurkan
        handler.removeCallbacks(runnable);
    }

    private void compressImageFile(File photoFile) {
        try {
            // Ubah file menjadi Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            // Cek ukuran file asli
            int originalSize = bitmap.getByteCount();

            // Tentukan kualitas kompresi awal
            int quality = 100;

            // Buat ByteArrayOutputStream untuk menampung data bitmap
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            // Kompresi bitmap sampai ukuran file di bawah 1MB
            while (outputStream.toByteArray().length > 1024 * 1024) {
                outputStream.reset(); // Reset ByteArrayOutputStream
                quality -= 10; // Kurangi kualitas
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            }

            // Buat file baru dengan ukuran yang sudah dikompresi
            FileOutputStream fos = new FileOutputStream(photoFile);
            fos.write(outputStream.toByteArray());
            fos.flush();
            fos.close();

            // Cek ukuran file setelah dikompresi
            int compressedSize = outputStream.toByteArray().length;

            Log.d("CompressImage", "Original size: " + originalSize + ", Compressed size: " + compressedSize);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final int CAMERA_REQUEST_CODE = 100;
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            // Tampilkan penjelasan mengapa aplikasi membutuhkan izin kamera
            Toast.makeText(this, "Aplikasi ini membutuhkan akses ke kamera Anda", Toast.LENGTH_LONG).show();
        }

        // Meminta izin kamera
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan
                Toast.makeText(this, "Izin kamera diberikan", Toast.LENGTH_SHORT).show();
            } else {
                // Izin kamera ditolak
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showTimePickerDialog() {
        // Mendapatkan waktu saat ini
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // Membuat TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Tindakan yang dilakukan setelah waktu dipilih
                        String selectedTime = hourOfDay + ":" + minute + ":" + "00";
                     // Contoh: menampilkan waktu yang dipilih menggunakan Toast
                        waktu_berjalan.setText(selectedTime);
                        timeCatch = selectedTime;
                        try {
                            curentTimeInMilis = convertStringToMillis(tanggal + " " + selectedTime);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
}, hour, minute, true);

        // Menampilkan TimePickerDialog
        timePickerDialog.show();
    }

    private long convertStringToMillis(String dateString) throws ParseException {
        // Menentukan format tanggal yang sesuai dengan string
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        // Mem-parsing string menjadi objek Date
        Date date = sdf.parse(dateString);
        // Mengembalikan waktu dalam milidetik
        return date.getTime();
    }

}