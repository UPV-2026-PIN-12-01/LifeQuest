ALTER TABLE public.goals
  DROP CONSTRAINT IF EXISTS check_rating,
  DROP CONSTRAINT IF EXISTS chk_goal_rating,
  DROP CONSTRAINT IF EXISTS goals_rating_range_check;

ALTER TABLE public.goals
  ADD CONSTRAINT goals_rating_range_check
  CHECK (rating IS NULL OR rating BETWEEN 1 AND 10);
