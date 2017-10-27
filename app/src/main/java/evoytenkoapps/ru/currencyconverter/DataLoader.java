package evoytenkoapps.ru.currencyconverter;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Загружает данные с локального хранилища, сервера и сохраняет их в хранилище0
 */

public class DataLoader extends AsyncTask<String, Void, String>
{
    private String TAG = this.getClass().getSimpleName();
    private final String FILE_NAME = "XML";
    private final int PAUSE = 5000;

    HttpDelegate delegate = null;
    private Context mContext;
    private SharedPreferences mPref;

    DataLoader(Context context)
    {
        mContext = context;
        mPref = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected String doInBackground(String... path)
    {
        String result;
        URL url = null;
        try
        {
            url = new URL(path[0]);
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        // Загружаем данные
        // Если есть интернет то с сервера
        if (isInternetAvailable())
        {
            result = getXMLFromServer(url);
        }
        // Если нет то из локального хранилища
        else
        {
            // Чтобы при первом запуске программы данные пытались загрузиться пока не загрузяться, не зависимо есть интернет или нет
            // isCancelled чтобы завершать while если поступит команда cancel
            while ((result = loadXMLFromFile()) == null && !isCancelled())
            {
                try
                {
                    // Чтобы обращаться к компанентам из MainActivity нужно использовать runOnUiThread
                    final MainActivity act = (MainActivity) mContext;
                    act.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            act.showToast("Для получения курса валют необходим интернет!");
                        }
                    });
                    // Чтобы загрузка по интернету была раз в 5 сек
                    Thread.sleep(PAUSE);
                    if (isInternetAvailable())
                    {
                        result = getXMLFromServer(url);
                    }

                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        // Если данные есть то грузим из хранилища
        return result;
    }


    @Override
    protected void onPostExecute(String xml)
    {
        super.onPostExecute(xml);
        if (xml != null)
        {
            delegate.processFinish(xml);
        }
    }

    //Чтобы получить XML с локального хранилища
    private String loadXMLFromFile()
    {
        if (mPref.contains("data"))
        {
            String res = mPref.getString("data", null);
            Log.d(TAG, "Загрузили XML из хранилища data = " + res);
            final MainActivity act = (MainActivity) mContext;
            act.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    act.showToast("Загружены предыдущие курсы валют");
                }
            });
            return res;
        }
        Log.d(TAG, "В хранилище нет XML файла");
        return null;
    }


    // Чтобы провереить есть интернет или нет
    private boolean isInternetAvailable()
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            // Интернет есть
            if ((networkInfo != null && networkInfo.isConnected()))
            {
                Log.d(TAG, "Интернет есть");
                return true;
            } else
            {
                Log.d(TAG, "Интернета нет");
                return false;
            }
        } catch (Exception ex)
        {
            Log.d(TAG, "Ошибка проверки интернет подключения " + " " + ex.toString());
            return false;
        }
    }

    // Чтобы получить данные с сервера
    private String getXMLFromServer(URL url)
    {
        Log.d(TAG, "Загружаем данные с сервера");
        InputStream inputStream = null;
        try
        {
            // Чтобы не было exception "Too many redirects: 21" CookieHandler
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(10 * 1000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int respCode = connection.getResponseCode();
            Log.d(TAG, "Код сервера: " + respCode);
            inputStream = connection.getInputStream();

            // Чтобы преобразовать поток в строку
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "windows-1251"));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            Log.d(TAG, "Получили от сервера:\n" + stringBuilder.toString());
            saveXML(stringBuilder.toString());

            // Чтобы обращаться к компанентам из MainActivity нужно использовать runOnUiThread
            final MainActivity act = (MainActivity) mContext;
            act.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    act.showToast("Курсы валют были успешно обновлены");
                }
            });
            return stringBuilder.toString();

        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (ProtocolException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "От сервреа не пришел XML");
        return null;
    }

    // Чтобы сохранить XML в хранилище
    private void saveXML(String xml)
    {
        Log.d(TAG, "Сохраняем XML data = " + xml);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("data", xml);
        editor.apply();
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        Log.d(TAG, "Task Cancel");
    }

}
