package com.bmzy.gpsinfo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bmzy.gpsinfo.adapter.SearchAdapter;
import com.bmzy.gpsinfo.adapter.SpinnerArrayAdapter;
import com.bmzy.gpsinfo.bean.Constants;
import com.bmzy.gpsinfo.bean.UserInfo;
import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.StringCallBack;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    TextView mTvSelectUser;

    /**
     * 接口地址
     */
    EditText etIpAddress;
    // 进入系统
    Button mBtnStart;
    // 刷新数据
    Button mBtnRefresh;
    private String gpsUrl = Constants.API_DOMAIN;

    //操作人员id
    String executorUserId = "";
    SpinnerArrayAdapter adapter = null;
    private List<UserInfo> list = new ArrayList<>();


    private LinearLayout empty;
    private AutoCompleteTextView autoTvSearch;
    SearchAdapter<String> autoSearchAdapter;
    private String[] searchArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vertifyExecutorUser();
        setContentView(R.layout.activity_login);
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
//                        adapter.refreshList(list);

                        // 此部分为title部分搜索数据集
                        if (list.size() > 0) {
                            searchArray = new String[list.size()];
                            for (int i = 0; i < list.size(); i++) {
                                UserInfo userInfo = list.get(i);
                                searchArray[i] = userInfo.userName + " - " + userInfo.post;
                            }

                            autoSearchAdapter = new SearchAdapter<>(LoginActivity.this,
                                    android.R.layout.simple_list_item_1, searchArray, SearchAdapter.ALL);
                            autoTvSearch.setAdapter(autoSearchAdapter);
                            Toast.makeText(LoginActivity.this,"数据获取成功",Toast.LENGTH_SHORT).show();
                        }
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

        empty = findViewById(R.id.empty);
        empty.setOnClickListener(view -> autoTvSearch.setText(""));

        mTvSelectUser = findViewById(R.id.tv_select_login_user);
        etIpAddress = findViewById(R.id.et_api_host);
        etIpAddress.setText(Constants.API_DOMAIN);

        autoTvSearch = findViewById(R.id.search);
        autoTvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                // AutoCompleteTextView 下拉列表数据集里只有姓名-职位集合，点击后使下面
                // 的spinner下拉框内也同步更新，需要反查一下数据
                TextView textView = (TextView) view;
                String name = textView.getText().toString();
                mTvSelectUser.setText(name);
                parseAutoTextArrayToSpinnerData(name);
            }
        });

//        mSpinner = findViewById(R.id.main_spinner_array);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnRefresh = findViewById(R.id.btn_refresh_user_list);
        mBtnStart.setOnClickListener(view -> {
            MyApplication.EXECUTOR_USER_ID = executorUserId;
            if (MyApplication.EXECUTOR_USER_ID.length() == 0) {
                Toast.makeText(LoginActivity.this, "请先搜索操作人员", Toast.LENGTH_LONG).show();
                return;
            }
            gpsUrl = etIpAddress.getText().toString().trim();
            // 全局变量，临时存储
            Constants.API_DOMAIN = gpsUrl;
            goGpsInfoActivity();
        });
        mBtnRefresh.setOnClickListener(view ->{
            gpsUrl = etIpAddress.getText().toString().trim();
            // 全局变量，临时存储
            Constants.API_DOMAIN = gpsUrl;
            initData();
        });

//        adapter = new SpinnerArrayAdapter(this, list);
//        mSpinner.setAdapter(adapter);
//
//        // 为spinner设置点击事件
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                executorUserId = list.get(i).id;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                executorUserId = "";
//            }
//        });
    }

    private void parseAutoTextArrayToSpinnerData(String clickName) {
        for (int i = 0; i < list.size(); i++) {
            UserInfo userInfo = list.get(i);
            String nameAndPost = userInfo.userName + " - " + userInfo.post;
            if (nameAndPost.equals(clickName)) {
                executorUserId = list.get(i).id;
                Log.e(executorUserId, clickName);
                return;
            }
        }
    }


    private void goGpsInfoActivity() {
        Intent intent = new Intent(LoginActivity.this, GPSInfoActivity.class);
        startActivity(intent);
        finish();
    }
}
