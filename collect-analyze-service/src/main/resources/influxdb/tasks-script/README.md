# IOTF InfluxDB aggregation tasks

These Flux task scripts generate report source data from raw device points.

- Source bucket: `energy`
- Target bucket: `energy_agg`
- Raw measurement: `device_energy_raw`
- Device aggregate measurements: `device_energy_hourly`, `device_energy_daily`, `device_energy_weekly`, `device_energy_monthly`, `device_energy_yearly`
- User aggregate measurements: `user_energy_hourly`, `user_energy_daily`, `user_energy_weekly`, `user_energy_monthly`, `user_energy_yearly`

Create the target bucket before enabling tasks:

```bash
influx bucket create --name energy_agg --org <org>
```

Create a task from a script:

```bash
influx task create --org <org> --file collect-analyze-service/src/main/resources/influxdb/tasks-script/01_device_energy_hourly.flux
```

The UI-importable JSON files are in:

```text
collect-analyze-service/src/main/resources/influxdb/tasks-json
```

They use the InfluxDB template format:

```json
[
  {
    "apiVersion": "influxdata.com/v2alpha1",
    "kind": "Task",
    "metadata": {
      "name": "iotf-device-energy-hourly"
    },
    "spec": {
      "every": "1h0s",
      "name": "iotf_device_energy_hourly",
      "offset": "5m0s",
      "query": "..."
    }
  }
]
```

Device aggregation tasks group only by `device_id`; user aggregation tasks group only by `user_id`.

Query one device's daily report source data:

```flux
from(bucket: "energy_agg")
  |> range(start: -30d)
  |> filter(fn: (r) => r._measurement == "device_energy_daily")
  |> filter(fn: (r) => r.device_id == "20001")
```

Query one user's total daily report source data:

```flux
from(bucket: "energy_agg")
  |> range(start: -30d)
  |> filter(fn: (r) => r._measurement == "user_energy_daily")
  |> filter(fn: (r) => r.user_id == "10001")
```

The `energy_sum_wh` field is the main report energy value. Power fields are supporting data for trend and peak-load analysis.
