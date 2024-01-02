-- Your SQL goes here
CREATE TABLE "users" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "alias_id" INTEGER NOT NULL UNIQUE,
  "alias_name" VARCHAR NOT NULL UNIQUE,
  "joined_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "opt_outed_at" TIMESTAMP
);

CREATE TABLE "channels" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "alias_id" INTEGER NOT NULL UNIQUE,
  "alias_name" VARCHAR NOT NULL UNIQUE,
  "joined_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "opt_outed_at" TIMESTAMP
);

CREATE TABLE "channel_preferences" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "channel_id" INTEGER NOT NULL UNIQUE REFERENCES "channels"("id"),
  "prefix" VARCHAR NOT NULL,
  "language" VARCHAR NOT NULL
);

CREATE TYPE "action_statuses" AS ENUM ('ok', 'error');

CREATE TABLE "actions" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "user_id" INTEGER NOT NULL REFERENCES "users"("id"),
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "command_name" VARCHAR NOT NULL,
  "arguments" VARCHAR,
  "response" VARCHAR NOT NULL,
  "status" "action_statuses" NOT NULL,
  "sent_at" TIMESTAMP NOT NULL,
  "processed_at" TIMESTAMP NOT NULL
);
