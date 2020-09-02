package com.bmzy.gpsinfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.bmzy.gpsinfo.adapter.SpinnerArrayAdapter;
import com.bmzy.gpsinfo.bean.Constants;
import com.bmzy.gpsinfo.bean.UserInfo;
import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.StringCallBack;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Spinner mSpinner;
    Button mBtnStart;

    //操作人员id
    String executorUserId = "";
    SpinnerArrayAdapter adapter = null;
    private List<UserInfo> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vertifyExecutorUser();
        setContentView(R.layout.activity_main);
        initViews();
        initData();
    }


    private void vertifyExecutorUser() {
        if (MyApplication.EXECUTOR_USER_ID != null && MyApplication.EXECUTOR_USER_ID.length() > 0) {
            goGpsInfoActivity();
            return;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {

        MyHttpUtils.build()
                .url(Constants.getUserList(Constants.API_DOMAIN))
                .onExecute(new StringCallBack() {
                    @Override
                    public void onSucceed(String result) {
                        list.clear();
                        list = UserInfo.arrayUserInfoFromData(result);
                        adapter.refreshList(list);

                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 初始化各view内容
     */
    private void initViews() {

        mSpinner = findViewById(R.id.main_spinner_array);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(view -> {
            MyApplication.EXECUTOR_USER_ID = executorUserId;
            if (MyApplication.EXECUTOR_USER_ID.length() == 0) {
                Toast.makeText(MainActivity.this, "请选择操作人员", Toast.LENGTH_LONG).show();
                return;
            }
            goGpsInfoActivity();
        });

        adapter = new SpinnerArrayAdapter(this, list);
        mSpinner.setAdapter(adapter);

        // 为spinner设置点击事件
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                executorUserId = list.get(i).id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                MyApplication.getApplication().resetUserId();
            }
        });
    }


    private void goGpsInfoActivity() {
        Intent intent = new Intent(MainActivity.this, GPSInfoActivity.class);
        startActivity(intent);
        finish();
    }
}
