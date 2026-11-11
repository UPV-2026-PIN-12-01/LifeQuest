UPDATE public.users
SET xp_acc_boost = (1.0 + 0.05 * LEAST(GREATEST(COALESCE(day_streak, 0), 0), 7))::real;
