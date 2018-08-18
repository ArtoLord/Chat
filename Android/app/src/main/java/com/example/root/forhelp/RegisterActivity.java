package com.example.root.forhelp;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.Requests.LoginApi;
import com.example.root.forhelp.Requests.RegUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    EditText email,pass;
    private static LoginApi Login;
    private Retrofit retrofit;
    String url = new Config().url;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText)findViewById(R.id.email);
        pass = (EditText)findViewById(R.id.pass);
        retrofit = new Retrofit.Builder()
                .baseUrl(url) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        Login = retrofit.create(LoginApi.class);

    }


    public void regUser(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Loading");
        builder.setMessage("Pleese wait...");
        builder.setCancelable(true);

        final AlertDialog dlg = builder.create();

        dlg.show();



        Login.setUser(new RegUser(
                email.getText().toString(),
                pass.getText().toString())).enqueue( new Callback<User>(){
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error", Toast.LENGTH_LONG);
                toast.show();


                dlg.dismiss();

            }

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body().email != null){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            response.body().email + " registrated", Toast.LENGTH_LONG);
                    toast.show();}
                dlg.dismiss();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);


            }
        }  );






    }






}
