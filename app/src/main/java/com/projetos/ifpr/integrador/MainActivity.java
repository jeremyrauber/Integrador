package com.projetos.ifpr.integrador;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
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

public class MainActivity extends AppCompatActivity {
    EditText login, senha;
    String IDusuario;
    private Usuario usuario;
    ProgressDialog progress;

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

                   progress = ProgressDialog.show(MainActivity.this, "Aguarde...",
                            "Verificando suas credenciais", true);

                    ChamadaWeb chamada = new ChamadaWeb("http://"+
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("ENDERECOSERVIDOR", "10.0.0.2")
                            + ":8090/IntegradorWS/rest/servicos/login",
                            "", login.getText().toString(), senha.getText().toString(), 2);
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
        try {
            JSONObject rsp = new JSONObject(resultado);

            if(rsp.getBoolean("resposta")){
                System.out.println(">>>>>>>>>>>>>>>>>>>>"+ rsp.getString("idUsuario"));


                //Adiciona idUsuario como preferencia, podendo ser acesso de qualquer lugar desse mundao Androidiano
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("idUsuario", rsp.getString("idUsuario")).commit();

                ChamadaWeb2 chamada = new ChamadaWeb2("http://"+
                        ConfiguracaoServidor.retornarEnderecoServidor(this)
                        +":8090/IntegradorWS/rest/servicos/trazum",Integer.parseInt(rsp.getString("idUsuario")),2);
                chamada.execute();


                // Nao sei o que faz?!
                SharedPreferences pref = getApplicationContext().getSharedPreferences("idUsuario", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("idUsuario", Integer.parseInt(rsp.getString("idUsuario")));
                editor.commit();

            }
            else{
                Toast.makeText(this.getBaseContext(), "Usuário ou senha não conferem! Tente Novamente.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                        List<NameValuePair> parametros = new ArrayList<NameValuePair>(2); //o 2 eh referente ao numero de params

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
            progress.dismiss();
            if(resultado != null){
                retornaMensagem(resultado);
            }else{
                Toast.makeText(MainActivity.this, "Você não está conectado! Verifique sua conexão com a internet", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void abrirEndereco(View view){
        Intent  i = new Intent(getApplicationContext(),ConfiguracaoServidor.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private class ChamadaWeb2 extends AsyncTask<String, Void, String> {
        private String enderecoWeb;
        private int idUsuario;
        private int tipoChamada;  //1 - GET 2 - POST


        public  ChamadaWeb2(String endereco,int id, int tipo){

            enderecoWeb = endereco;
            idUsuario = id;
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
                    List<NameValuePair> parametros = new ArrayList<NameValuePair>(1); //o 2 eh referente ao numero de params

                    parametros.add(new BasicNameValuePair("id", String.valueOf(idUsuario)));

                    chamada.setEntity(new UrlEncodedFormEntity(parametros));
                    HttpResponse resposta = cliente.execute(chamada);
                    String responseBody = EntityUtils.toString(resposta.getEntity()); // eh a resposta da servlet
                    return responseBody;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(String resultado){

            if(resultado!=null) {
                Gson gson = new Gson();
                System.out.println("----------------------------" + resultado);
                usuario = gson.fromJson(resultado, Usuario.class);
                System.out.println(usuario.getLikes().toString()+"-"+usuario.getDislikes().toString()+"-"+
                        usuario.getNome().toString()+"-"+usuario.getTelefone().toString());


                SharedPreferences pref = getApplicationContext().getSharedPreferences("usuario", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("nome", usuario.getNome().toString() );
                editor.putString("cel", usuario.getTelefone().toString() );
                editor.putString("likes", usuario.getLikes().toString() );
                editor.putString("dislikes", usuario.getDislikes().toString() );
                editor.commit();

                Intent  i = new Intent(getBaseContext(),Inicial.class);
                i.putExtra("EXTRA_SESSION_ID", usuario.getId());
                startActivity(i);
            }
        }
    }




}
