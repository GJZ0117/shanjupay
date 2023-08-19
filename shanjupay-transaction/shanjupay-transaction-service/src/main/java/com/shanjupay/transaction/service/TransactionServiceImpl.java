package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.qrcode.encoder.QRCode;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.IPUtil;
import com.shanjupay.common.util.PaymentUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDTO;
import com.shanjupay.transaction.api.vo.DealVo;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.apache.dubbo.config.annotation.Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    // 从配置文件读取支付入口url
    @Value("${shanjupay.payurl}")
    String payurl;

    @Reference
    AppService appService;

    @Reference
    MerchantService merchantService;

    @Autowired
    PayOrderMapper payOrderMapper;

    @Reference
    PayChannelAgentService payChannelAgentService;

    @Autowired
    PayChannelService payChannelService;

    @Autowired
    AppPlatformChannelMapper appPlatformChannelMapper;

    /**
     * 生成门店二维码url
     *
     * @param qrCodeDTO 传入merchantId appId channel subject body
     * @return 支付入口(url) 要携带参数（将传入的参数转为json 用base64编码）
     * @throws BusinessException
     */
    @Override
    public String createStoreQRCode(QRCodeDTO qrCodeDTO) throws BusinessException {
        // 校验商户id、应用id、门店id合法性(校验应用是否属于商户,门店是否属于商户)
        verifyAppAndStore(qrCodeDTO.getMerchantId(), qrCodeDTO.getAppId(), qrCodeDTO.getStoreId());
        // 组装数据
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDTO.getMerchantId());
        payOrderDTO.setAppId(qrCodeDTO.getAppId());
        payOrderDTO.setStoreId(qrCodeDTO.getStoreId());
        payOrderDTO.setSubject(qrCodeDTO.getSubject());//显示订单标题
        payOrderDTO.setChannel("shanju_c2b");//服务类型c扫b
        payOrderDTO.setBody(qrCodeDTO.getBody());//订单内容
        payOrderDTO.setTotalAmount(qrCodeDTO.getTotalAmount()); // test
        log.info("TransactionServiceImpl.payOrderDTO: {}", payOrderDTO);
        String jsonString = JSON.toJSONString(payOrderDTO); // 转成json
        // base64编码
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        // 目标生成一个支付入口url 需要携带url 将传入的参数转为json 用base64编码
        String url = payurl + ticket;
        return url;
    }

    /**
     * 支付宝订单保存:保存订单到闪聚平台，调用支付渠道代理服务，调用支付宝下单接口
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException {
        // 保存订单到闪聚平台
        payOrderDTO.setChannel("ALIPAY_WAP"); // 支付渠道
        payOrderDTO = save(payOrderDTO);

        // 调用支付渠道代理服务，调用支付宝下单接口
        return alipayH5(payOrderDTO.getTradeNo());
    }

    /**
     * 根据订单号查询订单信息
     *
     * @param tradeNo
     * @return
     * @throws BusinessException
     */
    @Override
    public PayOrderDTO queryPayOrder(String tradeNo) throws BusinessException {
        PayOrder payOrder = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getTradeNo, tradeNo));
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }

    /**
     * 更新订单支付状态
     *
     * @param tradeNo           支付宝或微信交易流水号（第三方支付系统订单）
     * @param payChannelTradeNo 闪聚平台订单号
     * @param state             订单状态(0订单生成 1支付中 2支付成功 4关闭 5失败)
     * @throws BusinessException
     */
    @Override
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) throws BusinessException {
        LambdaUpdateWrapper<PayOrder> payOrderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        payOrderLambdaUpdateWrapper.eq(PayOrder::getTradeNo, tradeNo)
                .set(PayOrder::getTradeState, state)
                .set(PayOrder::getPayChannelTradeNo, payChannelTradeNo);
        if (state != null && "2".equals(state)) {
            payOrderLambdaUpdateWrapper.set(PayOrder::getPaySuccessTime, LocalDateTime.now());
        }
        payOrderMapper.update(null, payOrderLambdaUpdateWrapper);
    }


    @Value("${weixin.oauth2RequestUrl}")
    private String wxOAuth2RequestUrl;
    @Value("${weixin.oauth2CodeReturnUrl}")
    private String wxOAuth2CodeReturnUrl;
    @Value("${weixin.oauth2Token}")
    private String wxOAuth2Token;

    /**
     * 获取微信授权码
     *
     * @param payOrderDTO 订单对象
     * @return 返回申请授权码的地址
     * @throws BusinessException
     */
    @Override
    public String getWXOAuth2Code(PayOrderDTO payOrderDTO) throws BusinessException {
        String appId = payOrderDTO.getAppId(); // 闪聚平台应用id
        // 获取微信支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, "shanju_c2b", "WX_JSAPI");
        String param = payChannelParamDTO.getParam();
        // 微信支付渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);
        // state是一个原样返回的参数
        String state = EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(payOrderDTO));
        try {
            String url = String.format("%s?appid=%s&scope=snsapi_base&state=%s#wechat_redirect", wxOAuth2RequestUrl, wxConfigParam.getAppId(), wxOAuth2CodeReturnUrl, state);
            return "redirect:" + url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "forward:/pay-page-error";
    }

    /**
     * 获取微信openid
     *
     * @param code  授权id
     * @param appId 闪聚平台应用id，用于获取微信支付渠道参数
     * @return openid
     */
    @Override
    public String getWXOAuthOpenId(String code, String appId) {
        //获取微信支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, "shanju_c2b", "WX_JSAPI");
        String param = payChannelParamDTO.getParam();
        //微信支付渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);
        String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code", wxOAuth2Token, wxConfigParam.getAppId(), wxConfigParam.getAppSecret(), code);
        //申请openid，请求url
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //申请openid接口响应的内容，其中包括了openid
        String body = exchange.getBody();
        log.info("申请openid响应的内容:{}", body);
        //获取openid
        String openid = JSON.parseObject(body).getString("openid");
        return openid;
    }

    /**
     * 微信确认支付 保存订单到闪聚平台 调用支付渠道代理服务调用微信接口
     *
     * @param payOrderDTO
     * @return 微信下单接口的响应数据
     * @throws BusinessException
     */
    @Override
    public Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException {
        // 支付渠道
        payOrderDTO.setChannel("WX_JSAPI");
        // 保存订单
        save(payOrderDTO);
        // 调用支付代理服务，调用微信下单接口

        return null;
    }

    /**
     * 根据商户id和分页参数查询订单列表
     *
     * @param merchantId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayOrderDTO> getAllOrderByMerchantId(Long merchantId, int pageNo, int pageSize) throws BusinessException {
        Page<PayOrder> page = new Page<>(pageNo, pageSize);
        IPage<PayOrder> payOrderIPage = payOrderMapper.selectPage(page,
                new LambdaQueryWrapper<PayOrder>()
                        .select(PayOrder::getPaySuccessTime, PayOrder::getTradeNo, PayOrder::getChannel, PayOrder::getSubject, PayOrder::getTotalAmount, PayOrder::getTradeState, PayOrder::getCurrency)
                        .eq(PayOrder::getMerchantId, merchantId)
                        .orderByDesc(PayOrder::getPaySuccessTime));
        List<PayOrder> payOrders = payOrderIPage.getRecords();
        List<PayOrderDTO> payOrderDTOS = PayOrderConvert.INSTANCE.listentity2dto(payOrders);
        return payOrderDTOS;
    }

    /**
     * 根据商户id查询订单总数
     *
     * @param merchantId
     * @return
     */
    @Override
    public Integer getPayOrderCountByMerchantId(Long merchantId) {
        return payOrderMapper.selectCount(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getMerchantId, merchantId));
    }

    /**
     * 某商户下所有应用的交易汇总
     *
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<DealVo> getDealCollect(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        DealVo dealVo = payOrderMapper.getDealCollect(merchantId, startTime, endTime);
        Double conversion = (double) dealVo.getSuccessNumber() / dealVo.getRequestNumber();
        dealVo.setConversion((new BigDecimal(conversion)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        List<DealVo> list = new ArrayList<>();
        list.add(dealVo);
        return list;
    }

    /**
     * 商户下所有交易按照应用分类汇总
     *
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<DealVo> getAllDealFromAppType(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        // 通过商户id查询其app列表
        List<AppDTO> appDTOS = appService.queryAppByMerchant(merchantId);
        List<DealVo> list = new ArrayList<>();
        for (AppDTO appDTO : appDTOS) {
            String appId = appDTO.getAppId();
            String appName = appDTO.getAppName();
            DealVo dealVo = payOrderMapper.getAllDealFromAppType(appId, startTime, endTime);
            dealVo.setAppName(appName);
            if (dealVo.getRequestNumber() != 0) {
                Double conversion = (double) dealVo.getSuccessNumber() / dealVo.getRequestNumber();
                dealVo.setConversion((new BigDecimal(conversion)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            list.add(dealVo);
        }
        return list;
    }

    /**
     * 商户下所有交易按渠道汇总
     *
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<DealVo> getAllDealFromChannel(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        // 通过商户id查询其app列表
        List<AppDTO> appDTOS = appService.queryAppByMerchant(merchantId);
        Map<String, DealVo> map = new HashMap<>();
        for (AppDTO appDTO : appDTOS) {
            String appId = appDTO.getAppId();
            // 通过商户的app中的appId查询其开通的支付渠道
            List<AppPlatformChannel> appPlatformChannels = appPlatformChannelMapper.selectList(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId));
            for (AppPlatformChannel appPlatformChannel : appPlatformChannels) {
                String channel = appPlatformChannel.getPlatformChannel().toUpperCase();
                // 根据appid、支付渠道、时间范围查询订单汇总情况
                DealVo dealVo = payOrderMapper.getAllDealFromChannel(appId, channel, startTime, endTime);
                dealVo.setChannelName(channel);
                map.put(channel, dealVo);
            }
        }

        List<DealVo> list = new ArrayList<>();
        for (DealVo dealVo : map.values()) {
            // 该渠道下没有支付订单，所有数据设为0
            if (dealVo.getTotalAmount() == null) {
                dealVo.setTotalAmount(0);
                dealVo.setSuccessAmount(0);
                dealVo.setRequestNumber(0);
                dealVo.setSuccessNumber(0);
                dealVo.setAvgAmount(0D);
                dealVo.setConversion(0D);
            } else { // 该渠道下有订单
                dealVo.setAvgAmount(Double.parseDouble(String.format("%.2f", (double)dealVo.getSuccessAmount()/dealVo.getSuccessNumber())));
                Double conversion = (double) dealVo.getSuccessNumber() / dealVo.getRequestNumber();
                dealVo.setConversion((new BigDecimal(conversion)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                System.out.println(dealVo);
            }
            list.add(dealVo);
        }
        return list;
    }

    // 调用支付渠道代理服务的支付宝下单接口
    private PaymentResponseDTO alipayH5(String tradeNo) throws BusinessException {
        // 从数据库查询订单信息
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        // 组装alipayBean
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOutTradeNo(payOrderDTO.getTradeNo()); // 订单号
        try {
            alipayBean.setTotalAmount(AmountUtil.changeF2Y(payOrderDTO.getTotalAmount().toString()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }
        alipayBean.setSubject(payOrderDTO.getSubject());
        alipayBean.setBody(payOrderDTO.getBody());
        alipayBean.setStoreId(payOrderDTO.getStoreId());
        alipayBean.setExpireTime("30m");

        // 从数据库查询支付渠道配置参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), "shanju_c2b", "ALIPAY_WAP");
        String paramJson = payChannelParamDTO.getParam();
        // 支付渠道参数
        AliConfigParam aliConfigParam = JSON.parseObject(paramJson, AliConfigParam.class);
        aliConfigParam.setCharest("utf-8");
        PaymentResponseDTO payOrderByAliWAP = payChannelAgentService.createPayOrderByAliWAP(aliConfigParam, alipayBean);
        return payOrderByAliWAP;
    }

    // 保存订单(公用，微信支付宝都调用这个)
    private PayOrderDTO save(PayOrderDTO payOrderDTO) throws BusinessException {
        PayOrder payOrder = PayOrderConvert.INSTANCE.dto2entity(payOrderDTO);
        payOrder.setTradeNo(PaymentUtil.genUniquePayOrderNo()); // 订单号 雪花算法
        payOrder.setCreateTime(LocalDateTime.now()); // 创建时间
        payOrder.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES)); // 订单过期时间 30min
        payOrder.setCurrency("CNY"); // 设置支付币种
        payOrder.setTradeState("0"); // 订单生成 还未支付
        payOrderMapper.insert(payOrder); // 插入订单
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }

    // 私有方法校验合法性
    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {
        // 根据应用id和商户id查询
        Boolean aBoolean = appService.queryAppInMerchant(appId, merchantId);
        if (!aBoolean) {
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        // 根据门店id和商户id查询
        Boolean aBoolean1 = merchantService.queryStoreInMerchant(storeId, merchantId);
        if (!aBoolean1) {
            throw new BusinessException(CommonErrorCode.E_200006);
        }
    }

    // 微信jsapi 调用支付渠道代理
    private Map<String, String> weChatJsapi(String openId, String tradeNo) {
        //根据订单号查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        if (payOrderDTO == null) {
            throw new BusinessException(CommonErrorCode.E_400002);
        }
        //构造微信订单参数实体
        WeChatBean weChatBean = new WeChatBean();
        weChatBean.setOpenId(openId);//openid
        weChatBean.setSpbillCreateIp(payOrderDTO.getClientIp());//客户ip
        weChatBean.setTotalFee(payOrderDTO.getTotalAmount());//金额
        weChatBean.setBody(payOrderDTO.getBody());//订单描述
        weChatBean.setOutTradeNo(payOrderDTO.getTradeNo());//使用聚合平台的订单号 tradeNo
        weChatBean.setNotifyUrl("none");//异步接收微信通知支付结果的地址(暂时不用)
        //根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), "shanju_c2b", "WX_JSAPI");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        // 微信渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), WXConfigParam.class);
        return payChannelAgentService.createPayOrderByWeChatJSAPI(wxConfigParam, weChatBean);
    }

}
