use diesel::pg::PgConnection;
use diesel::prelude::*;
use std::env;

pub mod twitch;

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

/// Establish connection to the database.
pub fn establish_connection() -> PgConnection {
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");

    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Error conencting to {}", database_url))
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
