/*
PostgreSQL Backup
Database: iotf/main
Backup Time: 2026-05-18 00:41:01
*/

DROP SEQUENCE IF EXISTS "main"."t_alarm_alarm_id_seq";
DROP SEQUENCE IF EXISTS "main"."t_alarm_log_alarm_log_id_seq";
DROP TABLE IF EXISTS "main"."t_alarm";
DROP TABLE IF EXISTS "main"."t_alarm_log";
DROP TABLE IF EXISTS "main"."t_device";
DROP TABLE IF EXISTS "main"."t_file";
DROP TABLE IF EXISTS "main"."t_operation_log";
DROP TABLE IF EXISTS "main"."t_role_permission";
DROP TABLE IF EXISTS "main"."t_user";
CREATE SEQUENCE "t_alarm_alarm_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
CREATE SEQUENCE "t_alarm_log_alarm_log_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
CREATE TABLE "t_alarm" (
  "alarm_id" int8 NOT NULL DEFAULT nextval('main.t_alarm_alarm_id_seq'::regclass),
  "device_id" int8 NOT NULL,
  "device_name" varchar(64) COLLATE "pg_catalog"."default",
  "alarm_type" varchar(32) COLLATE "pg_catalog"."default",
  "alarm_level" varchar(16) COLLATE "pg_catalog"."default",
  "status" varchar(16) COLLATE "pg_catalog"."default",
  "trigger_value" float8,
  "threshold" float8,
  "description" varchar(256) COLLATE "pg_catalog"."default",
  "triggered_time" timestamp(6),
  "acknowledged_time" timestamp(6),
  "resolved_time" timestamp(6),
  "acknowledged_by" int8,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "device_uuid" varchar(64) COLLATE "pg_catalog"."default",
  "user_id" int8
)
;
ALTER TABLE "t_alarm" OWNER TO "root";
COMMENT ON COLUMN "t_alarm"."alarm_id" IS '主键ID';
COMMENT ON COLUMN "t_alarm"."device_id" IS '设备ID';
COMMENT ON COLUMN "t_alarm"."device_name" IS '设备名称';
COMMENT ON COLUMN "t_alarm"."alarm_type" IS '告警类型: OVERLOAD-过载, HIGH_ENERGY-高能耗, LEAK-漏电, FAULT-设备故障';
COMMENT ON COLUMN "t_alarm"."alarm_level" IS '告警级别: NORMAL-普通, WARNING-警告, CRITICAL-严重';
COMMENT ON COLUMN "t_alarm"."status" IS '告警状态: TRIGGERED-已触发, ACKNOWLEDGED-已确认, RESOLVED-已恢复';
COMMENT ON COLUMN "t_alarm"."trigger_value" IS '触发时的值';
COMMENT ON COLUMN "t_alarm"."threshold" IS '告警阈值';
COMMENT ON COLUMN "t_alarm"."description" IS '告警描述';
COMMENT ON COLUMN "t_alarm"."triggered_time" IS '首次触发时间';
COMMENT ON COLUMN "t_alarm"."acknowledged_time" IS '用户确认时间';
COMMENT ON COLUMN "t_alarm"."resolved_time" IS '恢复时间';
COMMENT ON COLUMN "t_alarm"."acknowledged_by" IS '确认人';
COMMENT ON TABLE "t_alarm" IS '告警记录表';
CREATE TABLE "t_alarm_log" (
  "alarm_log_id" int8 NOT NULL DEFAULT nextval('main.t_alarm_log_alarm_log_id_seq'::regclass),
  "alarm_id" int8 NOT NULL,
  "from_status" varchar(16) COLLATE "pg_catalog"."default",
  "to_status" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "change_reason" varchar(256) COLLATE "pg_catalog"."default",
  "changed_time" timestamp(6) NOT NULL DEFAULT now(),
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "user_id" int8
)
;
ALTER TABLE "t_alarm_log" OWNER TO "root";
COMMENT ON COLUMN "t_alarm_log"."alarm_log_id" IS '主键ID';
COMMENT ON COLUMN "t_alarm_log"."alarm_id" IS '关联告警ID';
COMMENT ON COLUMN "t_alarm_log"."from_status" IS '变更前状态';
COMMENT ON COLUMN "t_alarm_log"."to_status" IS '变更后状态';
COMMENT ON COLUMN "t_alarm_log"."change_reason" IS '变更原因';
COMMENT ON COLUMN "t_alarm_log"."changed_time" IS '变更时间';
COMMENT ON TABLE "t_alarm_log" IS '告警状态变更日志表';
CREATE TABLE "t_device" (
  "user_id" int8 NOT NULL,
  "device_id" int8 NOT NULL,
  "device_uuid" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "device_name" varchar(255) COLLATE "pg_catalog"."default",
  "deleted" int2,
  "frequency" int4,
  "alarm_status" varchar(255) COLLATE "pg_catalog"."default",
  "report_status" bool,
  "threshold" float8,
  "location" varchar(128) COLLATE "pg_catalog"."default",
  "overload_threshold" float8,
  "high_energy_threshold" float8,
  "current_threshold" float8
)
;
ALTER TABLE "t_device" OWNER TO "root";
COMMENT ON COLUMN "t_device"."frequency" IS '当前上报频率 默认60秒 最小10 秒';
COMMENT ON COLUMN "t_device"."alarm_status" IS '当前告警状态*';
COMMENT ON COLUMN "t_device"."report_status" IS '上报状态';
COMMENT ON COLUMN "t_device"."overload_threshold" IS '过载阈值';
COMMENT ON COLUMN "t_device"."high_energy_threshold" IS '高能耗阈值';
COMMENT ON COLUMN "t_device"."current_threshold" IS '电流过载阈值';
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
LOCK TABLE "main"."t_alarm" IN SHARE MODE;
DELETE FROM "main"."t_alarm";
COMMIT;
BEGIN;
LOCK TABLE "main"."t_alarm_log" IN SHARE MODE;
DELETE FROM "main"."t_alarm_log";
COMMIT;
BEGIN;
LOCK TABLE "main"."t_device" IN SHARE MODE;
DELETE FROM "main"."t_device";
INSERT INTO "main"."t_device" ("user_id","device_id","device_uuid","device_type","create_time","update_time","device_name","deleted","frequency","alarm_status","report_status","threshold","location","overload_threshold","high_energy_threshold","current_threshold") VALUES (2052671096486903809, 111, 'device-001', 'AIRCON', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'living-room', NULL, NULL, NULL)
;
COMMIT;
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
ALTER TABLE "t_alarm" ADD CONSTRAINT "t_alarm_pkey" PRIMARY KEY ("alarm_id");
ALTER TABLE "t_alarm_log" ADD CONSTRAINT "t_alarm_log_pkey" PRIMARY KEY ("alarm_log_id");
ALTER TABLE "t_device" ADD CONSTRAINT "t_device_pkey" PRIMARY KEY ("device_id", "device_uuid", "user_id");
ALTER TABLE "t_file" ADD CONSTRAINT "v_file_pkey" PRIMARY KEY ("file_id");
ALTER TABLE "t_operation_log" ADD CONSTRAINT "v_operation_log1_pkey" PRIMARY KEY ("operate_id");
ALTER TABLE "t_role_permission" ADD CONSTRAINT "v_role_pkey" PRIMARY KEY ("role_id");
ALTER TABLE "t_user" ADD CONSTRAINT "t_user_pkey" PRIMARY KEY ("user_id");
ALTER SEQUENCE "t_alarm_alarm_id_seq"
OWNED BY "t_alarm"."alarm_id";
SELECT setval('"t_alarm_alarm_id_seq"', 1, false);
ALTER SEQUENCE "t_alarm_alarm_id_seq" OWNER TO "root";
ALTER SEQUENCE "t_alarm_log_alarm_log_id_seq"
OWNED BY "t_alarm_log"."alarm_log_id";
SELECT setval('"t_alarm_log_alarm_log_id_seq"', 1, false);
ALTER SEQUENCE "t_alarm_log_alarm_log_id_seq" OWNER TO "root";
