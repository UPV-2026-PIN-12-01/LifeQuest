DO $$
DECLARE
  rating_constraint RECORD;
BEGIN
  IF to_regclass('public.goals') IS NULL AND to_regclass('public.goal') IS NOT NULL THEN
    ALTER TABLE public.goal RENAME TO goals;
  END IF;

  IF to_regclass('public.goals') IS NOT NULL THEN
    FOR rating_constraint IN
      SELECT conname
      FROM pg_constraint
      WHERE conrelid = 'public.goals'::regclass
        AND contype = 'c'
        AND pg_get_constraintdef(oid) ILIKE '%rating%'
    LOOP
      EXECUTE format('ALTER TABLE public.goals DROP CONSTRAINT %I', rating_constraint.conname);
    END LOOP;

    ALTER TABLE public.goals
      ADD CONSTRAINT goals_rating_range_check
      CHECK (rating IS NULL OR rating BETWEEN 1 AND 10);
  END IF;
END;
$$;
