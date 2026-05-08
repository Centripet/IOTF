package org.iotf.requestFormation.auth.res;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TGroupMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long message_id;
    private Long sender_id;
    private Long group_id;
    private String content;
    private int type;
    private LocalDateTime create_time;
    private Boolean is_read;

}
