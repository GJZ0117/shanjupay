package com.shanjupay.user.convert;

import com.shanjupay.user.api.dto.tenant.AccountDTO;
import com.shanjupay.user.entity.Account;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-08-13T20:20:06+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.20 (Homebrew)"
)
public class AccountConvertImpl implements AccountConvert {

    @Override
    public AccountDTO entity2dto(Account entity) {
        if ( entity == null ) {
            return null;
        }

        AccountDTO accountDTO = new AccountDTO();

        accountDTO.setId( entity.getId() );
        accountDTO.setUsername( entity.getUsername() );
        accountDTO.setMobile( entity.getMobile() );
        accountDTO.setPassword( entity.getPassword() );
        accountDTO.setSalt( entity.getSalt() );

        return accountDTO;
    }

    @Override
    public Account dto2entity(AccountDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Account account = new Account();

        account.setId( dto.getId() );
        account.setUsername( dto.getUsername() );
        account.setMobile( dto.getMobile() );
        account.setPassword( dto.getPassword() );
        account.setSalt( dto.getSalt() );

        return account;
    }
}
