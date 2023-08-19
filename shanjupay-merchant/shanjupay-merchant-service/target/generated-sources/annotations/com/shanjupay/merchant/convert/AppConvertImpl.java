package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.entity.App;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-08-13T20:20:35+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.20 (Homebrew)"
)
public class AppConvertImpl implements AppConvert {

    @Override
    public AppDTO entity2dto(App entity) {
        if ( entity == null ) {
            return null;
        }

        AppDTO appDTO = new AppDTO();

        appDTO.setAppId( entity.getAppId() );
        appDTO.setAppName( entity.getAppName() );
        appDTO.setMerchantId( entity.getMerchantId() );
        appDTO.setPublicKey( entity.getPublicKey() );
        appDTO.setNotifyUrl( entity.getNotifyUrl() );

        return appDTO;
    }

    @Override
    public App dto2entity(AppDTO appDTO) {
        if ( appDTO == null ) {
            return null;
        }

        App app = new App();

        app.setAppId( appDTO.getAppId() );
        app.setAppName( appDTO.getAppName() );
        app.setMerchantId( appDTO.getMerchantId() );
        app.setPublicKey( appDTO.getPublicKey() );
        app.setNotifyUrl( appDTO.getNotifyUrl() );

        return app;
    }

    @Override
    public List<AppDTO> listEntity2dto(List<App> apps) {
        if ( apps == null ) {
            return null;
        }

        List<AppDTO> list = new ArrayList<AppDTO>( apps.size() );
        for ( App app : apps ) {
            list.add( entity2dto( app ) );
        }

        return list;
    }
}
