package com.bmzy.gpsinfo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.bmzy.gpsinfo.adapter.SpinnerArrayAdapter;
import com.bmzy.gpsinfo.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Spinner mSpinner;
    Button mBtnStart;

    //操作人员id
    int executorUserId = 0;

    SpinnerArrayAdapter adapter;

    private List<UserInfo> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vertifyExecutorUser();
        setContentView(R.layout.activity_main);
        initData();
        initViews();
    }


    private void vertifyExecutorUser(){
        if (MyApplication.EXECUTOR_USER_ID >0){
            goGpsInfoActivity();
            return;
        }
    }
    /**
     * 初始化数据
     */
    private void initData() {
        for (int i = 0; i < 25; i++) {
            UserInfo  userInfo = new UserInfo();
            userInfo.setId(i+1);
            userInfo.setName("安全员");
            list.add(userInfo);
        }
    }


    private void initViews() {
        mSpinner = findViewById(R.id.main_spinner_array);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(executorUserId == 0){
                    Toast.makeText(MainActivity.this,"请选择操作人员",Toast.LENGTH_LONG).show();
                    return;
                }
                MyApplication.EXECUTOR_USER_ID = executorUserId;
                goGpsInfoActivity();
            }
        });

        adapter = new SpinnerArrayAdapter(this, list);

        mSpinner.setAdapter(adapter);

        // 为spinner设置点击事件
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Log.e("选中：", list.get(i).getName());

                executorUserId=i;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                executorUserId=0;
            }
        });
    }

    private void goGpsInfoActivity(){
        Intent intent = new Intent(MainActivity.this,GPSInfoActivity.class);
        startActivity(intent);
        finish();
    }
}
