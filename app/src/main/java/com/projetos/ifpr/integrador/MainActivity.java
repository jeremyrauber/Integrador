package com.projetos.ifpr.integrador;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    EditText login, senha;
    String IDusuario;
    public final static String EXTRA_MESSAGE = "com.example.crash.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (EditText) findViewById(R.id.login);
        senha = (EditText) findViewById(R.id.senha);

        ImageView iv = (ImageView)findViewById(R.id.imageView1);
        iv.setImageResource(R.drawable.imagem2);

        Button btnLogar = (Button) findViewById(R.id.btnLogar);
        Button btnCadastrar = (Button) findViewById(R.id.btnCadastrar);

        //"http://10.0.2.2:8090/IntegradorWS/rest/servicos/login"


        btnLogar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChamadaWeb chamada = new ChamadaWeb("http://"+
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("ENDERECOSERVIDOR", "10.0.0.2")
                            + ":8090/IntegradorWS/rest/servicos/login",
                            "", login.getText().toString(), senha.getText().toString(), 2);
                    System.out.println("http://"+
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("ENDERECOSERVIDOR", "10.0.0.2")
                            + ":8090/IntegradorWS/rest/servicos/login");
                    chamada.execute();
                }
        });

        btnCadastrar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent  i = new Intent(getApplicationContext(),Cadastro.class);
                startActivity(i);
            }
        });

    }
//teste
    public void retornaMensagem(String resultado){

        System.out.println(resultado);
        if(resultado.contains("logado")){

            String array[] = new String[2];
            array = resultado.split(";");
            String IDusuario = array[1];

            Toast.makeText(this.getBaseContext(), "Usuario Logado! id:"+array[1], Toast.LENGTH_SHORT).show();

            Intent  i = new Intent(getApplicationContext(),Inicial.class);
            i.putExtra(EXTRA_MESSAGE, IDusuario);
            startActivity(i);

        }
        else{
            Toast.makeText(this.getBaseContext(), "Usuário ou senha não conferem! Tente Novamente", Toast.LENGTH_SHORT).show();
        }
    }

    private class ChamadaWeb extends AsyncTask<String, Void, String>{
        private String enderecoWeb;
        private Usuario usuario;
        private int tipoChamada;  //1 - GET 2 - POST


        public  ChamadaWeb(String endereco, String nome, String login, String senha, int tipo){

            this.usuario = new Usuario();

            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setSenha(senha);
            enderecoWeb = endereco;
            tipoChamada = tipo;

        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                if(tipoChamada == 1){
                    HttpGet chamada = new HttpGet(enderecoWeb);
                    HttpResponse resposta = cliente.execute(chamada);
                    return EntityUtils.toString(resposta.getEntity());

                }else if(tipoChamada == 2){

                        HttpPost chamada = new HttpPost(enderecoWeb);
                        List<NameValuePair> parametros = new ArrayList<NameValuePair>(2); //o 3 eh referente ao numero de params

                        parametros.add(new BasicNameValuePair("login", usuario.getLogin()));
                        parametros.add(new BasicNameValuePair("senha", usuario.getSenha()));

                        chamada.setEntity(new UrlEncodedFormEntity(parametros));
                        HttpResponse resposta = cliente.execute(chamada);
                        System.out.println(resposta);
                        String responseBody = EntityUtils.toString(resposta.getEntity()); // eh a resposta da servlet
                        return responseBody;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(String resultado){
            if(resultado != null){
                retornaMensagem(resultado);
            }
        }
    }
    public void abrirEndereco(View view){
        Intent  i = new Intent(getApplicationContext(),ConfiguracaoServidor.class);
        startActivity(i);
    }


}
