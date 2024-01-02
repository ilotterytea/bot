-- Your SQL goes here
CREATE TABLE "timers" (
  "id" SERIAL NOT NULL UNIQUE PRIMARY KEY,
  "name" VARCHAR NOT NULL,
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "messages" TEXT[] NOT NULL,
  "interval_sec" BIGINT NOT NULL,
  "last_executed_at" TIMESTAMP NOT NULL DEFAULT timezone('utc', now()),
  "is_enabled" BOOLEAN NOT NULL DEFAULT TRUE
);
