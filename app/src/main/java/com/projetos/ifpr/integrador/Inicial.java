package com.projetos.ifpr.integrador;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.TextKeyListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.gson.Gson;
import com.projetos.ifpr.integrador.Fragments.FragmentBuscar;
import com.projetos.ifpr.integrador.Fragments.FragmentMapa;
import com.projetos.ifpr.integrador.Fragments.FragmentPreferencias;
import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

public class Inicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int PERMISSIONS_REQUEST_PHONE_CALL = 100;
    public final static String EXTRA_MESSAGE = "com.example.crash.MESSAGE";
    private Button btnAdd;
    private TextView likes;
    private TextView dislikes;
    private Integer id;

    private TextView nav_user;
    private TextView nav_cel;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = ProgressDialog.show(this, "Aguarde...","Verificando suas credenciais", true);

        id = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("idUsuario", ""));



        btnAdd = (Button) findViewById(R.id.btnAdd);
        likes = (TextView) findViewById(R.id.likes);
        dislikes = (TextView) findViewById(R.id.dislikes);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView =  navigationView.getHeaderView(0);
        nav_user = (TextView)hView.findViewById(R.id.nomeUsuario);
        nav_cel = (TextView)hView.findViewById(R.id.telefoneUsuario);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("usuario", 0);

        final double log = Double.parseDouble(pref.getString("log", "0"));

        likes.setText(pref.getString("likes", "0"));
        dislikes.setText(pref.getString("dislikes", "0"));
        nav_user.setText(pref.getString("nome", "0"));
        nav_cel.setText(pref.getString("cel", "0"));
        progress.dismiss();

    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

    }

    // ACOES NO MENUBAR LATERAL
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_buscar) {
            Intent intent = new Intent(this, FragmentBuscar.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_editar) {
            Intent intent = new Intent(this, Editar.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_preferencias) {
            Intent intent = new Intent(this, FragmentPreferencias.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_mapa) {
            Intent intent = new Intent(this, FragmentMapa.class);
            finish();
            startActivity(intent);

        } else if (id == R.id.nav_call) {
            // fragmentClass = FragmentChamadas.class; acho q nao precisa abrir novo fragment
            call();
        } else if (id == R.id.nav_logout) {
            SharedPreferences mySPrefs =PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove("usuario");
            editor.remove("idUsuario");
            editor.apply();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity();
            }
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(fragmentClass!=null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.flContent, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    // DOIDERA PARA FAZER LIGACAUM

    private void call() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_PHONE_CALL);
        } else {
            //Open call function
            String number = "35248848";
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_PHONE_CALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                call();
            } else {
                Toast.makeText(this, "Desculpe!!! Permissão Negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void abrirDenuncia(View view){
        Intent  i = new Intent(getApplicationContext(),CadastrarDenuncia.class);
        startActivity(i);
    }





}
