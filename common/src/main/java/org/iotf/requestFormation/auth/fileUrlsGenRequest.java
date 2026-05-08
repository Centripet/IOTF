package org.iotf.requestFormation.auth;

import java.util.List;

public record fileUrlsGenRequest(
        List<Long> files_id
) {
}
