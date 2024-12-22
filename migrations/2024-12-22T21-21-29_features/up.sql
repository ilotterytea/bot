-- Your SQL goes here
ALTER TABLE "channel_preferences" ADD "features" INTEGER ARRAY NOT NULL DEFAULT ARRAY[]::INTEGER[];