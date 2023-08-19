package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.QRCodeUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.QRCodeDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 门店管理相关接口定义
 */

@Api(value = "商户平台-门店管理", tags = "商户平台-门店管理", description = "商户平台-门店的增删改查")
@RestController
@Slf4j
public class StoreController {

    @Value("${shanjupay.c2b.subject}")
    private String subject;

    @Value("${shanjupay.c2b.body}")
    private String body;

    @Reference
    MerchantService merchantService;

    @Reference
    TransactionService transactionService;

    @ApiOperation("门店列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true, dataType = "int", paramType = "query")
    })
    @PostMapping("/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        // 商户id
        Long merchantId = SecurityUtil.getMerchantId();
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchantId);

        PageVO<StoreDTO> storeDTOS = merchantService.queryStoreByPage(storeDTO, pageNo, pageSize);
        return storeDTOS;
    }

    @ApiOperation("生成商户应用门店二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "商户应用id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "storeId", value = "商户门店id", required = true, dataType = "String", paramType = "path"),
    })
    @GetMapping(value = "/my/apps/{appId}/stores/{storeId}/app-store-qrcode")
    public String createCScanBStoreQRCode(@PathVariable("appId") String appId, @PathVariable("storeId") Long storeId, @RequestParam("body") String itemMsg, @RequestParam("totalAmount") Integer totalAmount) throws BusinessException, IOException {
        // 获取商户id
        Long merchantId = SecurityUtil.getMerchantId();
        // 商户信息
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        QRCodeDTO qrCodeDTO = new QRCodeDTO();
        qrCodeDTO.setMerchantId(merchantId);
        qrCodeDTO.setStoreId(storeId);
        qrCodeDTO.setAppId(appId);

        // 设置支付商品信息
        if (totalAmount != null) { // 固定金额支付
            qrCodeDTO.setTotalAmount(totalAmount);
            qrCodeDTO.setSubject(itemMsg);
            qrCodeDTO.setBody(itemMsg);
        } else { // 自定义输入金额支付
            // 标题和内容(用商户名称替换%s)
            String subjectFormat = String.format(subject, merchantDTO.getMerchantName());
            qrCodeDTO.setSubject(subjectFormat);
            String bodyFormat = String.format(body, merchantDTO.getMerchantName());
            qrCodeDTO.setBody(bodyFormat);
        }

        // 获取二维码url
        String storeQRCodeUrl = transactionService.createStoreQRCode(qrCodeDTO);
        // 调用工具类生成二维码图片
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        // 二维码图片base64编码
        String qrCode = qrCodeUtil.createQRCode(storeQRCodeUrl, 200, 200);
        return qrCode;
    }

    /**
     * 新增门店
     *
     * @param staffIds
     * @param storeDTO
     */
    @PostMapping(value = "/my/createStore")
    public void createStore(@RequestParam("staffIds") List<String> staffIds, @RequestBody StoreDTO storeDTO) {
        Long merchantId = SecurityUtil.getMerchantId();
        storeDTO.setMerchantId(merchantId);
        storeDTO.setStoreStatus(true);
        merchantService.addStore(staffIds, storeDTO);
    }

    @PostMapping(value = "/my/updateStore")
    public void updateStore(@RequestBody StoreDTO storeDTO) {
        Long merchantId = SecurityUtil.getMerchantId();
        storeDTO.setMerchantId(merchantId);
        merchantService.updateStore(storeDTO);
    }


}
