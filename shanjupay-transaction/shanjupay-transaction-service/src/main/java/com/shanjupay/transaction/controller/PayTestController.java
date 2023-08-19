package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 支付宝接口对接测试类
 */

@Slf4j
@Controller
public class PayTestController {

    // 应用id
    String APP_ID = "9021000123612327";
    // 应用私钥
    String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCrUvQP1RFiqeVbBy8Lcp10ppETTXw4Sffw10IqsoF0xRlTEtandcWGQz/LXqUqLLxFf1PzXB16kcJeIJbRFJnUixBkWVWfPQOs2eRKtKk91ebPdziqHs4SQyvnh+Ymi0mkFJzhK7znxhOOcaAYakthpgqMF0QLQRLN+JGwF1qGemL6uD26keLhZxYFKIsAyGvwYvsrdQOk2vrcdQJWZjzTcR25m6X96BaCXtLsOuaoPq+uyORr9U4zdDZiTS4Nvi7uhePFK8em/Fe7Ylwqkh58sPXARXu3X76RPVjx2QAqDrAOaaJMPYrtH/zf/WV90Nf7AioS04anW3ccTsVsoJPvAgMBAAECggEATEymW0K1+m3VsmORpRSHYUMpSyJwFxWCMaV8V02mb92zuJakOHIv5OqgR68/+Xdw5baeKby1eDfgC1GSOU1mcQz54OcURdXDhECJ9mddFCfmIjFjcAhGGEYIEJYqfrH7uvJfJ7z15XcGkKKz/QuhxLxQ6DEYYba6bDauk3DjJQ4zJ/+6Jqjv5QzkMkKa7tFI5b4oyiWGwTZswFR0TdpX7NSYYarttrnZUTjiMK6VeGGVsX+6R2KYbjeFgU7ccijD8nWR1ttj3rpeMvtPIkd/R5h3i8Fgn1jQAfhEQTSnFCFAX+uBOxvI99Z6tDIg9FeXyOvqipjmeBbXf/Z8aVajsQKBgQDlNixgi7VMvr+O2UmpPAVCH9frhLxmGsTZXjd7SEIT8zB01uB8Ubq8b8g/au8Fv3R5xFWeRmt8plBGmfrme+IN9bsd1NB+BeH27Q6NqXBaxwWYXOICBZMq8Xkstj7oXQG5LDeB+KkLwI356pHgR7okEOqj2YQjt3upiQ6DZe/pRwKBgQC/WNSTEr/vd+kiFX0cDGJ5WZ3IWVBmHhiRym61vaBVQ78eWFAS7qyVgSUuQ9A/5B+LFO73CiyBVUlxqla8xt7AGkZ8ZhsKz2c4xZz/98WlcKTEitdhldwN7eDwle+AtFJ850Q6dXHiuEV4h0Qg25oZtxmJn12shGrqRpVlHtzUGQKBgQCKxlzh61RObeLO2zHk+8j3Ow5gsxHaA/UQ4iFXS+1t5Lx+0SZDPrx/oHIuK1EiOsOLgdqQpHQpmQxbYpjs+S5pY1DyA8pjsq4X8XjxPhgfbgYuNHZF4Rg0VNUXxBOcXctQULweve2jhuau3F2L3AGQGonlwCln8ow1nhyiY5J4uwKBgG5X6pY5rMeKrb2DCNfA6JS0MkIUB0IsPtzsEPXrOWeSHseABAU27/1zMR7B6ni9s9b/pmJ90nZu2WCb61MHl/LN9DHOq++K9SqPAt+1YZvrHolcy6GxyumZcSssO6ZL1nnC39yaV6d3vlAoKQiNuwrzRN3RMjLHVeRt+b8LkisZAoGBAJUCb71Bz86sMZoTK7MpZyw2Iiy8yl0J3vH1Iy+2FnNRTQtmQv1sFe0oqeujfyYeTBTaiHUTgXKB6b+wEDR+kX5y++n/BfYtsrzbc2Kbqo1E3VkWAL3KWCTPVUgkOudKknknFYjAMqUrBAWpKKgh84g+OYuqaz/vD+gbuqi1Su5x";
    // 支付宝公钥
    String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnG/+aETgNe1wzbu7DQR8vj8PAWEWnmRb6F636FrdkUcNQdbsohYF2oBJDr1gf6kyeUTgYZRzAqqcjGdaGDQciUCWACrLbTY5EWUIZXxBvw4L2PhCp4+lrNzkhwFCt89BlGn3L0GQfP/neZzkNVhospBRwusW52XZcRPicT58KBqC0syd77JhvpYgkHvsBe4p+GeSeW9Ujf9nD8MyvynkH4QU1aubHMjUUu4xCgX1WqYPvCfLx95N091mfDGNeDiEqw4Dr1bdy8pMelYU3jetfdPmc5gp3mymx1cpMPU60aAaQP5abvWoWtyikD+YbfSXS1DzpBrjTzNuvu6PRCCw7wIDAQAB";
    String CHARSET = "utf-8";
    // 支付宝接口网关地址
//    String serverUrl = "https://openapi.alipaydev.com/gateway.do";
    String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 签名算法类型
    String signType = "RSA2";

    @GetMapping("/alipaytest")
    public void alipayTest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        // 构造sdk客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, "json", "GBK", ALIPAY_PUBLIC_KEY, signType);
        //创建API对应的request
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();

        //填充业务参数
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150320010101004\"," +
                " \"total_amount\":\"88.88\"," +
                " \"subject\":\"Iphone6 16G\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");
        String form = "";
        try {
            // 请求支付宝的下单接口
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

//        /******必传参数******/
//        JSONObject bizContent = new JSONObject();
////商户订单号，商家自定义，保持唯一性
//        bizContent.put("out_trade_no", "20210817010101004");
////支付金额，最小值0.01元
//        bizContent.put("total_amount", 0.01);
////订单标题，不可使用特殊符号
//        bizContent.put("subject", "测试商品");
//
///******可选参数******/
////手机网站支付默认传值FAST_INSTANT_TRADE_PAY
//        bizContent.put("product_code", "QUICK_WAP_WAY");
////bizContent.put("time_expire", "2022-08-01 22:00:00");
//
////// 商品明细信息，按需传入
////JSONArray goodsDetail = new JSONArray();
////JSONObject goods1 = new JSONObject();
////goods1.put("goods_id", "goodsNo1");
////goods1.put("goods_name", "子商品1");
////goods1.put("quantity", 1);
////goods1.put("price", 0.01);
////goodsDetail.add(goods1);
////bizContent.put("goods_detail", goodsDetail);
//
////// 扩展信息，按需传入
////JSONObject extendParams = new JSONObject();
////extendParams.put("sys_service_provider_id", "2088511833207846");
////bizContent.put("extend_params", extendParams);
//
//        request.setBizContent(bizContent.toString());
//        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
//        if(response.isSuccess()){
//            System.out.println("调用成功");
//        } else {
//            System.out.println("调用失败");
//        }
}
