package org.iotf.requestFormation.auth.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class uploadSubmit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String key;
    private String suffix;
    private String origin_name;
    private Integer type;
    private String title;
    private String info;


}
