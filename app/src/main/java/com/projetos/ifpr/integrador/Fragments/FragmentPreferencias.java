package com.projetos.ifpr.integrador.Fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.projetos.ifpr.integrador.Inicial;
import com.projetos.ifpr.integrador.R;

/**
 * Created by jeremy on 30/03/2017.
 */

public class FragmentPreferencias extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Inicial.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Toast.makeText(this, PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_fonte", "") + "-" + key, Toast.LENGTH_SHORT).show();
        Integer tamanho = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_fonte", "12"));

        int themeID;

        switch (tamanho) {
            case 12:
                themeID = R.style.FontSizeSmall;
                System.out.println("12");
                break;
            case 14:
                themeID = R.style.FontSizeMedium;
                System.out.println("14");
                break;
            case 16:
                themeID = R.style.FontSizeLarge;
                System.out.println("16");
                break;
            case 18:
                themeID = R.style.FontSizeXLarge;
                System.out.println("18");
                break;
            case 20:
                themeID = R.style.FontSizeXXLarge;
                System.out.println("20");
                break;
            default:
                themeID = R.style.FontSizeSmall;
                break;
        }
        setTheme(themeID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }
}