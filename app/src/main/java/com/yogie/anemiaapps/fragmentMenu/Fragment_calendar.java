package com.yogie.anemiaapps.fragmentMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.UserPref;
import com.yogie.anemiaapps.helper.bottomsheet_fragment;
import com.yogie.anemiaapps.menu.minumObat_Activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class Fragment_calendar extends Fragment {

    Context context;
    MaterialButton button1,button2,button3;
    TextView textView_status_1,textView_status_2,textView_status_3;
    TextView tanggal;
    CalendarView calendarView;

    UserPref userPref;
    HashMap<String, Object> user;

    bottomsheet_fragment bottomSheet;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference pagi,siang,malam;

    MaterialCardView cv_pagi,cv_siang,cv_malam;

    FloatingActionButton fabCalender;
    public Fragment_calendar(Context context) {
        // Required empty public constructor
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        cv_pagi = view.findViewById(R.id.pagi);
        cv_siang = view.findViewById(R.id.siang);
        cv_malam = view.findViewById(R.id.malam);

        fabCalender = view.findViewById(R.id.fabCalendar);

        fabCalender.setOnClickListener(v->{
            Intent intent = new Intent(context, com.yogie.anemiaapps.menu.calendar_activity.class);
            context.startActivity(intent);
        });

        tanggal = view.findViewById(R.id.tv_tanggal);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        String date = formatter.format(calendar.getTime());
        tanggal.setText(date);

        textView_status_1 = view.findViewById(R.id.tv_Status_1);
        textView_status_2 = view.findViewById(R.id.tv_Status_2);
        textView_status_3 = view.findViewById(R.id.tv_Status_3);


        button1 = view.findViewById(R.id.btn_ambil_obat_1);
        button2 = view.findViewById(R.id.btn_ambil_obat_2);
        button3 = view.findViewById(R.id.btn_ambil_obat_3);

        button1.setOnClickListener(v -> camera(v,"belum","pagi","09:00"));
        button2.setOnClickListener(v -> camera(v,"belum","siang","15:00"));
        button3.setOnClickListener(v -> camera(v,"belum","malam","21:00"));

        userPref = new UserPref(context);
        if (userPref.isLogin()){
           user = userPref.getUserData();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Toast.makeText(context, tanggal_hari_ini(), Toast.LENGTH_SHORT).show();
        pagi = db.collection("users").document(user.get("documentId").toString()).collection("minum_obat").document(tanggal_hari_ini()+"_09:00");
        siang = db.collection("users").document(user.get("documentId").toString()).collection("minum_obat").document(tanggal_hari_ini()+"_15:00");
        malam = db.collection("users").document(user.get("documentId").toString()).collection("minum_obat").document(tanggal_hari_ini()+"_21:00");

        pagi.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                DocumentSnapshot document = documentSnapshot;

                cv_pagi.setOnClickListener(v -> {
                    bottomSheet = new bottomsheet_fragment(document,user.get("documentId").toString(),"pagi");
                    bottomSheet.show(getChildFragmentManager(),"bottomSheet");
                });

                button1.setVisibility(View.GONE);
                textView_status_1.setVisibility(View.VISIBLE);
                textView_status_1.setText("Sudah diambil pada "+document.get("waktu"));
            }
        });

        siang.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){

                DocumentSnapshot document = documentSnapshot;
                cv_siang.setOnClickListener(v -> {
                    bottomSheet = new bottomsheet_fragment(document,user.get("documentId").toString(),"siang");
                    bottomSheet.show(getChildFragmentManager(),"bottomSheet");
                });
                button2.setVisibility(View.GONE);
                textView_status_2.setVisibility(View.VISIBLE);
                textView_status_2.setText("Sudah diambil pada "+document.get("waktu"));
            }
        });

        malam.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                DocumentSnapshot document = documentSnapshot;

                cv_malam.setOnClickListener(v -> {
                    bottomSheet = new bottomsheet_fragment(document,user.get("documentId").toString(),"malam");
                    bottomSheet.show(getChildFragmentManager(),"bottomSheet");
                });

                button3.setVisibility(View.GONE);
                textView_status_3.setVisibility(View.VISIBLE);
                textView_status_3.setText("Sudah diambil pada "+document.get("waktu"));
            }
        });
    }

    private void camera(View v, String status, String waktu, String jam){
        Intent intent = new Intent(context, minumObat_Activity.class);
        intent.putExtra("status", status);
        intent.putExtra("waktu", waktu);
        intent.putExtra("jam", jam);

        context.startActivity(intent);
    }

    private String tanggal_hari_ini(){
        return  new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis());
    }

}