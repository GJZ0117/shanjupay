package com.shanjupay.paymentagent.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;

import java.util.Map;

/**
 * 与第三方支付渠道交互
 */
public interface PayChannelAgentService {

    /**
     * 调用支付宝下单接口
     *
     * @param aliConfigParam 支付渠道配置参数(支付宝必要参数)
     * @param alipayBean     业务订单参数(商户订单号、订单标题、订单描述...)
     * @return 统一返回PaymentResponseDTO
     * @throws BusinessException
     */
    public PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException;

    /**
     * 查询支付宝的订单状态
     *
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo     闪聚平台订单号
     * @return
     * @throws BusinessException
     */
    public PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo) throws BusinessException;

    /**
     * 微信jsapi下单接口请求
     *
     * @param wxConfigParam 微信支付渠道参数
     * @param weChatBean    订单业务数据
     * @return h5页面所需要的数据
     */
    public Map<String, String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean);


    /**
     * 查询微信支付结果
     *
     * @param wxConfigParam
     * @param outTradeNo
     * @return
     */
    public PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo);
}
