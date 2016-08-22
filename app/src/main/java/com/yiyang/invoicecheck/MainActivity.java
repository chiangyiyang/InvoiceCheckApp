package com.yiyang.invoicecheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    InvoiceCheck ic = null;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {    //startActivityForResult回傳值
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");    //取得QR Code內容
                if (InvoiceCheck.isInvoiceNumber(contents))
                    ((EditText) findViewById(R.id.txtInvoiceNum)).setText(contents.substring(2,10));
                else
                    ((EditText) findViewById(R.id.txtInvoiceNum)).setText("找不到");
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
                                ic.checkNumber(inNum));

                    }
                });
    }
}
