-- Your SQL goes here
CREATE TABLE IF NOT EXISTS "lua_storage" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "user_id" INTEGER NOT NULL REFERENCES "users"("id"),
    "lua_id" TEXT NOT NULL,
    "value" TEXT NOT NULL DEFAULT ''
);