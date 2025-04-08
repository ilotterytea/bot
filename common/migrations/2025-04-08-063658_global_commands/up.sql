-- Your SQL goes here
ALTER TABLE "custom_commands" ADD COLUMN "is_global" BOOLEAN NOT NULL DEFAULT 'false';
ALTER TABLE "custom_commands" ALTER COLUMN "is_enabled" SET DEFAULT 'true';