-- This file should undo anything in `up.sql`
ALTER TABLE "custom_commands" DROP COLUMN "is_global";
ALTER TABLE "custom_commands" ALTER COLUMN "is_enabled" SET DEFAULT 'false';