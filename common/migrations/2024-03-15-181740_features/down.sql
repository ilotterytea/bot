-- This file should undo anything in `up.sql`
ALTER TABLE "channel_preferences"
DROP COLUMN "features";
