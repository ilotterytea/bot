-- Your SQL goes here
CREATE TABLE "custom_commands" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
  "name" VARCHAR NOT NULL,
  "messages" TEXT[] NOT NULL DEFAULT ARRAY[]::VARCHAR[],
  "is_enabled" BOOLEAN NOT NULL DEFAULT FALSE,
  "created_at" TIMESTAMP NOT NULL DEFAULT timezone('utc', now()),
  "last_executed_at" TIMESTAMP
);
