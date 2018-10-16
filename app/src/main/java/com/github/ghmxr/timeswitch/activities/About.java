package com.github.ghmxr.timeswitch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class About extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);
        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        FloatingActionButton fab = findViewById(R.id.fab_about);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL,new String[]{"mxremail@qq.com"});
                try{
                    startActivity(i);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(About.this,e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.about_donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    startActivity(Intent.parseUri("https://qr.alipay.com/FKX08041Y09ZGT6ZT91FA5",Intent.URI_INTENT_SCHEME));
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(About.this,e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void processMessage(Message msg) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
