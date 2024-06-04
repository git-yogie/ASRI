package com.yogie.anemiaapps.menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraState;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.AlaramReceiver;
import com.yogie.anemiaapps.helper.UserPref;
import com.yogie.anemiaapps.helper.loadingDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class minumObat_Activity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private PreviewView viewFinder;
    private boolean useFrontCamera = true;
    private ImageView imageView;
    private ImageCapture imageCapture;
    private File photoFile;
    private FirebaseStorage storage;

    private TextView jadwal, waktu, waktu_berjalan, waktu_capture;

    private Runnable runnable;
    private Handler handler = new Handler();
    MaterialButton backButton, captureButton, changeCameraButton, save, cancel, selectImage;

    String fileName;

    loadingDialog loading;
    UserPref userPref;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String timeCatch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minum_obat);

        viewFinder = findViewById(R.id.viewFinder);
        backButton = findViewById(R.id.backButton);
        captureButton = findViewById(R.id.image_capture_button);
        imageView = findViewById(R.id.overlay);
        waktu_capture = findViewById(R.id.tv_waktu_minum_obat);

        captureButton = findViewById(R.id.image_capture_button);
        selectImage = findViewById(R.id.choose_image);
        loading = new loadingDialog(this);
        userPref = new UserPref(this);
        loading.setMessage("Menyimpan data..");

        save = findViewById(R.id.save_button);
        cancel = findViewById(R.id.take_again);

        jadwal = findViewById(R.id.tv_jadwal);
        waktu = findViewById(R.id.tv_waktu);

        waktu_berjalan = findViewById(R.id.tv_waktu_berjalan);

        runnable = new Runnable() {
            @Override
            public void run() {
                waktu_berjalan.setText(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()));
                waktu_berjalan.postDelayed(this, 1000);
            }
        };

        selectImage.setOnClickListener(view -> {
          openImageChooser();
        });

        handler.post(runnable);

        Bundle extras = getIntent().getExtras();
        requestCameraPermission();

        if(extras != null){
            jadwal.setText(extras.getString("waktu"));
            waktu.setText(extras.getString("jam"));
        }

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

    @SuppressLint("IntentReset")
    private void openImageChooser() {
        @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
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

        String tgl_now = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis())+"_"+waktu.getText().toString();
        data.put("created_at_day", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis()));
        data.put("created_at_time", timeCatch);
        data.put("created_at", System.currentTimeMillis());

        cancelAlarm(jadwal.getText().toString());

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                File photoFile = new File(getRealPathFromURI(imageUri));
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return path;
    }

    private void cancelAlarm(String jadwal) {

        int requestCode = 0;
        switch (jadwal){
            case "pagi":
               requestCode = 1;
                break;
            case "siang":
                requestCode = 2;
                break;
            case "malam":
                requestCode = 3;
                break;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlaramReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


}