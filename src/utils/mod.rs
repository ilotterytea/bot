use std::env;

pub mod diesel;

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

pub fn split_and_wrap_lines(
    init_string: &str,
    separator: &str,
    max_length_per_line: usize,
) -> Vec<String> {
    let input_lines = init_string.split(separator).collect::<Vec<&str>>();
    let mut output_lines: Vec<String> = Vec::new();
    let mut buffer_lines: Vec<String> = Vec::new();

    for line in input_lines {
        let buffer_string = buffer_lines.join(separator);

        if buffer_string.len() + line.len() + separator.len() >= max_length_per_line {
            output_lines.push(buffer_string);
            buffer_lines.clear();
        }

        buffer_lines.push(line.to_string());
    }

    output_lines.push(buffer_lines.join(separator));

    output_lines
}
