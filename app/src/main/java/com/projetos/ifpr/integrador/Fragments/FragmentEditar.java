package com.projetos.ifpr.integrador.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.projetos.ifpr.integrador.Inicial;
import com.projetos.ifpr.integrador.R;

/**
 * Created by jeremy on 30/03/2017.
 */

public class FragmentEditar extends PreferenceActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_editar);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Inicial.class);
        startActivity(intent);
        finish();
    }
}
