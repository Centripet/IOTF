package org.iotf.collectanalyzeservice.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Measurement(name = "device_energy_raw")
public class EnergyDataPoint {

    @Column(tag = true)
    private String deviceId;

    @Column(tag = true)
    private String deviceType;

    @Column
    private Double current;

    @Column
    private Double voltage;

    @Column
    private Double power;

    @Column
    private Double energy;

    @Column
    private Boolean isOn;

    @Column(timestamp = true)
    private Instant timestamp;
}
