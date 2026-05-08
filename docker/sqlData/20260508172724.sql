/*
PostgreSQL Backup
Database: iotf/main
Backup Time: 2026-05-08 17:27:24
*/

DROP TABLE IF EXISTS "main"."t_file";
DROP TABLE IF EXISTS "main"."t_operation_log";
DROP TABLE IF EXISTS "main"."t_role_permission";
DROP TABLE IF EXISTS "main"."t_user";
CREATE TABLE "t_file" (
  "file_id" int8 NOT NULL,
  "file_key" varchar(255) COLLATE "pg_catalog"."default",
  "is_public_read" bool,
  "uploader" int8,
  "create_time" timestamp(0),
  "update_time" timestamp(0),
  "type" int4,
  "suffix" varchar(8) COLLATE "pg_catalog"."default",
  "title" varchar(255) COLLATE "pg_catalog"."default",
  "info" text COLLATE "pg_catalog"."default",
  "origin_name" text COLLATE "pg_catalog"."default",
  "file_size" int8
)
;
ALTER TABLE "t_file" OWNER TO "root";
COMMENT ON TABLE "t_file" IS '文件';
CREATE TABLE "t_operation_log" (
  "operate_id" int8 NOT NULL,
  "operator" int8,
  "operation" varchar(64) COLLATE "pg_catalog"."default",
  "target_id" int8,
  "target_type" varchar(64) COLLATE "pg_catalog"."default",
  "detail" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(0),
  "update_time" timestamp(0)
)
;
ALTER TABLE "t_operation_log" OWNER TO "root";
CREATE TABLE "t_role_permission" (
  "role_id" int8 NOT NULL,
  "role" varchar(64) COLLATE "pg_catalog"."default",
  "permission" json,
  "create_time" timestamp(0),
  "update_time" timestamp(0)
)
;
ALTER TABLE "t_role_permission" OWNER TO "root";
COMMENT ON TABLE "t_role_permission" IS '角色权限表';
CREATE TABLE "t_user" (
  "user_id" int8 NOT NULL,
  "user_name" varchar(64) COLLATE "pg_catalog"."default",
  "phone" varchar(32) COLLATE "pg_catalog"."default",
  "email" varchar(32) COLLATE "pg_catalog"."default",
  "salt" varchar(255) COLLATE "pg_catalog"."default",
  "password_hash" varchar(255) COLLATE "pg_catalog"."default",
  "role_id" int8,
  "user_status" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "nick_name" varchar(64) COLLATE "pg_catalog"."default",
  "sex" int2,
  "icon" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "t_user" OWNER TO "root";
BEGIN;
LOCK TABLE "main"."t_file" IN SHARE MODE;
DELETE FROM "main"."t_file";
COMMIT;
BEGIN;
LOCK TABLE "main"."t_operation_log" IN SHARE MODE;
DELETE FROM "main"."t_operation_log";
COMMIT;
BEGIN;
LOCK TABLE "main"."t_role_permission" IN SHARE MODE;
DELETE FROM "main"."t_role_permission";
INSERT INTO "main"."t_role_permission" ("role_id","role","permission","create_time","update_time") VALUES (1, 'DEFAULT', '["DEFAULT"]', '2026-05-08 15:28:37', NULL)
;
COMMIT;
BEGIN;
LOCK TABLE "main"."t_user" IN SHARE MODE;
DELETE FROM "main"."t_user";
INSERT INTO "main"."t_user" ("user_id","user_name","phone","email","salt","password_hash","role_id","user_status","create_time","update_time","nick_name","sex","icon") VALUES (2052671096486903809, 'Centripet', '13613480352', '123123w', 'd2791870dea4636f', '1b8874ac00a1ed31941a605e5c317a706915c10125b2505b526776bfc39f4c12', 1, 1, '2026-05-08 16:45:00.837629', NULL, NULL, NULL, NULL)
;
COMMIT;
ALTER TABLE "t_file" ADD CONSTRAINT "v_file_pkey" PRIMARY KEY ("file_id");
ALTER TABLE "t_operation_log" ADD CONSTRAINT "v_operation_log1_pkey" PRIMARY KEY ("operate_id");
ALTER TABLE "t_role_permission" ADD CONSTRAINT "v_role_pkey" PRIMARY KEY ("role_id");
ALTER TABLE "t_user" ADD CONSTRAINT "t_user_pkey" PRIMARY KEY ("user_id");
