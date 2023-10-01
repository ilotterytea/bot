pub fn format_timestamp(timestamp_in_seconds: u64) -> String {
    let timestamp_as_f64 = timestamp_in_seconds as f64;
    let days = (timestamp_as_f64 / (60.0 * 60.0 * 24.0)).trunc();
    let hours = (timestamp_as_f64 / (60.0 * 60.0) % 24.0).trunc();
    let minutes = (timestamp_as_f64 % (60.0 * 60.0) / 60.0).trunc();
    let seconds = (timestamp_as_f64 % 60.0).trunc();

    if days == 0.0 && hours == 0.0 && minutes == 0.0 {
        format!("{}s", seconds)
    } else if days == 0.0 && hours == 0.0 {
        format!("{}m{}s", minutes, seconds)
    } else if days == 0.0 {
        format!("{}h{}m", hours, minutes)
    } else {
        format!("{}d{}h", days, hours)
    }
}
