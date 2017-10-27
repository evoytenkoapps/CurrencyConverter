package evoytenkoapps.ru.currencyconverter;

/*
* Видимое активити
* */

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import data.ValCurs;
import data.Valute;

public class MainActivity extends AppCompatActivity implements HttpDelegate
{
    private final String TAG = this.getClass().getSimpleName();
    private final String URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private DataLoader mDataLoader;
    private TextInputEditText mEdtSumm;
    private Spinner mSpFrom;
    private Spinner mSpTo;
    private MyAdapter mAdapterFrom;
    private MyAdapter mAdapterTo;
    private ArrayList<Valute> mCurrency;
    private Button mBtnConvert;
    private TextView mTvResult;

    private boolean isActivityShowing = false;
    private ProgressBar mPb;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdtSumm = (TextInputEditText) findViewById(R.id.edt_summ);
        mSpFrom = (Spinner) findViewById(R.id.sp_from);
        mSpTo = (Spinner) findViewById(R.id.sp_to);
        mBtnConvert = (Button) findViewById(R.id.btn_convert);
        mBtnConvert.setOnClickListener(btnOnClick);
        mTvResult = (TextView) findViewById(R.id.tv_result);
        mPb = (ProgressBar) findViewById(R.id.pb);
        mPb.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        mContext = this;

        mCurrency = new ArrayList<>();
        // В классе Valute переопределет toSting, он возвращает "Name + Value"
        mAdapterFrom = new MyAdapter(this, R.layout.spinner_row, mCurrency);
        mSpFrom.setAdapter(mAdapterFrom);
        mAdapterFrom.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        mSpFrom.setOnItemSelectedListener(spFromSelection);

        mAdapterTo = new MyAdapter(this, R.layout.spinner_row, mCurrency);
        mSpTo.setAdapter(mAdapterTo);
        mAdapterTo.setDropDownViewResource(R.layout.spinner_row);
        mSpTo.setOnItemSelectedListener(spToSelection);
    }

    // Чтобы определять какой элементы был выбран и показывать для него галочку
    AdapterView.OnItemSelectedListener spFromSelection = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            mAdapterFrom.setPos(position);
            Log.d(TAG, "Выбрано: " + mCurrency.get(position).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }
    };

    // Чтобы определять какой элементы был выбран и показывать для него галочку
    AdapterView.OnItemSelectedListener spToSelection = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            mAdapterTo.setPos(position);
            Log.d(TAG, "Выбрано: " + mCurrency.get(position).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }
    };

    @Override
    protected void onStop()
    {
        super.onStop();
        // Чтобы Toast не показывался, если активти не активно.
        isActivityShowing = false;
        //TODO проработать
        //Чтобы завершить все процессы в AsyncTask и при первом запуске программы, если данные не
        //загрузилиь и мы ее свернули, не было попыток загрузить данные в фоне.
        if (mDataLoader != null && mDataLoader.getStatus() != AsyncTask.Status.FINISHED)
            mDataLoader.cancel(true);
        Log.d(TAG, "onStop");
    }


    @Override
    protected void onResume()
    {
        super.onStop();
        Log.d(TAG, "onResume");
        // Чтобы Toast  показывался, если активти активно.
        isActivityShowing = true;
        // Чтобы если свернули приложение и данные не загрузились ранее, они загружались.
        if (mCurrency.size() == 0)
        {
            mDataLoader = new DataLoader(mContext);
            mDataLoader.delegate = MainActivity.this;
            mDataLoader.execute(URL);
        }
    }

    //Чтобы показывать сообщения пользователю
    public void showToast(String text)
    {
        if (isActivityShowing)
        {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    View.OnClickListener btnOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (validate())
            {
                double summ = Double.valueOf(String.valueOf(mEdtSumm.getText()));
                double fromValue = Double.valueOf(mCurrency.get(mSpFrom.getSelectedItemPosition()).getValue().replaceAll(",", "."));
                double fromNominal = mCurrency.get(mSpFrom.getSelectedItemPosition()).getNominal();
                double toValue = Double.valueOf(mCurrency.get(mSpTo.getSelectedItemPosition()).getValue().replaceAll(",", "."));
                double toNominal = mCurrency.get(mSpTo.getSelectedItemPosition()).getNominal();

                //https://academy.terrasoft.ru/sites/default/files/documents/docs/product/bpm%27online%20sales/team/7.7.0/BPMonlineHelp/chapter_currencies/faq_exchange_rates.htm
                //Сумма в валюте конвертации(2)]=[Сумма в валюте конвертации(1)]*[Кратность(1)]*[Курс(2)]/[Кратность(2)]*[Курс(1)]
                double result = (summ * fromNominal * toValue) / (toNominal * fromValue);
                Log.d(TAG,
                        "Итог:"
                                + "\n" + "Введена сумма: " + summ
                                + "\n" + mCurrency.get(mSpFrom.getSelectedItemPosition()).getName() + " " + fromValue
                                + "\n" + mCurrency.get(mSpTo.getSelectedItemPosition()).getName() + " " + toValue
                                + "\n" + "Результат: " + result);
                // Чтобы убрать лишние цифры после запятой у double
                mTvResult.setText("Результат: " + BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP));
            }
        }
    };

    // Чтобы выводить ошибку если не введена сумма
    private boolean validate()
    {
        String summ = mEdtSumm.getText().toString();
        if (summ.isEmpty())
        {
            mEdtSumm.setError("Введите сумму");
            return false;
        }
        if (mCurrency.size() == 0)
        {
            return false;
        }
        return true;
    }

    // Чтобы получать данные от DataLoader
    @Override
    public void processFinish(String xml)
    {
        if (xml != null)
        {
            mPb.setVisibility(View.INVISIBLE);
            parseXml(xml);
        }
    }

    // Чтобы преобразовать XML в Java
    private void parseXml(String xml)
    {
        if (xml != null)
        {
            Reader reader = new StringReader(xml);
            Persister serializer = new Persister();
            try
            {
                ValCurs valCurs = serializer.read(ValCurs.class, reader, false);
                mCurrency.clear();
                for (Valute valute : valCurs.getList())
                {
                    mCurrency.add(valute);
                }
                mAdapterFrom.notifyDataSetChanged();
                mAdapterTo.notifyDataSetChanged();
                Log.d(TAG, "Распарсили " + mCurrency.size() + " валют");
            } catch (Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
