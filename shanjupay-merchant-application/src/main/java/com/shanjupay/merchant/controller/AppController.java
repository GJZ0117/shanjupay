package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商户平台-应用管理", tags = "商户平台-应用相关", description = "商户平台-应用相关")
@RestController
public class AppController {
    @Reference
    private AppService appService;

    @ApiOperation(value = "商户创建应用")
    @ApiImplicitParam(name = "app", value = "应用信息", required = true, dataType = "AppDTO", paramType = "body")
    @PostMapping("/my/apps")
    public AppDTO creatApp(@RequestBody AppDTO appDTO) {
        // 从token中解析商户id
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.createApp(merchantId, appDTO);
    }

    @ApiOperation(value = "查询商户下的应用列表")
    @GetMapping(value = "/my/apps")
    public List<AppDTO> queryMyApps() {
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.queryAppByMerchant(merchantId);
    }

    @ApiOperation(value = "根据appid获取应用详细信息")
    @ApiImplicitParam(name = "appId", value = "商户应用id", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/my/apps/{appId}")
    public AppDTO getApp(@PathVariable("appId") String appId) {
        return appService.getAppById(appId);
    }
}
