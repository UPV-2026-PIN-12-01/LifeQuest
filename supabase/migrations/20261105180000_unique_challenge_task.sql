DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conrelid = 'public.challenges'::regclass
      AND conname = 'uq_challenges_task_id'
  ) THEN
    IF EXISTS (
      SELECT task_id
      FROM public.challenges
      WHERE task_id IS NOT NULL
      GROUP BY task_id
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION 'Cannot add uq_challenges_task_id: a task is assigned to multiple challenges';
    END IF;

    ALTER TABLE public.challenges
      ADD CONSTRAINT uq_challenges_task_id UNIQUE (task_id);
  END IF;
END;
$$;
