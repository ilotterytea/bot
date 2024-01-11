-- Your SQL goes here
CREATE TABLE IF NOT EXISTS "sessions" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "user_id" INTEGER NOT NULL REFERENCES "users"("id"),
  "access_token" VARCHAR NOT NULL,
  "refresh_token" VARCHAR NOT NULL,
  "scopes" TEXT[] NOT NULL,
  "expires_at" TIMESTAMP NOT NULL
);
