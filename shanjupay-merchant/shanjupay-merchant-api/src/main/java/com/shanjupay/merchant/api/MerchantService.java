package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

import java.util.List;

public interface MerchantService {

    // 根据ID查询商户
    public MerchantDTO queryMerchantById(Long id);


    /**
     * 根据租户id查询商户信息
     *
     * @param tenantId
     * @return
     * @throws BusinessException
     */
    MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException;

    /**
     * 注册商户服务接口，接收账号、密码、手机号，为了可扩展性使用merchantDTO接收数据
     *
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 资质申请接口
     *
     * @param merchantId  商户id
     * @param merchantDTO 资质申请信息
     * @throws BusinessException
     */
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 新增门店
     *
     * @param storeDTO 门店信息
     * @return 新增成功的门店信息
     * @throws BusinessException
     */
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;

    /**
     * 新增员工
     *
     * @param staffDTO 员工信息
     * @return 新增成功的员工信息
     * @throws BusinessException
     */
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;

    /**
     * 将员工和门店绑定(将员工设置为管理员)
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;

    /**
     * 门店列表查询
     *
     * @param storeDTO 查询条件 必要参数：商户id
     * @param pageNo   页码
     * @param pageSize 每页记录数
     * @return
     */
    PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize);

    /**
     * 校验门店是否属于商户
     *
     * @param storeId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    Boolean queryStoreInMerchant(Long storeId, Long merchantId) throws BusinessException;

    /**
     * 查询商户下员工
     *
     * @param merchantId
     * @return
     */
    List<StaffDTO> getMemberData(Long merchantId);

    /**
     * 新增门店
     *
     * @param staffIds
     * @param storeDTO
     */
    void addStore(List<String> staffIds, StoreDTO storeDTO);

    /**
     * 更新门店信息
     *
     * @param storeDTO
     */
    void updateStore(StoreDTO storeDTO);
}