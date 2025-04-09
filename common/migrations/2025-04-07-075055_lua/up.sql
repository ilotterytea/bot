-- Your SQL goes here
CREATE TABLE IF NOT EXISTS "lua_channel_storage" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "channel_id" INTEGER NOT NULL REFERENCES "channels"("id"),
    "lua_id" TEXT NOT NULL,
    "value" TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS "lua_user_storage" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "user_id" INTEGER NOT NULL REFERENCES "users"("id"),
    "lua_id" TEXT NOT NULL,
    "value" TEXT NOT NULL DEFAULT ''
);