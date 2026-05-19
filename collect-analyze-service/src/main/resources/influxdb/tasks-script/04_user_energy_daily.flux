option task = {
  name: "iotf_user_energy_daily",
  every: 1d,
  offset: 10m,
}

sourceBucket = "energy_agg"
targetBucket = "energy_agg"

base = (field) =>
  from(bucket: sourceBucket)
    |> range(start: -2d)
    |> filter(fn: (r) => r._measurement == "user_energy_hourly")
    |> filter(fn: (r) => r._field == field)
    |> filter(fn: (r) => exists r.user_id)
    |> group(columns: ["user_id"])

energy =
  base(field: "energy_sum_wh")
    |> aggregateWindow(every: 1d, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_daily")

powerAvg =
  base(field: "power_avg_w")
    |> aggregateWindow(every: 1d, fn: mean, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_daily")

powerMax =
  base(field: "power_max_w")
    |> aggregateWindow(every: 1d, fn: max, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_daily")

sampleCount =
  base(field: "sample_count")
    |> aggregateWindow(every: 1d, fn: sum, createEmpty: false)
    |> set(key: "_measurement", value: "user_energy_daily")

union(tables: [energy, powerAvg, powerMax, sampleCount])
  |> to(bucket: targetBucket)
