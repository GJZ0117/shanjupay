package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component // 解决autowired报错Autowired members must be defined in valid Spring bean
@org.apache.dubbo.config.annotation.Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;

    @Autowired
    StoreMapper storeMapper;

    @Autowired
    StaffMapper staffMapper;

    @Autowired
    StoreStaffMapper storeStaffMapper;

    @Reference
    TenantService tenantService;

    // 根据ID查询商户
    @Override
    public MerchantDTO queryMerchantById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 根据租户id查询商户信息
     *
     * @param tenantId
     * @return
     * @throws BusinessException
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 注册商户服务接口，接收账号、密码、手机号，为了可扩展性使用merchantDTO接收数据
     * 调用SaaS接口新增租户、用户、绑定租户和用户的关系，初始化权限
     *
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        // 校验参数合法性
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 校验手机号是否为空
        if (merchantDTO.getMobile() == null) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        // 校验密码是否为空
        if (merchantDTO.getPassword() == null) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        // 校验手机号格式
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        // 校验手机号唯一性
        // 根据手机号查询商户表
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile()));
        if (count > 0) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        // 调用SaaS接口
        // 构造调用参数
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant"); // 租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant"); // 套餐,根据套餐分配权限
        createTenantRequestDTO.setName(merchantDTO.getUsername()); // 租户名和账号名一样
        // 如果租户在SaaS已存在，直接返回此租户信息，否则进行添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        // 获取租户id
        if (tenantAndAccount == null || tenantAndAccount.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        Long tenantId = tenantAndAccount.getId();

        // 根据租户id从商户表查询,如果存在记录则不允许添加商户(租户id在商户表唯一)
        Integer tenantCount = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        if (tenantCount > 0) {
            throw new BusinessException(CommonErrorCode.E_200017);
        }

        // 使用mapStruct进行对象转换
        Merchant merchant = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        // 设置对应租户id
        merchant.setTenantId(tenantId);
        // 设置审核状态为0 未进行资质申请 2审核通过
        merchant.setAuditStatus("0");
        // 调用mapper向数据库写入记录
        merchantMapper.insert(merchant);

        // 新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId()); // 商户id
        storeDTO.setStoreStatus(true); // 门店状态
        StoreDTO store = createStore(storeDTO);
        // 新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchantDTO.getMobile()); // 手机号
        staffDTO.setUsername(merchantDTO.getUsername()); // 账号
        staffDTO.setStoreId(store.getId()); // 员工所属门店id
        staffDTO.setMerchantId(merchant.getId()); // 商户id
        staffDTO.setStaffStatus(true); // 员工状态 启用
        StaffDTO staff = createStaff(staffDTO);
        // 为门店设置管理员
        bindStaffToStore(store.getId(), staff.getId());

        // 将DTO中写入新增商户的id
        // merchantDTO.setId(merchant.getId());
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 资质申请接口
     *
     * @param merchantId  商户id
     * @param merchantDTO 资质申请信息
     * @throws BusinessException
     */
    @Override
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if (merchantId == null || merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 校验merchantId合法性，查询商户表，如果查不到记录认为id非法
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        // 将dto转为entity
        Merchant entity = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        // 将必要的参数设置到entity
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());
        entity.setAuditStatus("2"); // 1已申请待审核 2审核通过
        entity.setTenantId(merchant.getTenantId()); // 租户id
        // 调用mapper更新商户表
        merchantMapper.updateById(entity);
    }

    /**
     * 新增门店
     *
     * @param storeDTO 门店信息
     * @return 新增成功的门店信息
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("新增门店信息: {}", JSON.toJSONString(store));
        // 新增门店
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    /**
     * 新增员工
     *
     * @param staffDTO 员工信息
     * @return 新增成功的员工信息
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        // 参数合法性校验
        if (staffDTO == null || StringUtils.isBlank(staffDTO.getMobile())
                || StringUtils.isBlank(staffDTO.getUsername())
                || staffDTO.getStoreId() == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        // 在同一个商户下员工账号和手机号必须唯一
        Boolean existStaffByUsername = isExistStaffByUsername(staffDTO.getUsername(), staffDTO.getMerchantId());
        if (existStaffByUsername) {
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        Boolean existStaffByMobile = isExistStaffByMobile(staffDTO.getMobile(), staffDTO.getMerchantId());
        if (existStaffByMobile) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        Staff staff = StaffConvert.INSTANCE.dto2entity(staffDTO);
        staffMapper.insert(staff);
        return StaffConvert.INSTANCE.entity2dto(staff);
    }

    /**
     * 将员工和门店绑定(将员工设置为管理员)
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);
        storeStaff.setStoreId(storeId);
        storeStaffMapper.insert(storeStaff);
    }

    /**
     * 门店列表查询
     *
     * @param storeDTO 查询条件 必要参数：商户id
     * @param pageNo   页码
     * @param pageSize 每页记录数
     * @return
     */
    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) {
        // 分页条件
        Page<Store> page = new Page<>(pageNo, pageSize);
        // 查询条件拼接
        LambdaQueryWrapper<Store> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (storeDTO != null && storeDTO.getMerchantId() != null) {
            lambdaQueryWrapper.eq(Store::getMerchantId, storeDTO.getMerchantId());
        }
        if (storeDTO != null && storeDTO.getStoreName() != null) {
            lambdaQueryWrapper.eq(Store::getStoreName, storeDTO.getStoreName());
        }
        // 查询数据库
        IPage<Store> storeIPage = storeMapper.selectPage(page, lambdaQueryWrapper);
        List<Store> records = storeIPage.getRecords();
        // 将包含entity的list转为包含dto的list
        List<StoreDTO> storeDTOS = StoreConvert.INSTANCE.listentity2dto(records);
        return new PageVO(storeDTOS, storeIPage.getTotal(), pageNo, pageSize);
    }

    /**
     * 校验门店是否属于商户
     *
     * @param storeId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public Boolean queryStoreInMerchant(Long storeId, Long merchantId) throws BusinessException {
        Integer count = storeMapper.selectCount(new LambdaQueryWrapper<Store>()
                .eq(Store::getId, storeId)
                .eq(Store::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 查询商户下员工
     *
     * @param merchantId
     * @return
     */
    @Override
    public List<StaffDTO> getMemberData(Long merchantId) {
        List<Staff> staffList = staffMapper.selectList(new LambdaQueryWrapper<Staff>().eq(Staff::getMerchantId, merchantId));
        return StaffConvert.INSTANCE.listentity2dto(staffList);
    }

    /**
     * 新增门店
     *
     * @param staffIds
     * @param storeDTO
     */
    @Override
    public void addStore(List<String> staffIds, StoreDTO storeDTO) {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        storeMapper.insert(store);
        Long storeId = store.getId();
        for (String staffId : staffIds) {
            StoreStaff storeStaff = new StoreStaff();
            storeStaff.setStoreId(storeId);
            storeStaff.setStaffId(Long.parseLong(staffId));
            storeStaffMapper.insert(storeStaff);
        }
    }

    /**
     * 更新门店信息
     *
     * @param storeDTO
     */
    @Override
    public void updateStore(StoreDTO storeDTO) {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        storeMapper.updateById(store);
    }

    /**
     * 员工手机号在同一个商户下唯一性校验
     *
     * @param mobile
     * @param merchantId
     * @return
     */
    private Boolean isExistStaffByMobile(String mobile, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getMobile, mobile)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 员工账号在同一个商户下唯一性校验
     *
     * @param username
     * @param merchantId
     * @return
     */
    private Boolean isExistStaffByUsername(String username, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getUsername, username)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }
}
