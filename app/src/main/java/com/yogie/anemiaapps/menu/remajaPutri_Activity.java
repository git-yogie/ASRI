package com.yogie.anemiaapps.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.UserPref;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.yogie.anemiaapps.helper.loadingDialog;

public class remajaPutri_Activity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    MaterialButton button_simpan;

    RadioGroup radioGroup,haidRutin,nyeri_haid,frekuensi_makan;

    TextInputLayout nama,usia,jurusan;
    TextInputEditText nama_editText,usia_editText;


    /*
    pola haid
     */
    TextInputLayout usia_pertamaHaid, jumlah_pembalut,riwayat_penyakit;
    TextInputEditText usia_pertamaHaid_editText, jumlah_pembalut_editext,riwayat_penyakit_editText;

    /* Pola Makan */
    TextInputLayout jenis_makanan,buah,sayur;
    TextInputEditText jenis_makanan_editText,buah_editText,sayur_editText;
    UserPref userData;

    FirebaseFirestore db;

    loadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remaja_putri);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        loadingDialog = new loadingDialog(this);
        loadingDialog.setMessage("Menyimpan data...");


        radioGroup = findViewById(R.id.radioGroup);
        haidRutin = findViewById(R.id.haid_rutin);
        nyeri_haid = findViewById(R.id.nyeri_haid);
        frekuensi_makan = findViewById(R.id.frekuensi_makan);


        nama = findViewById(R.id.nama_textField);
        usia = findViewById(R.id.usia_textField);
        jurusan = findViewById(R.id.jurusan_textField);
        usia_pertamaHaid = findViewById(R.id.usia_pertamaHaid_textField);
        jumlah_pembalut = findViewById(R.id.jumlah_pembalut_textField);
        riwayat_penyakit = findViewById(R.id.riwayat_penyakit_textField);
        jenis_makanan = findViewById(R.id.jenis_makanan_textField);
        buah = findViewById(R.id.buah_textField);
        sayur = findViewById(R.id.sayur_textField);

        nama_editText = findViewById(R.id.nama_editText);
        usia_editText = findViewById(R.id.usia_editText);
        usia_pertamaHaid_editText = findViewById(R.id.usia_pertamaHaid_editText);
        jumlah_pembalut_editext = findViewById(R.id.jumlah_pembalut_editText);
        riwayat_penyakit_editText = findViewById(R.id.riwayat_penyakit_editText);
        jenis_makanan_editText = findViewById(R.id.jenis_makanan_editText);
        buah_editText = findViewById(R.id.buah_editText);
        sayur_editText = findViewById(R.id.sayur_editText);



        userData = new UserPref(this);
        if(userData.isLogin()){
            nama_editText.setText(userData.getUserData().get("nama").toString());
            usia_editText.setText(userData.getUserData().get("usia").toString());
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Remaja Putri");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        autoCompleteTextView  = findViewById(R.id.jurusan_editText);
        radioGroup.setOnCheckedChangeListener(this::checkedKelas);

        button_simpan = findViewById(R.id.simpan_button);
        button_simpan.setOnClickListener(this::simpan);

        getAndReplace();
    }

    public void simpan(View view){
        loadingDialog.show();
        String nama = nama_editText.getText().toString();
        String usia = usia_editText.getText().toString();
        String jurusan = autoCompleteTextView.getText().toString();
        String kelas = radioGroup.getCheckedRadioButtonId() == R.id.kelasX ? "X" : "XI";
        String haidRutin_str = haidRutin.getCheckedRadioButtonId() == R.id.ya ? "ya" : "tidak";
        String nyeri_haid_str = nyeri_haid.getCheckedRadioButtonId() == R.id.ya_nyeri ? "ya" : "tidak";
        String frekuensi_makan_str = "0";
        String jumlahPembalut = jumlah_pembalut_editext.getText().toString();
        String riwayatPenyakit = riwayat_penyakit_editText.getText().toString();

        if(frekuensi_makan.getCheckedRadioButtonId()!=-1){
            RadioButton frekuensiMakan = findViewById(frekuensi_makan.getCheckedRadioButtonId());
           frekuensi_makan_str = frekuensiMakan.getText().toString();
        }


        String sayurTidakSuka = sayur_editText.getText().toString();
        String buahTidakSuka = buah_editText.getText().toString();
        String jenisMakanan = jenis_makanan_editText.getText().toString();

        if(nama.isEmpty()){
            this.nama.setError("Nama tidak boleh kosong");
        }
        if(usia.isEmpty()){
            this.usia.setError("Usia tidak boleh kosong");
        }
        if(jurusan.isEmpty()){
            this.jurusan.setError("Jurusan tidak boleh kosong");
        }

        if(radioGroup.getCheckedRadioButtonId() == -1){
            Snackbar.make(view, "Kelas tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
        }




        if(!nama.isEmpty() && !usia.isEmpty() && !jurusan.isEmpty() && radioGroup.getCheckedRadioButtonId() != -1){
            this.nama.setError(null);
            this.usia.setError(null);
            this.jurusan.setError(null);

            Map<String,Object> data = new HashMap<>();
            data.put("nama", nama);
            data.put("usia", usia);
            data.put("jurusan", jurusan);
            data.put("kelas", kelas);
            data.put("usia_pertama_haid", usia_pertamaHaid_editText.getText().toString());
            data.put("haid_rutin", haidRutin_str);
            data.put("nyeri_haid", nyeri_haid_str);
            data.put("jumlah_pembalut", jumlahPembalut);
            data.put("riwayat_penyakit", riwayatPenyakit);
            data.put("frekuensi_makan", frekuensi_makan_str);
            data.put("jenis_makanan", jenisMakanan);
            data.put("buah_tidak_suka", buahTidakSuka);
            data.put("sayur_tidak_suka", sayurTidakSuka);

            db.collection("remaja_putri").document(userData.getUserData().get("documentId").toString()).set(data, SetOptions.merge()).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    loadingDialog.dismiss();
                    Snackbar.make(view, "Data berhasil disimpan", Snackbar.LENGTH_SHORT).show();
                } else {
                    loadingDialog.dismiss();
                    Snackbar.make(view, "Data gagal disimpan", Snackbar.LENGTH_SHORT).show();
                    Log.d("error", "simpan: "+ Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }

    }

    public void getAndReplace(){
        db.collection("remaja_putri").document(userData.getUserData().get("documentId").toString()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Map<String,Object> data = task.getResult().getData();
                if(data != null){
                    nama_editText.setText(Objects.requireNonNull(data.get("nama")).toString());
                    usia_editText.setText(Objects.requireNonNull(data.get("usia")).toString());
                    autoCompleteTextView.setText(Objects.requireNonNull(data.get("jurusan")).toString());
                    usia_pertamaHaid_editText.setText(Objects.requireNonNull(data.get("usia_pertama_haid")).toString());

                    if(Objects.requireNonNull(data.get("kelas")).toString().equals("X")){
                        radioGroup.check(R.id.kelasX);
                    } else {
                        radioGroup.check(R.id.KelasXI);
                    }
                    if(Objects.requireNonNull(data.get("haid_rutin")).toString().equals("ya")){
                        haidRutin.check(R.id.ya);
                    } else {
                        haidRutin.check(R.id.tidak);
                    }
                    if(Objects.requireNonNull(data.get("nyeri_haid")).toString().equals("ya")){
                        nyeri_haid.check(R.id.ya_nyeri);
                    } else {
                        nyeri_haid.check(R.id.tidak_nyeri);
                    }
                    if(Objects.requireNonNull(data.get("frekuensi_makan")).toString().equals("1")){
                        frekuensi_makan.check(R.id.makan_1);
                    } else if(Objects.requireNonNull(data.get("frekuensi_makan")).toString().equals("2")){
                        frekuensi_makan.check(R.id.makan_2);
                    } else if(Objects.requireNonNull(data.get("frekuensi_makan")).toString().equals("3")){
                        frekuensi_makan.check(R.id.makan_3);
                    } else if(Objects.requireNonNull(data.get("frekuensi_makan")).toString().equals("4")){
                        frekuensi_makan.check(R.id.makan_4);
                    }else if(Objects.requireNonNull(data.get("frekuensi_makan")).toString().equals("5")){
                        frekuensi_makan.check(R.id.makan_5);
                    }
                    jenis_makanan_editText.setText(Objects.requireNonNull(data.get("jenis_makanan")).toString());
                    buah_editText.setText(Objects.requireNonNull(data.get("buah_tidak_suka")).toString());
                    sayur_editText.setText(Objects.requireNonNull(data.get("sayur_tidak_suka")).toString());
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishAfterTransition();
        return true;
    }

    public void checkedKelas(RadioGroup group, int checkedId){
        if(checkedId == R.id.kelasX){
            String[] kelas = {"teknik kimia industri","rekayasa perangkat lunak 1","rekayasa perangkat lunak 2 ","usaha layanan wisata","perhotelan","tata kecantikan kulit dan rambut","desain dan produk busana","akuntansi 1 ","akuntansi 2"};
            setJurusan(kelas);

        } else if(checkedId == R.id.KelasXI){
            String[] kelas = {"kimia industri","rekayasa perangkat lunak 1","rekayasa perangkat lunak 2","usaha perjalanan wisata","perhotelan","tata kecantikan kulit dan rambut","tata busana","akuntansi dan keuangan lembaga 1","akuntansi dan keuangan lembaga 2"};
            setJurusan(kelas);
        }
    }

    public void setJurusan (String[] kelas){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kelas);
        autoCompleteTextView.setAdapter(adapter);
    }

}