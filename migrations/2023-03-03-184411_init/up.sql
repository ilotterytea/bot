-- Your SQL goes here
CREATE TABLE "channels" (
	"id"	INTEGER NOT NULL,
	"alias_id"	INTEGER NOT NULL UNIQUE,
	"referred_from" INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE "preferences" (
	"channel_id"	INTEGER NOT NULL UNIQUE,
	"flags"	TEXT DEFAULT '[]',
	"prefix"	TEXT DEFAULT '!',
	"language"	TEXT DEFAULT 'en_us',
	PRIMARY KEY("channel_id")
);

CREATE TABLE "stats" (
	"channel_id"	INTEGER NOT NULL UNIQUE,
	"chat_lines"	INTEGER NOT NULL DEFAULT 0,
	"successful_tests"	INTEGER NOT NULL DEFAULT 0,
	"executed_commands"	INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY("channel_id")
);

CREATE TABLE "custom_commands" (
	"id"	INTEGER NOT NULL,
	"channel_id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL,
	"content"	TEXT NOT NULL,
	"enabled"	INTEGER NOT NULL DEFAULT 1,
	PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE "users" (
	"id"	INTEGER NOT NULL,
	"alias_id"	INTEGER NOT NULL UNIQUE,
	"roles"	TEXT NOT NULL DEFAULT '[]',
	"restrictions"	TEXT NOT NULL DEFAULT '[]',
	"recent_activity"	TEXT NOT NULL DEFAULT '[]',
	"is_suspended"	INTEGER NOT NULL DEFAULT 0,
	"is_superuser"	INTEGER NOT NULL DEFAULT 0,
	"secret_key"	TEXT,
	"created_timestamp"	INTEGER NOT NULL,
	"last_timestamp"	INTEGER NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE "listenable" (
	"id"	INTEGER NOT NULL,
	"alias_id"	INTEGER NOT NULL,
	"channel_id"	INTEGER NOT NULL,
	"user_ids"	TEXT NOT NULL DEFAULT '[]',
	"message"	TEXT NOT NULL,
	"icon"	TEXT NOT NULL,
	"flags"	TEXT NOT NULL DEFAULT '[]',
	PRIMARY KEY("id" AUTOINCREMENT)
);
