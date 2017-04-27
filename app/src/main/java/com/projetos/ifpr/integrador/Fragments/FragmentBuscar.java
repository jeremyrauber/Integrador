package com.projetos.ifpr.integrador.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.projetos.ifpr.integrador.Consulta;
import com.projetos.ifpr.integrador.Editar;
import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Inicial;
import com.projetos.ifpr.integrador.MainActivity;
import com.projetos.ifpr.integrador.R;
import com.projetos.ifpr.integrador.VisualizaDenuncia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

import static com.projetos.ifpr.integrador.R.mipmap.ic_launcher;

/**
 * Created by jeremy on 30/03/2017.
 */

public class FragmentBuscar  extends FragmentActivity implements View.OnClickListener  {

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
                txtView.setText(jsonO.getString("id")+jsonO.getString("descricao"));
                txtView.setTextSize(20);
                txtView.setTextColor(Color.parseColor("#000000"));
                btn.setId(Integer.parseInt(jsonO.getString("id")));
                btn.setOnClickListener(this);
                //Seta os parametros
                txtView.setLayoutParams(new LinearLayout.LayoutParams(350, LinearLayout.LayoutParams.WRAP_CONTENT));
                btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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


    private void addEditText() {
        //Criamos o EditText
        final LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        final TextView txtView = new TextView(linearLayout.getContext());
        final Button btn = new Button(linearLayout.getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        txtView.setText("Vizinho pau no toba!!!");
        txtView.setTextSize(20);
        txtView.setTextColor(Color.parseColor("#000000"));
        btn.setId(R.id.selectionDetails);
        btn.setText("Abrir");
        //Seta os parametros
        txtView.setLayoutParams(new LinearLayout.LayoutParams(350, LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //adiciona no containerAddress
        linearLayout.addView(txtView);
        linearLayout.addView(btn);
        containerAddress.addView(linearLayout);
    }
}
