package com.projetos.ifpr.integrador;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.pinball83.maskededittext.MaskedEditText;
import com.projetos.ifpr.integrador.Fragments.FragmentBuscar;
import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

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

public class Editar extends AppCompatActivity {
    EditText nome, login, senha, senha2;
    MaskedEditText maskedEditText = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro);


        nome = (EditText)findViewById(R.id.nome);
        login = (EditText)findViewById(R.id.login);
        senha = (EditText)findViewById(R.id.senha);
        senha2 = (EditText)findViewById(R.id.senha2);
        maskedEditText = (MaskedEditText) this.findViewById(R.id.masked_edit_text);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("idUsuario", 0);
        final int idUsuario = pref.getInt("idUsuario", 0);

        ChamadaWeb chamada = new ChamadaWeb("http://"+
                ConfiguracaoServidor.retornarEnderecoServidor(Editar.this)
                +":8090/IntegradorWS/rest/servicos/consulta","","","","", idUsuario,2);

        chamada.execute();


        Button btnGravar = (Button)findViewById(R.id.salvar);
        btnGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               System.out.println( nome.getText().toString()+ " | "+ login.getText().toString()+ " | "+ senha.getText().toString());

                ChamadaWeb chamada = new ChamadaWeb("http://"+
                        ConfiguracaoServidor.retornarEnderecoServidor(Editar.this)
                        +":8090/IntegradorWS/rest/servicos/editar", nome.getText().toString(), login.getText().toString(),
                        senha.getText().toString(),maskedEditText.getUnmaskedText().toString(),idUsuario,1);
                chamada.execute();
            }
        });
    }



    public void onBackPressed() {
        Intent intent = new Intent(this, Inicial.class);
        startActivity(intent);
        finish();
    }

    public void atualizaMensagem(String resultado)
    {
        JSONObject rst = null;
        try {
            rst = new JSONObject(resultado);
            if(rst.getBoolean("resposta")){
                if(rst.has("editou")){

                    Toast.makeText(this.getBaseContext(), "Edição realizado com sucesso!!!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Inicial.class);
                    startActivity(intent);

                }else{
                    nome.setText(rst.getString("nome"));
                    login.setText(rst.getString("login"));
                    senha.setText(rst.getString("senha"));
                    senha2.setText(rst.getString("senha"));
                    maskedEditText.setMaskedText(rst.getString("telefone"));
                }
            }else{
                Toast.makeText(this.getBaseContext(), "Edição com problema, tente novamente", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb, _nome, _senha, _telefone, _login;
        private int tipoChamada;
        private int idUsuario;

        public  ChamadaWeb(String endereco, String nome, String login, String senha, String telefone, int _idUsuario, int tipo){
            idUsuario = _idUsuario;
            _nome = nome;
            _senha = senha;
            _login = login;
            _telefone = telefone;
            enderecoWeb = endereco;
            tipoChamada = tipo;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient cliente = HttpClientBuilder.create().build();

            try {
                if(tipoChamada == 1)
                {
                    HttpPost chamada = new HttpPost(enderecoWeb);
                    List<NameValuePair> parametros = new ArrayList<NameValuePair>(1);
                    parametros.add(new BasicNameValuePair("nome", _nome));
                    parametros.add(new BasicNameValuePair("senha", _senha));
                    parametros.add(new BasicNameValuePair("login", _login));
                    parametros.add(new BasicNameValuePair("telefone", _telefone));
                    parametros.add(new BasicNameValuePair("id", String.valueOf(idUsuario)));

                    chamada.setEntity(new UrlEncodedFormEntity(parametros));
                    HttpResponse resposta = cliente.execute(chamada);

                    String responseBody = EntityUtils.toString(resposta.getEntity());
                    return responseBody;

                }else if(tipoChamada == 2)
                {
                    HttpPost chamada = new HttpPost(enderecoWeb);
                    List<NameValuePair> parametros = new ArrayList<NameValuePair>(1);
                    parametros.add(new BasicNameValuePair("id", String.valueOf(idUsuario)));


                    System.out.println(String.valueOf(idUsuario));

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