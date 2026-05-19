option task = {
  name: "iotf_user_energy_hourly",
  every: 1h,
  offset: 5m,
}

sourceBucket = "energy"
targetBucket = "energy_agg"

base = (field) =>
  from(bucket: sourceBucket)
    |> range(start: -2h)
    |> filter(fn: (r) => r._measurement == "device_energy_raw")
    |> filter(fn: (r) => r._field == field)
    |> filter(fn: (r) => exists r.user_id)
    |> group(columns: ["user_id"])

energy =
  base(field: "energy")
    |> aggregateWindow(every: 1h, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_hourly")
    |> set(key: "_field", value: "energy_sum_wh")

powerAvg =
  base(field: "power")
    |> aggregateWindow(every: 1h, fn: mean, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_hourly")
    |> set(key: "_field", value: "power_avg_w")

powerMax =
  base(field: "power")
    |> aggregateWindow(every: 1h, fn: max, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_hourly")
    |> set(key: "_field", value: "power_max_w")

sampleCount =
  base(field: "energy")
    |> aggregateWindow(every: 1h, fn: count, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_hourly")
    |> set(key: "_field", value: "sample_count")

union(tables: [energy, powerAvg, powerMax, sampleCount])
  |> to(bucket: targetBucket)
