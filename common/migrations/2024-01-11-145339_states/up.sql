-- Your SQL goes here
CREATE TABLE IF NOT EXISTS "session_states" (
  "state" VARCHAR NOT NULL UNIQUE PRIMARY KEY,
  "created_at" TIMESTAMP NOT NULL DEFAULT timezone('utc', now())
);
