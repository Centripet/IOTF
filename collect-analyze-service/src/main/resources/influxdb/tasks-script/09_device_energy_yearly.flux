option task = {
  name: "iotf_device_energy_yearly",
  every: 1d,
  offset: 25m,
}

sourceBucket = "energy_agg"
targetBucket = "energy_agg"

base = (field) =>
  from(bucket: sourceBucket)
    |> range(start: -2y)
    |> filter(fn: (r) => r._measurement == "device_energy_monthly")
    |> filter(fn: (r) => r._field == field)
    |> filter(fn: (r) => exists r.user_id and exists r.device_id)
    |> group(columns: ["device_id"])

energy =
  base(field: "energy_sum_wh")
    |> aggregateWindow(every: 1y, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_yearly")

powerAvg =
  base(field: "power_avg_w")
    |> aggregateWindow(every: 1y, fn: mean, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_yearly")

powerMax =
  base(field: "power_max_w")
    |> aggregateWindow(every: 1y, fn: max, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_yearly")

sampleCount =
  base(field: "sample_count")
    |> aggregateWindow(every: 1y, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "device_energy_yearly")

union(tables: [energy, powerAvg, powerMax, sampleCount])
  |> to(bucket: targetBucket)
