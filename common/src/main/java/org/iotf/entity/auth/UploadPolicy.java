package org.iotf.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class UploadPolicy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String OSSAccessKeyId;
    private String policy;
    private String signature;
    private String key;
    private String host;
    private long expire;
}
