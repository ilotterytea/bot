ARG RUST_TAG=1.76

FROM rust:$RUST_TAG AS builder
  WORKDIR /tmp/ilotterytea/bot/api
  COPY . .

  RUN cargo build --release --package api

  EXPOSE 8085
