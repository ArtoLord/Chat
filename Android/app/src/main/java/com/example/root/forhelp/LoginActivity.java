package com.example.root.forhelp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.Requests.LogUser;
import com.example.root.forhelp.Requests.LoginApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    EditText log;
    EditText pass;
    private static LoginApi Login;
    private Retrofit retrofit;
    String url = new Config().url;
    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sPref = getSharedPreferences("MyPref",MODE_PRIVATE);
        if(sPref.getString(StaticCl.IS_ENTER,"").equals("True")){
            String email = sPref.getString(StaticCl.EMAIL,"");
            String _id  = sPref.getString(StaticCl._ID,"");
            String jwt = sPref.getString(StaticCl.JWT,"");
            User user = new User(_id,email,jwt);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            intent.putExtra(User.class.getCanonicalName(),user);
            startActivity(intent);
        }

        log = (EditText)findViewById(R.id.login);
        pass = (EditText)findViewById(R.id.pass);
        retrofit = new Retrofit.Builder()
                .baseUrl(url) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        Login = retrofit.create(LoginApi.class);
    }






    public void enterUser(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Loading");
            builder.setMessage("Pleese wait...");
            builder.setCancelable(true);

            final AlertDialog dlg = builder.create();

            dlg.show();



        Login.login(new LogUser(log.getText().toString(),pass.getText().toString())).enqueue(new Callback<User>(){
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No such user", Toast.LENGTH_LONG);
                toast.show();


                dlg.dismiss();

            }

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                try {


                if (response.body().email != null){
                }
                dlg.dismiss();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                intent.putExtra(User.class.getCanonicalName(),response.body());
                startActivity(intent);



            }catch(Exception err){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            err.toString(), Toast.LENGTH_LONG);
                    toast.show();
                    dlg.dismiss();
                }
            }

        }  );






            }



    public void register(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

}





