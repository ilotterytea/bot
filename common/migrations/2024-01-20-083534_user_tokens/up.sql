-- Your SQL goes here
CREATE TABLE IF NOT EXISTS "user_tokens" (
  "user_id" INTEGER NOT NULL UNIQUE PRIMARY KEY REFERENCES "users"("id"),
  "token" UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
  "created_at" TIMESTAMP NOT NULL DEFAULT timezone('utc', now())
);
