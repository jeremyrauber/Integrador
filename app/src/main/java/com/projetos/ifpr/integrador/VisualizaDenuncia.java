package com.projetos.ifpr.integrador;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.projetos.ifpr.integrador.Fragments.FragmentBuscar;
import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Helper.GPSTracker;
import com.projetos.ifpr.integrador.Helper.PermissionUtils;
import com.projetos.ifpr.integrador.Model.Denuncia;

import org.json.JSONException;
import org.json.JSONObject;

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
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;


/**
 * Created by Crash on 16/04/2017.
 */

public class VisualizaDenuncia extends FragmentActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {


    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private ImageView imageView;
    private Button btnExcluir;
    private Button btnAdicionar;
    private TextView textInfo;
    private EditText edtDescricao;
    private String img_str;
    private Denuncia d;
    private byte[] image;
    private Bitmap fotoquevaiproupload;

//http://stackoverflow.com/questions/42330052/the-photo-lose-its-quality-when-it-appears-into-the-imageview


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.visualiza_denuncia);

        imageView = (ImageView) findViewById(R.id.fotoThumb);
        textInfo = (TextView) findViewById(R.id.txtDescricao);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("denuncia", 0);
        final int idDenuncia = pref.getInt("idDenuncia", 0);

        ChamadaWeb chamada = new ChamadaWeb("http://"+
                ConfiguracaoServidor.retornarEnderecoServidor(VisualizaDenuncia.this)
                +":8090/IntegradorWS/rest/servicos/pegaDenuncia", idDenuncia);

        chamada.execute();



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void onBackPressed() {
        Intent intent = new Intent(this, FragmentBuscar.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        SharedPreferences pref = getApplicationContext().getSharedPreferences("denuncia", 0);
        final double lat = Double.parseDouble(pref.getString("lat", "0"));
        final double log = Double.parseDouble(pref.getString("log", "0"));

        System.out.println(lat+"/"+log);

        LatLng FozdoIguacu = new LatLng(lat, log);
        //mMap.setOnMyLocationButtonClickListener(this);
        mMap.getMaxZoomLevel();
        mMap.addMarker(new MarkerOptions().position(FozdoIguacu)
                .title("Localização da denúncia"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FozdoIguacu,14));

    }

    public void atualizaMensagem(String resultado)
    {
        JSONObject rsp = null;
        try {
            rsp = new JSONObject(resultado);

            SharedPreferences pref = getApplicationContext().getSharedPreferences("denuncia", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("lat", rsp.getString("lat"));
            editor.putString("log", rsp.getString("log"));
            editor.commit();

            textInfo.setText(rsp.getString("descricao"));
            byte[] decodedString = Base64.decode(rsp.getString("foto"), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //************************ chamada web
    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb;
        private int idDenuncia;


        public ChamadaWeb(String endereco, int idDenuncia) {
            this.enderecoWeb = endereco;
            this.idDenuncia = idDenuncia;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                HttpPost chamada = new HttpPost(enderecoWeb);
                List<NameValuePair> parametros = new ArrayList<NameValuePair>(1);
                parametros.add(new BasicNameValuePair("id", String.valueOf(idDenuncia)));


                chamada.setEntity(new UrlEncodedFormEntity(parametros));
                HttpResponse resposta = cliente.execute(chamada);

                String responseBody = EntityUtils.toString(resposta.getEntity());
                return responseBody;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(String resultado)
        {
            if(resultado != null){
                System.out.println(resultado);
                atualizaMensagem(resultado);
            }
        }
    }
}