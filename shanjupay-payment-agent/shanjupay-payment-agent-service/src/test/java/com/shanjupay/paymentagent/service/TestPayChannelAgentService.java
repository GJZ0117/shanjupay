package com.shanjupay.paymentagent.service;

import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestPayChannelAgentService {
    @Autowired
    PayChannelAgentService payChannelAgentService;

    @Test
    public void testQueryPayOrderByAli() {

        // 应用id
        String APP_ID = "9021000123612327";
        // 应用私钥
        String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCrUvQP1RFiqeVbBy8Lcp10ppETTXw4Sffw10IqsoF0xRlTEtandcWGQz/LXqUqLLxFf1PzXB16kcJeIJbRFJnUixBkWVWfPQOs2eRKtKk91ebPdziqHs4SQyvnh+Ymi0mkFJzhK7znxhOOcaAYakthpgqMF0QLQRLN+JGwF1qGemL6uD26keLhZxYFKIsAyGvwYvsrdQOk2vrcdQJWZjzTcR25m6X96BaCXtLsOuaoPq+uyORr9U4zdDZiTS4Nvi7uhePFK8em/Fe7Ylwqkh58sPXARXu3X76RPVjx2QAqDrAOaaJMPYrtH/zf/WV90Nf7AioS04anW3ccTsVsoJPvAgMBAAECggEATEymW0K1+m3VsmORpRSHYUMpSyJwFxWCMaV8V02mb92zuJakOHIv5OqgR68/+Xdw5baeKby1eDfgC1GSOU1mcQz54OcURdXDhECJ9mddFCfmIjFjcAhGGEYIEJYqfrH7uvJfJ7z15XcGkKKz/QuhxLxQ6DEYYba6bDauk3DjJQ4zJ/+6Jqjv5QzkMkKa7tFI5b4oyiWGwTZswFR0TdpX7NSYYarttrnZUTjiMK6VeGGVsX+6R2KYbjeFgU7ccijD8nWR1ttj3rpeMvtPIkd/R5h3i8Fgn1jQAfhEQTSnFCFAX+uBOxvI99Z6tDIg9FeXyOvqipjmeBbXf/Z8aVajsQKBgQDlNixgi7VMvr+O2UmpPAVCH9frhLxmGsTZXjd7SEIT8zB01uB8Ubq8b8g/au8Fv3R5xFWeRmt8plBGmfrme+IN9bsd1NB+BeH27Q6NqXBaxwWYXOICBZMq8Xkstj7oXQG5LDeB+KkLwI356pHgR7okEOqj2YQjt3upiQ6DZe/pRwKBgQC/WNSTEr/vd+kiFX0cDGJ5WZ3IWVBmHhiRym61vaBVQ78eWFAS7qyVgSUuQ9A/5B+LFO73CiyBVUlxqla8xt7AGkZ8ZhsKz2c4xZz/98WlcKTEitdhldwN7eDwle+AtFJ850Q6dXHiuEV4h0Qg25oZtxmJn12shGrqRpVlHtzUGQKBgQCKxlzh61RObeLO2zHk+8j3Ow5gsxHaA/UQ4iFXS+1t5Lx+0SZDPrx/oHIuK1EiOsOLgdqQpHQpmQxbYpjs+S5pY1DyA8pjsq4X8XjxPhgfbgYuNHZF4Rg0VNUXxBOcXctQULweve2jhuau3F2L3AGQGonlwCln8ow1nhyiY5J4uwKBgG5X6pY5rMeKrb2DCNfA6JS0MkIUB0IsPtzsEPXrOWeSHseABAU27/1zMR7B6ni9s9b/pmJ90nZu2WCb61MHl/LN9DHOq++K9SqPAt+1YZvrHolcy6GxyumZcSssO6ZL1nnC39yaV6d3vlAoKQiNuwrzRN3RMjLHVeRt+b8LkisZAoGBAJUCb71Bz86sMZoTK7MpZyw2Iiy8yl0J3vH1Iy+2FnNRTQtmQv1sFe0oqeujfyYeTBTaiHUTgXKB6b+wEDR+kX5y++n/BfYtsrzbc2Kbqo1E3VkWAL3KWCTPVUgkOudKknknFYjAMqUrBAWpKKgh84g+OYuqaz/vD+gbuqi1Su5x";
        // 支付宝公钥
        String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnG/+aETgNe1wzbu7DQR8vj8PAWEWnmRb6F636FrdkUcNQdbsohYF2oBJDr1gf6kyeUTgYZRzAqqcjGdaGDQciUCWACrLbTY5EWUIZXxBvw4L2PhCp4+lrNzkhwFCt89BlGn3L0GQfP/neZzkNVhospBRwusW52XZcRPicT58KBqC0syd77JhvpYgkHvsBe4p+GeSeW9Ujf9nD8MyvynkH4QU1aubHMjUUu4xCgX1WqYPvCfLx95N091mfDGNeDiEqw4Dr1bdy8pMelYU3jetfdPmc5gp3mymx1cpMPU60aAaQP5abvWoWtyikD+YbfSXS1DzpBrjTzNuvu6PRCCw7wIDAQAB";
        String CHARSET = "utf-8";
        // 支付宝接口网关地址
        String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        // 签名算法类型
        String signType = "RSA2";

        AliConfigParam aliConfigParam = new AliConfigParam();
        aliConfigParam.setAppId(APP_ID);
        aliConfigParam.setRsaPrivateKey(APP_PRIVATE_KEY);
        aliConfigParam.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        aliConfigParam.setCharest(CHARSET);
        aliConfigParam.setUrl(serverUrl);
        aliConfigParam.setSigntype(signType);
        aliConfigParam.setFormat("json");

        PaymentResponseDTO paymentResponseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam, "SJ1689291596442669056");
        System.out.println(paymentResponseDTO);
    }
}
