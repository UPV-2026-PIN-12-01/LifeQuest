ALTER TABLE public.achievement_index
  ADD COLUMN IF NOT EXISTS requirement_activity text;

UPDATE public.achievement_index
SET
  requirement_target = CASE
    WHEN lower(description) LIKE '%hora%' THEN COALESCE(
      NULLIF((regexp_match(description, '([0-9]+)'))[1], '')::numeric * 60,
      requirement_target
    )
    ELSE COALESCE(
      NULLIF((regexp_match(description, '([0-9]+)'))[1], '')::numeric,
      requirement_target
    )
  END,
  requirement_unit = CASE
    WHEN lower(description) LIKE '%nivel%' THEN 'nivel'
    WHEN lower(description) LIKE '%hora%' THEN 'horas'
    WHEN lower(description) LIKE '%racha%' OR lower(description) LIKE '%días%' THEN 'días'
    ELSE 'misiones'
  END,
  requirement_type = CASE
    WHEN lower(description) LIKE '%nivel%' THEN 'level'
    WHEN lower(description) LIKE '%hora%' THEN 'study_minutes'
    WHEN lower(description) LIKE '%racha%' OR lower(description) LIKE '%días%' THEN 'day_streak'
    ELSE 'completed_tasks'
  END,
  requirement_activity = CASE
    WHEN lower(description) LIKE '%trabajo%' THEN 'WORK'
    WHEN lower(description) LIKE '%entrenamiento%' THEN 'EXERCISE'
    WHEN lower(description) LIKE '%deporte%' THEN 'SPORTS'
    WHEN lower(description) LIKE '%vida social%' THEN 'SOCIAL'
    WHEN lower(description) LIKE '%ocio%' THEN 'ENTERTAINMENT'
    ELSE NULL
  END
WHERE description IS NOT NULL;
