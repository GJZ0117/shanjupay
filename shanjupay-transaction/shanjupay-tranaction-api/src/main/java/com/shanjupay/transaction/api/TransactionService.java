package com.shanjupay.transaction.api;

import com.google.zxing.qrcode.encoder.QRCode;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDTO;
import com.shanjupay.transaction.api.vo.DealVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 交易相关服务接口
 */
public interface TransactionService {

    /**
     * 生成门店二维码url
     *
     * @param qrCodeDTO 传入merchantId appId channel subject body
     * @return 支付入口(url) 要携带参数（将传入的参数转为json 用base64编码）
     * @throws BusinessException
     */
    String createStoreQRCode(QRCodeDTO qrCodeDTO) throws BusinessException;

    /**
     * 支付宝订单保存:保存订单到闪聚平台，调用支付渠道代理服务调用支付宝接口
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 根据订单号查询订单信息
     *
     * @param tradeNo
     * @return
     * @throws BusinessException
     */
    public PayOrderDTO queryPayOrder(String tradeNo) throws BusinessException;

    /**
     * 更新订单支付状态
     *
     * @param tradeNo           支付宝或微信交易流水号（第三方支付系统订单）
     * @param payChannelTradeNo 闪聚平台订单号
     * @param state             订单状态(0订单生成 1支付中 2支付成功 4关闭 5失败)
     * @throws BusinessException
     */
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) throws BusinessException;

    /**
     * 获取微信授权码
     *
     * @param payOrderDTO 订单对象
     * @return 返回申请授权码的地址
     * @throws BusinessException
     */
    public String getWXOAuth2Code(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 获取微信openid
     *
     * @param code  授权id
     * @param appId 闪聚平台应用id，用于获取微信支付渠道参数
     * @return openid
     */
    public String getWXOAuthOpenId(String code, String appId);

    /**
     * 微信确认支付 保存订单到闪聚平台 调用支付渠道代理服务调用微信接口
     *
     * @param payOrderDTO
     * @return 微信下单接口的响应数据
     * @throws BusinessException
     */
    Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 根据商户id和分页参数查询订单列表
     *
     * @param merchantId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BusinessException
     */
    List<PayOrderDTO> getAllOrderByMerchantId(Long merchantId, int pageNo, int pageSize) throws BusinessException;

    /**
     * 根据商户id查询订单总数
     *
     * @param merchantId
     * @return
     */
    Integer getPayOrderCountByMerchantId(Long merchantId);

    /**
     * 某商户下所有应用的交易汇总
     *
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    List<DealVo> getDealCollect(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 商户下所有交易按照应用分类汇总
     *
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    List<DealVo> getAllDealFromAppType(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 商户下所有交易按渠道汇总
     *
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    List<DealVo> getAllDealFromChannel(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);
}
