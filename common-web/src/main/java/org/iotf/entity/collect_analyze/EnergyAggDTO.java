package org.iotf.entity.collect_analyze;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyAggDTO {

    private Instant time;

    private Long user_id;

    private Long device_id;

    private String period;

    private Double energy_sum_wh;

    private Double power_avg_w;

    private Double power_max_w;

    private Long sample_count;
}
