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
