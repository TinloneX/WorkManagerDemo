package com.tinlone.paging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class BActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);
    }

    public void close(View view) {
        setResult("this is message from b , setResult & finish");
//        finish();
    }

    public void close2(View view) {
        finish();
    }

    private void setResult(String message) {
        Utils.log("setResult: ========== " + message);
        Intent intent = new Intent();
        intent.putExtra("message", message);
        setResult(100, intent);
    }

    @Override
    public void finish() {
        Utils.log("finish start");
        super.finish();
        Utils.log("finish end");
    }

    @Override
    protected void onPause() {
        setResult("this is a message from b , setResult & onPause 1");
        super.onPause();
        setResult("this is a message from b , setResult & onPause 2");
    }

    @Override
    protected void onStop() {
        setResult("this is a message from b , setResult & onStop 1");
        super.onStop();
        setResult("this is a message from b , setResult & onStop 2");
    }

    @Override
    protected void onDestroy() {
        setResult("this is a message from b , setResult & onDestroy 1");
        super.onDestroy();
        setResult("this is a message from b , setResult & onDestroy 2");
    }
}