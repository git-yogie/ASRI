package com.yogie.anemiaapps.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.menu.minumObat_Activity;

public class pilih_waktu_input extends BottomSheetDialogFragment {


    TextView tanggal_dipilih;
    String tanggal;

    Button btn_pagi, btn_siang, btn_malam;
    Context context;
    public pilih_waktu_input(Context context, String tanggal) {
        // Required empty public constructor
        this.context = context;
        this.tanggal = tanggal;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pilih_waktu_input, container, false);
        tanggal_dipilih = view.findViewById(R.id.tv_tanggal_dipilihh);
        tanggal_dipilih.setText(tanggal);

        btn_pagi = view.findViewById(R.id.btn_ambil_obat_1);
        btn_siang = view.findViewById(R.id.btn_ambil_obat_2);
        btn_malam = view.findViewById(R.id.btn_ambil_obat_3);

        btn_pagi.setOnClickListener(v -> camera(v,tanggal,"pagi","09:00"));
        btn_siang.setOnClickListener(v -> camera(v, tanggal,"siang","15:00"));
        btn_malam.setOnClickListener(v -> camera(v,tanggal,"malam","21:00"));

        // Inflate the layout for this fragment
        return view;
    }

    private void camera(View v, String tanggal, String waktu, String jam){
        Intent intent = new Intent(context, activity_input_manual.class);
        intent.putExtra("tanggal",tanggal);
        intent.putExtra("waktu", waktu);
        intent.putExtra("jam", jam);

        context.startActivity(intent);
    }
}