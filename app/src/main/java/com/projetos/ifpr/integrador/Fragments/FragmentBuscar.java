package com.projetos.ifpr.integrador.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Inicial;
import com.projetos.ifpr.integrador.R;
import com.projetos.ifpr.integrador.VisualizaDenuncia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
 * Created by jeremy on 30/03/2017.
 */

public class FragmentBuscar  extends AppCompatActivity implements View.OnClickListener  {

    private LinearLayout containerAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_denuncias);


        containerAddress = LinearLayout.class.cast(findViewById(R.id.containerAddress));

        SharedPreferences pref = getApplicationContext().getSharedPreferences("idUsuario", 0);
        final int idUsuario = pref.getInt("idUsuario", 0);

        ChamadaWeb chamada = new ChamadaWeb("http://"+
                ConfiguracaoServidor.retornarEnderecoServidor(this)
                +":8090/IntegradorWS/rest/servicos/consultaDenuncias",String.valueOf(idUsuario));

        chamada.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void atualizaMensagem(String resultado)
    {
        JSONArray rst = null;
        JSONObject jsonO = null;
        try {

            rst = new JSONArray(resultado);

            for(int i=0; i<rst.length(); i++){
                jsonO = rst.getJSONObject(i);

                final LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                final TextView txtView = new TextView(linearLayout.getContext());
                final Button btn = new Button(linearLayout.getContext());


                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                txtView.setText(jsonO.getString("descricao"));
                txtView.setTextSize(20);
                txtView.setTextColor(Color.parseColor("#000000"));
                btn.setId(Integer.parseInt(jsonO.getString("id")));
                btn.setOnClickListener(this);
                //Seta os parametros
                txtView.setLayoutParams(new LinearLayout.LayoutParams(350, LinearLayout.LayoutParams.WRAP_CONTENT));
                btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                btn.setBackgroundResource(R.drawable.ic_trending_flat_black_24dp);
                //adiciona no containerAddress
                linearLayout.addView(txtView);
                linearLayout.addView(btn);
                containerAddress.addView(linearLayout);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onBackPressed() {
        Intent intent = new Intent(this, Inicial.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("denuncia", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("idDenuncia", v.getId());
        editor.commit();


        Intent intent = new Intent(this, VisualizaDenuncia.class);
        finish();
        startActivity(intent);
    }

    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb;
        private String idUsuario;


        public ChamadaWeb(String endereco, String id) {
            enderecoWeb = endereco;
            idUsuario = id;
        }

        /*
            0-usuario
            1-mensagem
         */


        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                HttpPost chamada = new HttpPost(enderecoWeb);
                List<NameValuePair> parametros = new ArrayList<NameValuePair>(1);
                parametros.add(new BasicNameValuePair("id", idUsuario));


                System.out.println(String.valueOf(idUsuario));

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
                atualizaMensagem(resultado);
            }
        }
    }
}
