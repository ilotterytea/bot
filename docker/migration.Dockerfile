ARG RUST_TAG=1.76

FROM rust:$RUST_TAG AS builder
  WORKDIR /tmp/ilotterytea/bot/migrations
  COPY ./common/migrations .

  RUN cargo install diesel_cli --no-default-features --features postgres
