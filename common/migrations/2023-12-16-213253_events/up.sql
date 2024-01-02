-- Your SQL goes here
CREATE TYPE "event_type" AS ENUM ('live', 'offline', 'title', 'category', 'custom');
CREATE TYPE "event_flag" AS ENUM ('massping');

CREATE TABLE IF NOT EXISTS "events" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "target_alias_id" INTEGER,
  "custom_alias_id" VARCHAR,
  "event_type" event_type NOT NULL,
  "flags" event_flag[] NOT NULL DEFAULT ARRAY[]::event_flag[],
  "message" VARCHAR NOT NULL,

  CONSTRAINT check_target_alias_id CHECK (("target_alias_id" IS NULL AND "custom_alias_id" IS NOT NULL) OR ("target_alias_id" IS NOT NULL AND "custom_alias_id" IS NULL)),
  CONSTRAINT check_event_type CHECK (("target_alias_id" IS NOT NULL AND "event_type" != 'custom') OR ("custom_alias_id" IS NOT NULL AND "event_type" = 'custom'))
);

CREATE TABLE IF NOT EXISTS "event_subscriptions" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "event_id" INTEGER NOT NULL REFERENCES "events"("id"),
  "user_id" INTEGER NOT NULL REFERENCES "users"("id")
);
