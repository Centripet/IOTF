option task = {
  name: "iotf_device_energy_monthly",
  every: 1d,
  offset: 20m,
}

sourceBucket = "energy_agg"
targetBucket = "energy_agg"

base = (field) =>
  from(bucket: sourceBucket)
    |> range(start: -62d)
    |> filter(fn: (r) => r._measurement == "device_energy_daily")
    |> filter(fn: (r) => r._field == field)
    |> filter(fn: (r) => exists r.user_id and exists r.device_id)
    |> group(columns: ["device_id"])

energy =
  base(field: "energy_sum_wh")
    |> aggregateWindow(every: 1mo, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_monthly")

powerAvg =
  base(field: "power_avg_w")
    |> aggregateWindow(every: 1mo, fn: mean, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_monthly")

powerMax =
  base(field: "power_max_w")
    |> aggregateWindow(every: 1mo, fn: max, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_monthly")

sampleCount =
  base(field: "sample_count")
    |> aggregateWindow(every: 1mo, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_monthly")

union(tables: [energy, powerAvg, powerMax, sampleCount])
  |> to(bucket: targetBucket)
