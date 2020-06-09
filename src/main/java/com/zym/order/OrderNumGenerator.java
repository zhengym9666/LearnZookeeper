package com.zym.order;

import java.text.SimpleDateFormat;
import java.util.Date;

//生成订单号规则：时间戳+业务ID
public class OrderNumGenerator {

    //业务ID
    private static int count = 0;

    //生成订单号
    public String createOrderNum() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return sdf.format(new Date())+"-"+ ++count;
    }

}
