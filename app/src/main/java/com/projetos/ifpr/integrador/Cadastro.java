package com.projetos.ifpr.integrador;


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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pinball83.maskededittext.MaskedEditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.projetos.ifpr.integrador.Helper.ConfiguracaoServidor;
import com.projetos.ifpr.integrador.Helper.GPSTracker;
import com.projetos.ifpr.integrador.Helper.PermissionUtils;
import com.projetos.ifpr.integrador.Model.Denuncia;
import com.projetos.ifpr.integrador.Model.Usuario;

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
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

//testando pushgfgfgbfgbfbgfgbf

public class Cadastro extends AppCompatActivity {
    EditText nome, login, senha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro);


        nome = (EditText)findViewById(R.id.nome);
        login = (EditText)findViewById(R.id.login);
        senha = (EditText)findViewById(R.id.senha);

        final MaskedEditText maskedEditText = (MaskedEditText) this.findViewById(R.id.masked_edit_text);

        System.out.println(maskedEditText.getText());


        Button btnGravar = (Button)findViewById(R.id.salvar);
        btnGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               System.out.println( nome.getText().toString()+ " | "+ login.getText().toString()+ " | "+ senha.getText().toString());

                ChamadaWeb chamada = new ChamadaWeb("http://"+
                        ConfiguracaoServidor.retornarEnderecoServidor(Cadastro.this)
                        +":8090/IntegradorWS/rest/servicos/cadastro", nome.getText().toString(), login.getText().toString(),
                        senha.getText().toString(), maskedEditText.getUnmaskedText().toString(),2);
                chamada.execute();
            }
        });
    }

    public void atualizaMensagem(String resultado)
    {
        JSONObject rst = null;
        try {
            rst = new JSONObject(resultado);
            if(rst.getBoolean("resposta")){
                Toast.makeText(this.getBaseContext(), "Cadastro realizado com sucesso!!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(this.getBaseContext(), "Cadastro com problema, tente novamente", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ChamadaWeb extends AsyncTask<String, Void, String> {
        private String enderecoWeb;
        private Usuario usuario;
        private int tipoChamada; //1 - GET 2 - POST


        public  ChamadaWeb(String endereco, String nome, String login, String senha, String telefone, int tipo){

            this.usuario = new Usuario();

            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setSenha(senha);
            enderecoWeb = endereco;
            usuario.setTelefone(telefone);
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
                    parametros.add(new BasicNameValuePair("telefone", usuario.getTelefone()));

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