package com.yiyang.invoicecheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    InvoiceCheck ic = null;
    String[] lstYears = null;
    String[] lstMonths = new String[]{"01月、02", "03月、04", "05月、06", "07月、08", "09月、10", "11月、12"};
    int inxYear = 0;
    int inxMonth = 0;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {    //startActivityForResult回傳值
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");    //取得QR Code內容
                if (InvoiceCheck.isInvoiceNumber(contents)) {
                    ((EditText) findViewById(R.id.txtInvoiceNum)).setText(contents.substring(2, 10));

                    String year = contents.substring(10, 13);
                    for (int i = 0; i < lstYears.length; i++) {
                        if(year.equals(lstYears[i])){
                            inxYear = i;
                        }
                    }
                    ((Spinner) findViewById(R.id.spnYear)).setSelection(inxYear);

                    inxMonth = (Integer.valueOf(contents.substring(13, 15)) - 1) / 2;
                    ((Spinner) findViewById(R.id.spnMonth)).setSelection(inxMonth);
                } else
                    ((EditText) findViewById(R.id.txtInvoiceNum)).setText("找不到");
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        lstYears = getYearList();
        Spinner spnYear = (Spinner) findViewById(R.id.spnYear);
        ArrayAdapter adapterYear = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lstYears);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnYear.setAdapter(adapterYear);

        Spinner spnMonth = (Spinner) findViewById(R.id.spnMonth);
        ArrayAdapter adapterMonth = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lstMonths);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(adapterMonth);


        ic = new InvoiceCheck("http://invoice.etax.nat.gov.tw/invoice.xml");
        ic.loadData();


        ((Button) findViewById(R.id.btnScan)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent("com.google.zxing.client.android.SCAN");    //開啟條碼掃描器
                        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");    //設定QR Code參數
                        startActivityForResult(intent, 1);    //要求回傳1

                    }
                });

        ((Button) findViewById(R.id.btnCheck)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String inNum = "********" + ((EditText) findViewById(R.id.txtInvoiceNum)).getText().toString();
                        inNum = inNum.substring(inNum.length() - 8);

                        ((TextView) findViewById(R.id.txtResult)).setText("查詢 " + inNum + " 結果:\n" +
                                ic.checkNumber(inNum, lstYears[inxYear] + "年" + lstMonths[inxMonth]));
                    }
                });

        ((Spinner) findViewById(R.id.spnYear)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                inxYear = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ((Spinner) findViewById(R.id.spnMonth)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                inxMonth = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private String[] getYearList() {
        Calendar cal = Calendar.getInstance();

        String[] result = new String[5];
        for (int i = 0; i < 5; i++) {
            result[i] = String.valueOf(cal.get(Calendar.YEAR) - 1911 - i);
        }
        return result;
    }
}
