-- Fields the home calendar needs per task. "in_progress" and "finished" are derived from the times, so only the user's outcome is stored.
ALTER TABLE "public"."tasks"
  ADD COLUMN IF NOT EXISTS "status"   text NOT NULL DEFAULT 'pending',
  ADD COLUMN IF NOT EXISTS "priority" text NOT NULL DEFAULT 'medium',
  ADD COLUMN IF NOT EXISTS "color"    text NOT NULL DEFAULT 'purple';

ALTER TABLE "public"."tasks"
  ADD CONSTRAINT "chk_task_status" CHECK (status IN ('pending', 'completed', 'not_completed')),
  ADD CONSTRAINT "chk_task_priority" CHECK (priority IN ('low', 'medium', 'high')),
  ADD CONSTRAINT "chk_task_color" CHECK (color IN ('purple', 'red', 'blue', 'yellow', 'green'));

CREATE INDEX IF NOT EXISTS "tasks_user_id_start_time_idx" ON "public"."tasks" (user_id, start_time);
