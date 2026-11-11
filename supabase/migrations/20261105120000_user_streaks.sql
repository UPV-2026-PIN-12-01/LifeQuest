ALTER TABLE public.users
  ADD COLUMN IF NOT EXISTS day_streak integer NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS last_activity_date date;

UPDATE public.users
SET task_streak = COALESCE(task_streak, 0),
    xp_acc_boost = 1.0;

ALTER TABLE public.users
  ALTER COLUMN task_streak SET DEFAULT 0,
  ALTER COLUMN task_streak SET NOT NULL,
  ALTER COLUMN xp_acc_boost SET DEFAULT 1.0,
  ALTER COLUMN xp_acc_boost SET NOT NULL;
