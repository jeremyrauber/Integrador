package com.projetos.ifpr.integrador;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Helper.GPSTracker;
import com.projetos.ifpr.integrador.Model.Denuncia;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


/**
 * Created by Crash on 16/04/2017.
 */

public class CadastrarDenuncia extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private ImageView imageView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button btnExcluir;
    private Button btnAdicionar;
    private TextView textInfo;
    private EditText edtDescricao;
    private String img_str;
    private Denuncia d;
    private byte[] image;
    private String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.denuncia);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imageView = (ImageView) findViewById(R.id.fotoThumb);
        btnExcluir = (Button) findViewById(R.id.btnExcluir);
        textInfo = (TextView) findViewById(R.id.textoInfo);
        edtDescricao = (EditText) findViewById(R.id.edtDescricao);
        btnAdicionar = (Button) findViewById(R.id.btnAdicionar);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        mMap.getMaxZoomLevel();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {

        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    /*********** CODIGO DO ACESSO A CAMERA AND STUFS ***********/

    public void carregarFoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            // transforma foto em string
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //imageBitmap.compress(Bitmap.CompressFormat.PNG,100, stream);
            image = stream.toByteArray();
            img_str = Base64.encodeToString(image, Base64.DEFAULT);


            btnExcluir.setVisibility(View.VISIBLE);
            btnAdicionar.setVisibility(View.INVISIBLE);
            textInfo.setText("Clique no X para excluir a foto.");
        }
    }

    public void excluirFoto(View view) {
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.ic_photo_camera_black_24dp);
        imageView.setImageDrawable(drawable);
        btnExcluir.setVisibility(View.INVISIBLE);
        btnAdicionar.setVisibility(View.VISIBLE);
        textInfo.setText("Clique na câmera para adicionar foto.");
    }

    public void enviarDenuncia(View view) {

        GPSTracker gps = new GPSTracker(this);
        d = new Denuncia();
        d.setFoto(img_str);
        d.setLongitude(gps.getLongitude());
        d.setLatitude(gps.getLatitude());
        d.setDescricao(edtDescricao.getText().toString());
        d.setFotobyte(image);


        if ((d.getFoto() != null && d.getFoto() != "")
                && (d.getDescricao() != null && d.getDescricao() != "")
                && (d.getLatitude() != null && d.getLongitude() != null)) {

            ChamadaWeb chamada = new ChamadaWeb("http://" +
                    ConfiguracaoServidor.retornarEnderecoServidor(CadastrarDenuncia.this)
                    + ":8090/IntegradorWS/rest/servicos/enviararquivo2");
            chamada.execute();
        }
        String msg = "";
        if (d.getFoto() == null || d.getFoto().equals("")) {
            msg += " Adicione uma foto a denúncia! ";
        }
        if (d.getDescricao() == null || d.getDescricao().equals("")) {
            msg += " Adicione uma descrição a denúncia! ";
        }

        if (!msg.equals("")) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

    }


    //************************ chamada web
    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb;


        public ChamadaWeb(String endereco) {
            this.enderecoWeb = endereco;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                HttpPost chamada = new HttpPost(enderecoWeb);
                List<NameValuePair> parametros = new ArrayList<NameValuePair>(5); //numeor de params
                parametros.add(new BasicNameValuePair("latitude", d.getLatitude().toString()));
                parametros.add(new BasicNameValuePair("longitude", d.getLongitude().toString()));
                parametros.add(new BasicNameValuePair("descricao", d.getDescricao()));
                parametros.add(new BasicNameValuePair("imgb64", d.getFoto()));

                chamada.setEntity(new UrlEncodedFormEntity(parametros));
                HttpResponse resposta = cliente.execute(chamada);

                String responseBody = EntityUtils.toString(resposta.getEntity());
                return responseBody;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(String resultado) {
            if (resultado != null) {
                System.out.println(resultado);

                Intent intent = getIntent();
                finish();
                startActivity(intent);
                Toast.makeText(CadastrarDenuncia.this, resultado, Toast.LENGTH_SHORT).show();
            }
        }
    }

}