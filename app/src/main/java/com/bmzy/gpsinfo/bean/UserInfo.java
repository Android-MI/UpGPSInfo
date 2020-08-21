package com.bmzy.gpsinfo.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserInfo {

    /**
     * id : 0001E410000000009QOH
     * userName : 吕
     * sex : 1
     * email : *
     * birthday : null
     * orgID : 1001E41000000006VI1P
     * pdaRole : null
     * invalid : true
     * version : 1
     * createdTime : 2017-03-03 15:17:18
     * lastmodifidTime : 2020-07-02 11:00:12
     * password : 1
     * status : true
     * lastLoginTime : null
     * workNo : 06000241
     * cardNo : null
     * post : 副大队长
     * postId : 1001E41000000006VYFA
     * outsiderValid : null
     */

    public String id;
    public String userName;
    public int sex;
    public String email;
    public String phone;
    public Object birthday;
    public String orgID;
    public Object pdaRole;
    public boolean invalid;
    public int version;
    public String createdTime;
    public String lastmodifidTime;
    public String password;
    public boolean status;
    public Object lastLoginTime;
    public String workNo;
    public Object cardNo;
    public String post;
    public String postId;
    public Object workAreaContent;
    public String address;
    public Object isCertified;
    public int delSatus;
    public Object userType;
    public Object defaultLineId;
    public Object outWorkGroupId;
    public Object outsiderValid;

    public static UserInfo objectFromData(String str) {

        return new Gson().fromJson(str, UserInfo.class);
    }

    public static List<UserInfo> arrayUserInfoFromData(String str) {

        Type listType = new TypeToken<ArrayList<UserInfo>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }
}
