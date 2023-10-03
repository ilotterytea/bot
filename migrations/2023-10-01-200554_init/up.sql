-- Your SQL goes here
CREATE TABLE "channels" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "alias_id" INTEGER NOT NULL UNIQUE,
  "joined_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "opt_outed_at" TIMESTAMP
);

CREATE TABLE "channel_preferences" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "prefix" VARCHAR,
  "language" VARCHAR
);

CREATE TABLE "users" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "alias_id" INTEGER NOT NULL UNIQUE,
  "joined_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "opt_outed_at" TIMESTAMP,
  "is_super_user" BOOLEAN NOT NULL DEFAULT FALSE
);
