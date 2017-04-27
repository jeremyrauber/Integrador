package com.projetos.ifpr.integrador;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by Crash on 28/02/2017.
 */

public class Consulta extends AppCompatActivity {
    TextView textoRetorno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta);
        //textoRetorno = (TextView) findViewById(R.id.textoConsulta);

        Intent intent = getIntent();
        String idUsuario = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        ChamadaWeb chamada = new ChamadaWeb("http://"+
                ConfiguracaoServidor.retornarEnderecoServidor(this)
                +":8090/IntegradorWS/rest/servicos/consulta",idUsuario);
        chamada.execute();

        //Button btnInicial = (Button) findViewById(R.id.btnInicial);
       /* btnInicial.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent  i = new Intent(getApplicationContext(),Inicial.class);
                startActivity(i);
            }
        });*/

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
                if (!enderecoWeb.endsWith("?"))
                    enderecoWeb += "?";

                List<NameValuePair> parameters = new LinkedList<NameValuePair>();

                if (idUsuario != null)
                    parameters.add(new BasicNameValuePair("id", idUsuario));

                String paramString = URLEncodedUtils.format(parameters, "utf-8");

                enderecoWeb += paramString;

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
                atualizaMensagem(resultado);
            }
        }
    }
    public void atualizaMensagem(String resultado) {
        textoRetorno.setText(resultado);
    }
}

