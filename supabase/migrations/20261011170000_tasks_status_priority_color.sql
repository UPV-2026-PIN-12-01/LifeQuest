-- Card color for the home calendar. status and priority already exist as the enums task_status and task_priority.
ALTER TABLE "public"."tasks"
  ADD COLUMN IF NOT EXISTS "color" text NOT NULL DEFAULT 'purple';

ALTER TABLE "public"."tasks"
  ADD CONSTRAINT "chk_task_color" CHECK (color IN ('purple', 'red', 'blue', 'yellow', 'green'));

CREATE INDEX IF NOT EXISTS "tasks_user_id_start_time_idx" ON "public"."tasks" (user_id, start_time);
