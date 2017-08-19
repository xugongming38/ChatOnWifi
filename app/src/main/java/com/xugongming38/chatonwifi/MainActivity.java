package com.xugongming38.chatonwifi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.xugongming38.chatonwifi.activity.RealMainActivity;
import com.xugongming38.chatonwifi.utils.UsedConst;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s="正在自动连接wifi中...\n\n(若未连接，请手动连接\nWiFi名为：ChatOnWifi\n密码为：123456789)";
                showDialog(s);
            }
        });

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s="正在创建热点中..." +
                        "WiFi名为：ChatOnWifi\n" +
                        "密码为：123456789\n\n(若未自动创建，请手动打开热点)";
                showDialog(s);
            }
        });

    }
    public void SetName(View view){
        EditText edt=(EditText)findViewById(R.id.ed);
        String str=edt.getText().toString();
        if(str.length()!=0)
            UsedConst.name=str;

    }
   private void showDialog(String string){
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        View localView = getLayoutInflater().inflate(R.layout.abouts, null);
        TextView localTextView1 = (TextView)localView.findViewById(R.id.title);
        Typeface localTypeface = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        localTextView1.setTypeface(localTypeface);
        TextView localTextView2 = (TextView)localView.findViewById(R.id.content);
        SpannableString localSpannableString = new SpannableString(string);
        Linkify.addLinks(localSpannableString,Linkify.ALL);
        localTextView2.setTypeface(localTypeface);
        localTextView2.setText(localSpannableString);
        localBuilder.setView(localView).setPositiveButton("确定！", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
                startActivity(new Intent(MainActivity.this, RealMainActivity.class));
            }
        });
        localBuilder.show();
    }

}
