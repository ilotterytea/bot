-- Your SQL goes here

CREATE TYPE "level_of_rights" AS ENUM ('suspended', 'user', 'subscriber', 'vip', 'moderator', 'broadcaster');

CREATE TABLE IF NOT EXISTS "rights" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "user_id" INTEGER NOT NULL REFERENCES "users"("id"),
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "level" level_of_rights NOT NULL DEFAULT 'user',
  "is_fixed" BOOLEAN NOT NULL DEFAULT FALSE
);
