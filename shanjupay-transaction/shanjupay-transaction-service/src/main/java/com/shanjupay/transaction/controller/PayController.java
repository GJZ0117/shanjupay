package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.IPUtil;
import com.shanjupay.common.util.ParseURLPairUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.vo.OrderConfirmVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 支付相关接口
 */
@Controller
@Slf4j
public class PayController {

    @Autowired
    TransactionService transactionService;

    @Reference
    AppService appService;

    /**
     * 支付入口
     *
     * @param ticket  传入数据，对json数据进行base64编码
     * @param request
     * @return
     */
    @RequestMapping(value = "/pay-entry/{ticket}")
    public String payEntry(@PathVariable("ticket") String ticket, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 准备确认页面所需要的数据
        String jsonString = EncryptUtil.decodeUTF8StringBase64(ticket);
        // 将ticket(json)转为对象
        PayOrderDTO payOrderDTO = JSON.parseObject(jsonString, PayOrderDTO.class);
        log.info("payOrderDTO:{}", payOrderDTO);
        // 将对象的属性和值组成一个url的key value串
        String params = ParseURLPairUtil.parseURLPair(payOrderDTO);
        // 解析客户端类型(微信、支付宝)
        // 得到客户端类型
        BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));
        switch (browserType) {
            case ALIPAY:
                // totalAmount属性不为空，固定金额支付，跳过pay-page页面直接调用支付宝支付接口
                if (payOrderDTO.getTotalAmount()!=null) {
                    System.out.println("payOrderDTO totalAmount is not null");
                    String appId = payOrderDTO.getAppId(); // 应用id
                    AppDTO app = appService.getAppById(appId);
                    payOrderDTO.setMerchantId(app.getMerchantId()); // 商户id
                    payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(payOrderDTO.getTotalAmount().longValue()))); // 将前端输入的元转为分
                    payOrderDTO.setClientIp(IPUtil.getIpAddr(request)); // 客户端ip
                    // 保存订单,调用支付渠道代理服务来实现第三方支付宝下单
                    PaymentResponseDTO paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);
                    String content = String.valueOf(paymentResponseDTO.getContent());
                    log.info("controller支付宝H5支付响应的结果: {}", content);
                    // 向前端响应html页面
                    response.setContentType("text/html;charset=UTF‐8");
                    response.getWriter().write(content);//直接将完整的表单html输出到页面
                    response.getWriter().flush();
                    response.getWriter().close();
                    return "";
                }
                // totalAmount值为空，转发到输入金额的前端页面
                return "forward:/pay-page?" + params;
            case WECHAT:
                // 先获取授权码 申请openid 再到支付确认页面
                return transactionService.getWXOAuth2Code(payOrderDTO);
            default:
        }
        // 客户端类型不支持，转发到错误页面
        return "forward:/pay-page-error";
    }

    /**
     * 支付宝下单接口,将前端订单确认页面的参数请求进来，点击确认支付，请求进来
     *
     * @param orderConfirmVO 订单信息
     * @param request
     * @param response
     */
    @ApiOperation("支付宝门店下单付款")
    @PostMapping("/createAliPayOrder")
    public void createAlipayOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        String appId = payOrderDTO.getAppId(); // 应用id
        AppDTO app = appService.getAppById(appId);
        payOrderDTO.setMerchantId(app.getMerchantId()); // 商户id
        payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount()).toString())); // 将前端输入的元转为分
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request)); // 客户端ip

        // 保存订单,调用支付渠道代理服务来实现第三方支付宝下单
        PaymentResponseDTO paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);
        String content = String.valueOf(paymentResponseDTO.getContent());
        log.info("支付宝H5支付响应的结果: {}", content);
        // 向前端响应html页面
        response.setContentType("text/html;charset=UTF‐8");
        response.getWriter().write(content);//直接将完整的表单html输出到页面
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * 微信授权码回调,申请获取授权码，微信将授权码请求到此地址
     *
     * @param code  授权码
     * @param state 订单信息
     * @return
     */
    @ApiOperation("微信授权码回调")
    @GetMapping("/wx-oauth-code-return")
    public String wxOAuth2CodeReturn(@RequestParam String code, @RequestParam String state) throws Exception {
        String jsonString = EncryptUtil.decodeUTF8StringBase64(state);
        PayOrderDTO payOrderDTO = JSON.parseObject(jsonString, PayOrderDTO.class);
        String appId = payOrderDTO.getAppId(); // 闪聚平台应用id
        // 获取openid
        String openId = transactionService.getWXOAuthOpenId(code, appId);
        // 将对象的属性和值组成一个url的key value串
        String params = ParseURLPairUtil.parseURLPair(payOrderDTO);
        // 重定向到支付确认页面
        String url = String.format("forward:/pay-page?openid=%s&%s", openId, params);
        return url;
    }

    @ApiOperation("微信门店下单付款")
    @PostMapping("/wxjspay")
    public ModelAndView createWXOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request) {
        if (StringUtils.isBlank(orderConfirmVO.getOpenId())) {
            throw new BusinessException(CommonErrorCode.E_300002);
        }
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        String appId = payOrderDTO.getAppId(); //应用id
        AppDTO app = appService.getAppById(appId); //商户id
        payOrderDTO.setMerchantId(app.getMerchantId()); //客户端ip
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request)); //将前端输入的元转成分
        payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount())));
        //调用微信下单接口
        Map<String, String> jsapiResponse = transactionService.submitOrderByWechat(payOrderDTO);
        log.info("/wxjspay 微信门店下单接口响应内容:{}", jsapiResponse);
        return new ModelAndView("wxpay", jsapiResponse);
    }

}
