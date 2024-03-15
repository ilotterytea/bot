-- Your SQL goes here
ALTER TABLE "channel_preferences"
ADD COLUMN "features" TEXT[] NOT NULL DEFAULT '{}';
