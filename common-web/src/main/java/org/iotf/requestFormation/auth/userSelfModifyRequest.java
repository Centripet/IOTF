package org.iotf.requestFormation.auth;

public record userSelfModifyRequest (
        String nick_name,
        Short sex,
        // icon:file_id
        Long icon
){
}
