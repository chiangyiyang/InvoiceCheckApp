package com.yiyang.invoicecheck;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by yvtc on 2016/8/22.
 */
public class InvoiceCheck {
    private final String url;
    private InvoiceData[] invData;

    public static boolean isInvoiceNumber(String contents) {
        return contents.substring(0,10).toUpperCase().matches("[A-Z]{2}[0-9]{8}.*");
    }

    public InvoiceCheck(String url) {
        this.url = url;
    }

    public boolean loadData() {
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        DocumentBuilder db = null;
//        Document doc = null;
//        try {
//            db = dbf.newDocumentBuilder();
//            doc = db.parse(new URL(url).openStream());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        NodeList titles = doc.getElementsByTagName("title");
//        NodeList descriptions = doc.getElementsByTagName("description");
//
//        invData = new InvoiceData[titles.getLength() - 1];
//        for (int i = 1; i < titles.getLength(); i++) {
//            String[] items = descriptions.item(i).getTextContent().split("</p><p>");
//            invData[i - 1] = new InvoiceData(
//                    titles.item(i).getTextContent(),
//                    items[0].replace("<p>", "").replace("特別獎：", ""),
//                    items[1].replace("特獎：", ""),
//                    items[2].replace("頭獎：", ""),
//                    items[3].replace("</p>", "").replace("增開六獎：", ""));
//        }


        InvoiceDataLoader idl = new InvoiceDataLoader(url);
        idl.start();
        try {
            idl.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Document doc = idl.doc;

        while (doc == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        NodeList titles = doc.getElementsByTagName("title");
        NodeList descriptions = doc.getElementsByTagName("description");

        invData = new InvoiceData[titles.getLength() - 1];
        for (int i = 1; i < titles.getLength(); i++) {
            String[] items = descriptions.item(i).getTextContent().split("</p><p>");
            invData[i - 1] = new InvoiceData(
                    titles.item(i).getTextContent(),
                    items[0].replace("<p>", "").replace("特別獎：", ""),
                    items[1].replace("特獎：", ""),
                    items[2].replace("頭獎：", ""),
                    items[3].replace("</p>", "").replace("增開六獎：", ""));
        }


        return true;
    }

    public void dumpData() {
        for (int i = 0; i < invData.length; i++)
            System.out.println(invData[i].toString() + "\n");
    }

    public String checkNumber(String chkNum) {
        String result = "";
        for (int i = 0; i < invData.length; i++) {
            if (chkNum.equals(invData[i].price10M))
                result += "中了" + invData[i].periodInfo + "月的特別獎 " + invData[i].price10M + " (1000萬)\n";
            if (chkNum.equals(invData[i].price2M))
                result += "中了" + invData[i].periodInfo + "月的特獎 " + invData[i].price2M + " (200萬)\n";

            //頭獎
            {
                String[] nums = invData[i].price200K.split("、");
                int matchedNum = 0;
                String matchedPattern = "";
                for (int j = 0; j < nums.length; j++) {
                    matchedNum = 0;
                    for (int k = 3; k <= 8; k++) {
                        if (!chkNum.substring(8 - k).equals(nums[j].substring(8 - k))) break;
                        matchedPattern = chkNum.substring(8 - k);
                        matchedNum = k;
                    }
                    if (matchedNum >= 3) break;
                }
                switch (matchedNum) {
                    case 3:
                        result += "中了" + invData[i].periodInfo + "月的六獎 " + matchedPattern + " (200元)\n";
                        break;
                    case 4:
                        result += "中了" + invData[i].periodInfo + "月的五獎 " + matchedPattern + " (1000元)\n";
                        break;
                    case 5:
                        result += "中了" + invData[i].periodInfo + "月的四獎 " + matchedPattern + " (4000元)\n";
                        break;
                    case 6:
                        result += "中了" + invData[i].periodInfo + "月的三獎 " + matchedPattern + " (1萬)\n";
                        break;
                    case 7:
                        result += "中了" + invData[i].periodInfo + "月的二獎 " + matchedPattern + " (4萬)\n";
                        break;
                    case 8:
                        result += "中了" + invData[i].periodInfo + "月的頭獎 " + matchedPattern + " (20萬)\n";
                        break;
                }
            }

            //增開六獎
            {
                String[] nums = invData[i].extPrice200.split("、");
                for (int j = 0; j < nums.length; j++) {
                    if (chkNum.substring(5).equals(nums[j])) {
                        result += "中了" + invData[i].periodInfo + "月的增開六獎 " + nums[j] + " (200元)\n";
                        break;
                    }
                }
            }
        }

        return result.equals("") ? "沒中獎" : result;
    }

    private class InvoiceData {
        private final String periodInfo;
        private final String price10M;
        private final String price2M;
        private final String price200K;
        private final String extPrice200;

        public InvoiceData(String periodInfo, String price10M, String price2M, String price200K, String extPrice200) {
            this.periodInfo = periodInfo;
            this.price10M = price10M;
            this.price2M = price2M;
            this.price200K = price200K;
            this.extPrice200 = extPrice200;
        }

        @Override
        public String toString() {
            return "期別: " + periodInfo + "\n特別獎：" + price10M + "\n特獎：" + price2M + "\n頭獎：" + price200K + "\n增開六獎：" + extPrice200;
        }

    }

    private class InvoiceDataLoader extends Thread {
        private final String url;
        public Document doc = null;

        public InvoiceDataLoader(String url) {
            this.url = url;
        }

        @Override
        public void run() {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;

            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(new URL(url).openStream());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}


