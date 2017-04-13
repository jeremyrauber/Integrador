package com.projetos.ifpr.integrador.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.projetos.ifpr.integrador.Consulta;
import com.projetos.ifpr.integrador.Inicial;
import com.projetos.ifpr.integrador.MainActivity;
import com.projetos.ifpr.integrador.PermissionUtils;
import com.projetos.ifpr.integrador.R;
import com.projetos.ifpr.integrador.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by jeremy on 13/04/2017.
 */

public class FragmentMapa extends FragmentActivity  implements OnMapReadyCallback{

    /******** CONFIGURACOES PERSONALIZADAS PARA O HEATMAP ********/

    /* Convolução radial alternativa pro raio*/
    private static final int ALT_HEATMAP_RADIUS = 12;

    /* Opacidade alternativa para a camada de heatmap */
    private static final double ALT_HEATMAP_OPACITY = 0.5;

    /* Troca de cor do gradiente para o heatmap (azul -> vermelho) */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);


    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;
    private String pontosFromWebService;
    private boolean variavelControleInternet;
    LatLng myPosition;


    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private HashMap<String, DataSet> mLists = new HashMap<String, DataSet>();

    protected Location mLastLocation;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heatmaps_demo);

        ChamadaWeb chamada = new ChamadaWeb("http://10.0.2.2:8090/IntegradorWS/rest/servicos/consultaMapaCalor");
        chamada.execute();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Inicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    protected void startDemo() {

        LatLng FozdoIguacu = new LatLng(-25.5469,-54.5882);

        mMap.addMarker(new MarkerOptions().position(FozdoIguacu).title("Marcador em Foz do Iguaçu"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FozdoIguacu, 10));


        // Set up the spinner/dropdown list
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        try {
            mLists.put(getString(R.string.foco), new DataSet(readItems(R.raw.police)));

        } catch (JSONException e) {
            Toast.makeText(this, "Problema lendo a lista de localização.", Toast.LENGTH_LONG).show();
        }



    }

    public void changeRadius(View view) {
        if (mDefaultRadius) {
            mProvider.setRadius(ALT_HEATMAP_RADIUS);
        } else {
            mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
        }
        mOverlay.clearTileCache();
        mDefaultRadius = !mDefaultRadius;
    }

    public void changeGradient(View view) {
        if (mDefaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
        } else {
            mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT);
        }
        mOverlay.clearTileCache();
        mDefaultGradient = !mDefaultGradient;
    }

    public void changeOpacity(View view) {
        if (mDefaultOpacity) {
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
        } else {
            mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
        }
        mOverlay.clearTileCache();
        mDefaultOpacity = !mDefaultOpacity;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mMap != null) {
            return;
        }
        mMap = map;
        startDemo();
            }



    // Dealing with spinner choices
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();

            if(variavelControleInternet) {
                // Check if need to instantiate (avoid setData etc twice)
                if (mProvider == null) {
                    mProvider = new HeatmapTileProvider.Builder().data(
                            mLists.get(getString(R.string.foco)).getData()).build();
                    mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    // Render links
                } else {
                    mProvider.setData(mLists.get(dataset).getData());
                    mOverlay.clearTileCache();
                }
            }

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        //InputStream inputStream = getResources().openRawResource(R.raw.police);

       // String json = new Scanner(inputStream).useDelimiter("\\A").next();

        JSONArray array = new JSONArray(pontosFromWebService);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    private class DataSet {
        private ArrayList<LatLng> mDataset;

        public DataSet(ArrayList<LatLng> dataSet) {
            this.mDataset = dataSet;
        }

        public ArrayList<LatLng> getData() {
            return mDataset;
        }

    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }


    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb;
        private String idUsuario;

        public ChamadaWeb(String endereco) {
            enderecoWeb = endereco;
        }
        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                System.out.println(enderecoWeb);
                HttpGet chamada = new HttpGet(enderecoWeb);
                HttpResponse resposta = cliente.execute(chamada);
                return EntityUtils.toString(resposta.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(String resultado) {
            if (resultado != null) {
                pontosFromWebService = resultado;
                    System.out.println(resultado);
                    setUpMap();
                    variavelControleInternet = true;
                Toast.makeText(FragmentMapa.this, "Usando banco de dados online!", Toast.LENGTH_SHORT).show();
            }else{
                InputStream inputStream = getResources().openRawResource(R.raw.police);
                pontosFromWebService =new Scanner(inputStream).useDelimiter("\\A").next();

                variavelControleInternet = false;
                Toast.makeText(FragmentMapa.this, "Não foi possível conectar com o banco de dados, verifique sua conexão e tente novamente", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
