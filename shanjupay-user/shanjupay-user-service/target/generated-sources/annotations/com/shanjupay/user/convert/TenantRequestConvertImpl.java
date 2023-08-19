package com.shanjupay.user.convert;

import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.entity.Tenant;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-08-13T20:20:06+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.20 (Homebrew)"
)
public class TenantRequestConvertImpl implements TenantRequestConvert {

    @Override
    public CreateTenantRequestDTO entity2dto(Tenant entity) {
        if ( entity == null ) {
            return null;
        }

        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();

        createTenantRequestDTO.setName( entity.getName() );
        createTenantRequestDTO.setTenantTypeCode( entity.getTenantTypeCode() );
        createTenantRequestDTO.setBundleCode( entity.getBundleCode() );

        return createTenantRequestDTO;
    }

    @Override
    public Tenant dto2entity(CreateTenantRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Tenant tenant = new Tenant();

        tenant.setName( dto.getName() );
        tenant.setTenantTypeCode( dto.getTenantTypeCode() );
        tenant.setBundleCode( dto.getBundleCode() );

        return tenant;
    }
}
