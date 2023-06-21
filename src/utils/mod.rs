/// Format the timestamp (in seconds) as a humanized timestamp.
/// <br>
/// Example:
/// + 86400 seconds is 1d0h as a string timestamp.
/// + 10 seconds is 10s as a string timestamp.
/// + 90 seconds is 1m30s as a string timestamp.
pub fn format_timestamp(timestamp: u64) -> String {
    let d = (timestamp as f64 / (60.0 * 60.0 * 24.0)).trunc() as u32;
    let h = (timestamp as f64 / (60.0 * 60.0) % 24.0).trunc() as u32;
    let m = (timestamp as f64 % (60.0 * 60.0) / 60.0).trunc() as u32;
    let s = (timestamp as f64 % 60.0).trunc() as u32;

    if d == 0 && h == 0 && m == 0 {
        format!("{}s", s)
    } else if d == 0 && h == 0 {
        format!("{}m{}s", m, s)
    } else if d == 0 {
        format!("{}h{}m", h, m)
    } else {
        format!("{}d{}h", d, h)
    }
}