package com.example.vb.webservice;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RunnableFuture;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    //eger android emulator kullanıyor iseniz alltaki url kullanın
    //Genymotion için 10.0.3.2 kullanın
    private static String url ="http://10.0.2.2:8080/api/ogrenci";

    //gelen verileri tuttuğumuz listemiz
    ArrayList<HashMap<String,String>> list ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //gerekli tanımlamalar
        list = new ArrayList<>();
        lv= (ListView) findViewById(R.id.list);
        //alttaki classımıza Async' işlem kullnarak çalıştırmasını sağlıyourz
        new getOgrenci().execute();
    }

    private class getOgrenci extends AsyncTask<Void,Void,Void> {


        //Async işlemlerde ilk çalışan metod giriş işlemleri vs ayarlanır
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //bir progress dialog ile gelen verileri beklerken bekleme paneli oluşturduk
            pDialog=new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Lütfen bekleyin...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
           HttpHandler hh = new HttpHandler();

            //servisimizi çağırıyoruz
            String jsonStr = hh.makeServiceCall(url);
            Log.e(TAG,"Response from url:"+ jsonStr);

            if (jsonStr!=null) {
                //eğer herhangi bir hata yoksa json işlemleride alt satıra devam
                try {
                    //artık elimizde bir json satırı var ve bunu işleme zamanı
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    //json nesnemizin title' da ögrenciler di unutmayın
                    JSONArray contacts = jsonObj.getJSONArray("ogrenciler");

                    for (int i=0;i<contacts.length();i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        //sırayla okuma var burada
                        //verileri alıp birer stringe atıyoruz
                        String id = c.getString("id");
                        String name = c.getString("name");
                        String email = c.getString("age");
                        String mobile = c.getString("school");

                        HashMap<String,String> contact = new HashMap<>();
                        //geçici bir contact nesnesine atayıp bunuda ana listemize atıyoruz
                        contact.put("id",id);
                        contact.put("name",name);
                        contact.put("age",email);
                        contact.put("school",mobile);

                        list.add(contact);

                    }


                } catch (final JSONException e) {
                    //json dizilimde bir hata var
                   Log.e(TAG,"Json parsing error:"+ e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Json error"+e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
            else {
                //servere bağlanmada eğer açık değilse hata verir
                Log.e(TAG,"Couldn't get json from server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Couldnt get json from server Check logcat", Toast.LENGTH_SHORT).show();
                    }
                });
                
            }

            return null;
        }

        //async işlemlerde son işlemin yapılıp buraya paslanır ve sonlanır
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing()) pDialog.dismiss();

            //listemize gömüyoruz
            ListAdapter adapter = new SimpleAdapter(MainActivity.this
                    ,list,R.layout.list_item
                    ,new String[]{"name","age","school"}
                    ,new int[]{R.id.tvName,R.id.tvAge,R.id.tvSchool});
            lv.setAdapter(adapter);
        }
    }
}
