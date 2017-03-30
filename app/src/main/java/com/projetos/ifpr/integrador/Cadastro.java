package com.projetos.ifpr.integrador;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

//testando pushgfgfgbfgbfbgfgbf

public class Cadastro extends AppCompatActivity {
    EditText nome, login, senha, er;


    //comentario by jeremy22
    //dlksajodijasdF
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro);


        nome = (EditText)findViewById(R.id.nome);
        login = (EditText)findViewById(R.id.login);
        senha = (EditText)findViewById(R.id.senha);


        Button btnGravar = (Button)findViewById(R.id.salvar);
        btnGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               System.out.println( nome.getText().toString()+ " | "+ login.getText().toString()+ " | "+ senha.getText().toString());

                ChamadaWeb chamada = new ChamadaWeb("http://10.0.2.2:8080/WebServiceProjetoIntegrador/rest/servicos/cadastro", nome.getText().toString(), login.getText().toString(), senha.getText().toString(),2);
                chamada.execute();
            }
        });
    }

    public void atualizaMensagem(String resultado)
    {
        System.out.println(resultado);
        if(resultado.equals("cadastrou") ){
            Toast.makeText(this.getBaseContext(), "Cadastro realizado com sucesso!!!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this.getBaseContext(), "Cadastro com problema, tente novamente", Toast.LENGTH_SHORT).show();
        }
    }

    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb;
        private Usuario usuario;
        private int tipoChamada; //1 - GET 2 - POST


        public  ChamadaWeb(String endereco, String nome, String login, String senha, int tipo){

            this.usuario = new Usuario();

            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setSenha(senha);
            enderecoWeb = endereco;
            tipoChamada = tipo;
        }

        /*
            0-usuario
            1-mensagem
         */



        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                if(tipoChamada == 1)
                {
                    HttpGet chamada = new HttpGet(enderecoWeb);
                    HttpResponse resposta = cliente.execute(chamada);
                    return EntityUtils.toString(resposta.getEntity());

                }else if(tipoChamada == 2)
                {
                    HttpPost chamada = new HttpPost(enderecoWeb);
                    List<NameValuePair> parametros = new ArrayList<NameValuePair>(3);
                    parametros.add(new BasicNameValuePair("nome", usuario.getNome()));
                    parametros.add(new BasicNameValuePair("login", usuario.getLogin()));
                    parametros.add(new BasicNameValuePair("senha", usuario.getSenha()));

                    chamada.setEntity(new UrlEncodedFormEntity(parametros));
                    HttpResponse resposta = cliente.execute(chamada);

                    String responseBody = EntityUtils.toString(resposta.getEntity());
                    return responseBody;
                }
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