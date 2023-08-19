package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.transaction.api.vo.DealVo;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import com.shanjupay.merchant.vo.PayOrderVo;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Api(tags = "商户平台应用接口", description = "商户平台应用接口")
public class MerchantController {
    // 注入远程调用的接口
    @org.apache.dubbo.config.annotation.Reference
    private MerchantService merchantService;

    @org.apache.dubbo.config.annotation.Reference
    private TransactionService transactionService;

    // 将本地bean注入
    @Autowired
    SmsService smsService;

    @Autowired
    FileService fileService;

    @ApiOperation(value = "根据ID查询商户信息")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取登陆用户的商户信息")
    @GetMapping(value = "/my/merchants")
    public MerchantDTO getMyMerchantInfo() {
        // 从token中获取商户id
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.queryMerchantById(merchantId);
    }

    @ApiOperation(value = "获取手机验证码")
    @ApiImplicitParam(value = "手机号", name = "phone", required = true, dataType = "String", paramType = "query")
    @GetMapping(value = "/sms")
    public String getSmsCode(@RequestParam("phone") String phone) {
        return smsService.sendSms(phone);
    }

    @ApiOperation(value = "商户注册")
    @ApiImplicitParam(value = "商户注册信息", name = "MerchantRegisterVO", required = true, dataType = "MerchantRegisterVO", paramType = "body")
    @PostMapping(value = "/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO) {
        // 校验参数合法性
        if (merchantRegisterVO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 校验手机号是否为空
        if (merchantRegisterVO.getMobile() == null) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        // 校验手机号格式
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        // 校验验证码
        smsService.checkVerifyCode(merchantRegisterVO.getVerifiykey(), merchantRegisterVO.getVerifiyCode());
        // 向dto中写入商户注册信息
        // 使用map struct转换对象
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        // 调用dubbo服务接口
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    @ApiOperation(value = "上传证件照")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传的文件", required = true) @RequestParam("file") MultipartFile file) throws IOException {
        // 生成文件名 保证唯一性
        String originalFileName = file.getOriginalFilename();
        String suffix = originalFileName.substring(originalFileName.lastIndexOf(".") - 1); // 扩展名
        String fileName = UUID.randomUUID() + suffix;
        // 调用fileService上传文件 返回url地址
        return fileService.upload(file.getBytes(), fileName);
    }

    @ApiOperation(value = "资质申请")
    @ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true, dataType = "MerchantDetailVO", paramType = "body")
    @PostMapping(value = "/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantInfo) {
        // 解析token，取出当前登陆商户id
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantInfo);
        merchantService.applyMerchant(merchantId, merchantDTO);
    }

    @ApiOperation(value = "根据商户id和分页参数查询订单PayOrder")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true, dataType = "int", paramType = "query")
    })
    @GetMapping("/my/merchants/orders")
    public PayOrderVo getAllOrderByMerchantId(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        Long merchantId = SecurityUtil.getMerchantId();
        List<PayOrderDTO> payOrderDTOS = transactionService.getAllOrderByMerchantId(merchantId, pageNo, pageSize);
        PayOrderVo payOrderVo = new PayOrderVo();
        payOrderVo.setPayOrderDTOList(payOrderDTOS);
        payOrderVo.setTotal(transactionService.getPayOrderCountByMerchantId(merchantId));
        payOrderVo.setPageNo(pageNo);
        payOrderVo.setPageSize(pageSize);
        return payOrderVo;
    }

    @ApiOperation(value = "deal.vue汇总订单信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = true, dataType = "String", paramType = "query")
    })
    @GetMapping("/my/merchants/dealDetails")
    public List<List<DealVo>> getDealDetails(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        Long merchantId = SecurityUtil.getMerchantId();
        startTime = ("0".equals(startTime) ? "1970-01-01" : startTime) + "T00:00:00";
        endTime = ("0".equals(endTime) ? "2099-12-31" : endTime) + "T23:59:59";
        LocalDateTime startTimeParse = LocalDateTime.parse(startTime);
        LocalDateTime endTimeParse = LocalDateTime.parse(endTime);
        List<List<DealVo>> pageData = new ArrayList<>();
        List<DealVo> dealCollect1 = transactionService.getDealCollect(merchantId, startTimeParse, endTimeParse);
        pageData.add(dealCollect1);
        List<DealVo> dealCollect2 = transactionService.getAllDealFromChannel(merchantId, startTimeParse, endTimeParse);
        pageData.add(dealCollect2);
        List<DealVo> dealCollect3 = transactionService.getAllDealFromAppType(merchantId, startTimeParse, endTimeParse);
        pageData.add(dealCollect3);
        return pageData;
    }

    @ApiOperation("查询商户下员工")
    @PostMapping("/my/staffs/page")
    public List<StaffDTO> getMemberData() {
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.getMemberData(merchantId);
    }
}