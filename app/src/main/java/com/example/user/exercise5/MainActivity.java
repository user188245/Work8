package com.example.user.exercise5;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener{
    EditText editText;
    WebView webView;
    ProgressDialog dialog;
    Handler handler = new Handler();
    Animation anim;
    LinearLayout linearLayout;

    boolean isViewFav = false;

    ListView listview;
    ArrayList<CharSequence> myList;
    ArrayAdapter<CharSequence> arrayAdapter;

    int c = 0;
    int[] colorList = {Color.RED,Color.BLUE,Color.GREEN};
    Runnable crazy = new Runnable() {
        @Override
        public void run() {
            linearLayout.setBackgroundColor(colorList[(c++) % 3]);
            if(linearLayout.getVisibility() == View.VISIBLE)
                handler.postDelayed(this, 40);
        }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        setTitle("웹 도우미");
        webView = (WebView) findViewById(R.id.webView);
        webView.addJavascriptInterface(new JavaScriptMethods(),"Application0");
        myList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1,myList);
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(arrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CharSequence s = myList.get(position);
                                try {
                                    String url = s.toString().split(">")[1];
                                    webView.loadUrl(url);
                                    listview.setVisibility(View.VISIBLE);
                                    isViewFav = true;
                                }catch(NullPointerException e){}
                            }
                        });
                listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                        dlg.setTitle("삭제")
                                .setMessage("목록을 삭제하시겠습니까?")
                                .setNegativeButton("아니오",null)
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        myList.remove(position);
                                        Toast.makeText(getApplicationContext(),"삭제되었습니다.",Toast.LENGTH_SHORT).show();
                                        arrayAdapter.notifyDataSetChanged();
                            }
                        }).show();
                return false;
            }
        });




        editText = (EditText) findViewById(R.id.editText);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        anim = AnimationUtils.loadAnimation(this,R.anim.translate_top);
        anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    linearLayout.setVisibility(LinearLayout.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
        });
        handler.post(crazy);



        dialog = new ProgressDialog(this);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        webSettings.setBuiltInZoomControls(true);

        webSettings.setSupportZoom(true);

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.setMessage("로딩중");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return super.shouldOverrideUrlLoading(view, request);
            }
        });


        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if( newProgress >= 100) dialog.dismiss();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "즐겨찾기추가").setOnMenuItemClickListener(this);
        menu.add(0, 2, 0, "즐겨찾기목록").setOnMenuItemClickListener(this);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button:
                webView.loadUrl(editText.getText().toString());
                break;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case 1:
                webView.loadUrl("file:///android_asset/www/urladd.html");
                linearLayout.setVisibility(View.VISIBLE);
                popUp();
                break;
            case 2:
                if(!isViewFav) {
                    listview.setVisibility(View.VISIBLE);
                    isViewFav = true;
                }else{
                    listview.setVisibility(View.GONE);
                    isViewFav = false;
                }
                break;
            }
            return false;
        }



        public class JavaScriptMethods implements Runnable{
            String name;
            String url;
            @JavascriptInterface
            public void addItem(String name, String url){
                this.name = name;
                this.url = url;
                handler.post(this);
            }
            @JavascriptInterface
            public void viewTopBar(){
                popUp();
            }

            @Override
            public void run() {
                if(myList == null)
                    myList = new ArrayList<>();
                if(name.contains("<") || name.contains(">") || url.contains("<") || url.contains(">")) {
                    webView.loadUrl("javascript:setMsg(' \"<\"와 \">\"는 사용할 수 없습니다.')");
                    return;
                }
                CharSequence c = "<" + name + ">" + url;
                if(myList.contains(c))
                    webView.loadUrl("javascript:setMsg('이미 존재하는 항목입니다.')");
                else{
                    myList.add("<" + name + ">" + url);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }

    private void popUp() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(linearLayout.getVisibility() == View.GONE) {
                    linearLayout.setVisibility(View.VISIBLE);
                    handler.post(crazy);
                    webView.loadUrl("javascript:displayMsg2()");
                }
                else {
                    linearLayout.setAnimation(anim);
                    anim.start();
                    webView.loadUrl("javascript:displayMsg()");
                }
            }

        });
    }
}


