-- Your SQL goes here
CREATE TABLE IF NOT EXISTS "actions" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "user_id" INTEGER NOT NULL REFERENCES "users"("id"),
  "command" VARCHAR NOT NULL,
  "attributes" VARCHAR,
  "full_message" VARCHAR NOT NULL,
  "response" VARCHAR,
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP 
);
