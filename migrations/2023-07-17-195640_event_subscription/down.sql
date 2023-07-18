-- This file should undo anything in `up.sql`
DROP TABLE "event_subscriptions";

ALTER TABLE "events" DROP CONSTRAINT "check_target";
ALTER TABLE "events" DROP CONSTRAINT "check_type";

ALTER TABLE "events" DROP COLUMN "flags";
ALTER TABLE "events" DROP COLUMN "event_type";
DROP TYPE "event_flag";
DROP TYPE "event_type";

DROP TABLE "events";

