-- Your SQL goes here
CREATE TYPE "event_type" AS ENUM (
  'live',
  'offline',
  'title',
  'game',
  'custom'
);

CREATE TYPE "event_flag" AS ENUM (
  'massping'
);

CREATE TABLE "events" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "target_alias_id" INTEGER,
  "custom_alias_id" VARCHAR(500),
  "message" VARCHAR(500) NOT NULL,
  "event_type" event_type NOT NULL,
  "flags" event_flag[] NOT NULL DEFAULT ARRAY[]::event_flag[],

  CONSTRAINT check_target
    CHECK (
      ("target_alias_id" IS NULL AND "custom_alias_id" IS NOT NULL) OR
      ("target_alias_id" IS NOT NULL AND "custom_alias_id" IS NULL)
    ),

  CONSTRAINT check_type
    CHECK (
      ("target_alias_id" IS NOT NULL AND "event_type" != 'custom') OR
      ("custom_alias_id" IS NOT NULL AND "event_type" = 'custom')
    )
);

CREATE TABLE "event_subscriptions" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "event_id" INTEGER NOT NULL REFERENCES "events"("id"),
  "user_id" INTEGER NOT NULL REFERENCES "users"("id")
);
