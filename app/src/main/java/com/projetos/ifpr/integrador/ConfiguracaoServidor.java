package com.projetos.ifpr.integrador;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Crash on 15/04/2017.
 */

public class ConfiguracaoServidor extends Activity {

    private EditText edtEndereco;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracao_servidor);

        edtEndereco = (EditText) findViewById(R.id.enderecoServidor);
        Toast.makeText(this, "Ligacoes: "+PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_ligacoes", Boolean.parseBoolean(null))+" - "+
                        " Rede: "+PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_rede",Boolean.parseBoolean(null))+" - "+
                        " Anonimo: "+PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_anonimo",Boolean.parseBoolean(null))+" - "+
                " Visuais: "+PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_not_visuais",Boolean.parseBoolean(null))+" - "+
                        " Sonoras: "+PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_not_sonoras",Boolean.parseBoolean(null))+" - "+
                        " Fonte: "+PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_fonte","")+" - "+
                " Endereco Servidor: "+  PreferenceManager.getDefaultSharedPreferences(this).getString("ENDERECOSERVIDOR", "10.0.0.2")

                , Toast.LENGTH_LONG).show();

    }

    public void salvarEnderecoServidor(View view){
        if(edtEndereco.getText().toString() !=null ){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("ENDERECOSERVIDOR", edtEndereco.getText().toString()).commit();
            Toast.makeText(this, "Endereço salvo com sucesso", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, "Insira um endereço para o servidor", Toast.LENGTH_SHORT).show();
        }

    }

    public static String retornarEnderecoServidor(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("ENDERECOSERVIDOR", "10.0.0.2");

    }

}
