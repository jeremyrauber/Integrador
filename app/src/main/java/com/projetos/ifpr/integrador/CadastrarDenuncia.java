package com.projetos.ifpr.integrador;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

public class CadastrarDenuncia extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
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
    private ProgressDialog progress;

//http://stackoverflow.com/questions/42330052/the-photo-lose-its-quality-when-it-appears-into-the-imageview


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
        GPSTracker gps = new GPSTracker(this);

        LatLng FozdoIguacu = new LatLng(gps.getLatitude(),gps.getLongitude());
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        mMap.getMaxZoomLevel();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FozdoIguacu,14));
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

        Toast.makeText(this, "Verifique sua localização!", Toast.LENGTH_SHORT).show();

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

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    static final int REQUEST_TAKE_PHOTO = 1;
    public void carregarFoto(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the Fil
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.projetos.ifpr.integrador",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            setPic();


            btnExcluir.setVisibility(View.VISIBLE);
            btnAdicionar.setVisibility(View.INVISIBLE);
            textInfo.setText("Clique no X para excluir a foto.");

        }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        fotoquevaiproupload = bitmap;
        imageView.setImageBitmap(bitmap);
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

        progress = ProgressDialog.show(CadastrarDenuncia.this, "Aguarde...",
                "Enviando sua denúncia", true);

        realizarEtapas();
    }

    public void realizarEtapas(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("idUsuario", 0);
        int idUsuario=pref.getInt("idUsuario", 0);

        GPSTracker gps = new GPSTracker(this);
        d = new Denuncia();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        fotoquevaiproupload.compress(Bitmap.CompressFormat.PNG, 75, stream);
        image = stream.toByteArray();
        img_str = Base64.encodeToString(image, Base64.DEFAULT);

        System.out.println(img_str.length()+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        d.setFoto(img_str);
        d.setLongitude(gps.getLongitude());
        d.setLatitude(gps.getLatitude());
        d.setDescricao(edtDescricao.getText().toString());
        d.setIdUsuario(idUsuario);


        if ((d.getFoto() != null && d.getFoto() != "")
                && (d.getDescricao() != null && d.getDescricao() != "")
                && (d.getLatitude() != null && d.getLongitude() != null)) {

            ChamadaWeb chamada = new ChamadaWeb("http://" +
                    ConfiguracaoServidor.retornarEnderecoServidor(CadastrarDenuncia.this)
                    + ":8090/IntegradorWS/rest/servicos/uploadDenuncia");
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
            progress.dismiss();
        }
    }



    public void atualizaMensagem(String resultado)
    {
        JSONObject rsp = null;
        try {
            rsp = new JSONObject(resultado);
            System.out.print(rsp.toString());
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            progress.dismiss();
            if(rsp.getBoolean("resposta")){
                Toast.makeText(CadastrarDenuncia.this, "Upload efetuado com sucesso!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(CadastrarDenuncia.this, "Erro ao efetuar a denúncia!", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                List<NameValuePair> parametros = new ArrayList<NameValuePair>(4); //numeor de params
                parametros.add(new BasicNameValuePair("latitude", d.getLatitude().toString()));
                parametros.add(new BasicNameValuePair("longitude", d.getLongitude().toString()));
                parametros.add(new BasicNameValuePair("descricao", d.getDescricao()));
                parametros.add(new BasicNameValuePair("imgb64", d.getFoto()));
                parametros.add(new BasicNameValuePair("idUsuario", d.getIdUsuario().toString()));


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