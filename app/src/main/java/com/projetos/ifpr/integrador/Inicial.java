package com.projetos.ifpr.integrador;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.projetos.ifpr.integrador.Fragments.FragmentBuscar;
import com.projetos.ifpr.integrador.Fragments.FragmentEditar;
import com.projetos.ifpr.integrador.Fragments.FragmentMapa;
import com.projetos.ifpr.integrador.Fragments.FragmentPreferencias;

public class Inicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int PERMISSIONS_REQUEST_PHONE_CALL = 100;
    private static String[] PERMISSIONS_PHONECALL = {Manifest.permission.CALL_PHONE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
    } else {
        super.onBackPressed();
    }
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
            fragmentClass = FragmentBuscar.class;
        } else if (id == R.id.nav_editar) {
            fragmentClass = FragmentEditar.class;
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
            String number = "91324567";
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
    // FIM DA DOIDERA PARA FAZER LIGACAUM
}
