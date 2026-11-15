ALTER TABLE public.achievement_index
  ADD COLUMN IF NOT EXISTS requirement_type text NOT NULL DEFAULT 'completed_tasks',
  ADD COLUMN IF NOT EXISTS requirement_target numeric NOT NULL DEFAULT 1,
  ADD COLUMN IF NOT EXISTS requirement_unit text NOT NULL DEFAULT 'missions';

INSERT INTO public.achievement_index
  (name, description, xp_reward, gold_reward, icon_url, requirement_type, requirement_target, requirement_unit)
VALUES
  ('Primer paso', 'Completa tu primera misión.', 25, 10, '👣', 'completed_tasks', 1, 'misiones'),
  ('Principiante', 'También conocido como Productivo. Completa 5 misiones.', 50, 20, '⚡', 'completed_tasks', 5, 'misiones'),
  ('Constante', 'Mantén una racha de 7 días.', 100, 40, '🔥', 'day_streak', 7, 'días'),
  ('Concentrado', 'También conocido como Enfocado. Completa 5 sesiones con foco.', 125, 50, '🎯', 'focused_tasks', 5, 'sesiones'),
  ('Héroe', 'También conocido como Nivel 10. Alcanza el nivel 10.', 200, 80, '🏆', 'level', 10, 'nivel'),
  ('Ratón de biblioteca', 'También conocido como Maestro del Tiempo. Registra 10 horas de estudio.', 175, 70, '📚', 'study_minutes', 600, 'horas'),
  ('Veterano', 'También conocido como Campeón. Completa 50 misiones.', 300, 120, '👑', 'completed_tasks', 50, 'misiones'),
  ('Imparable', 'Mantén una racha de 30 días.', 500, 200, '💎', 'day_streak', 30, 'días')
ON CONFLICT (name) DO UPDATE SET
  description = EXCLUDED.description,
  xp_reward = EXCLUDED.xp_reward,
  gold_reward = EXCLUDED.gold_reward,
  icon_url = EXCLUDED.icon_url,
  requirement_type = EXCLUDED.requirement_type,
  requirement_target = EXCLUDED.requirement_target,
  requirement_unit = EXCLUDED.requirement_unit;
