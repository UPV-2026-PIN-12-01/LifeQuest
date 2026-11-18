-- Per-stat upgrade buffs, e.g. {"strength": 2, "speed": 1}.
-- upgrade_stat_buffs (one integer for every stat) stays as the fallback for older code.
ALTER TABLE public.tool_index
  ADD COLUMN IF NOT EXISTS upgrade_stats_buff jsonb NOT NULL DEFAULT '{}'::jsonb;

-- Backfill: same behaviour as before (the old integer on every base stat).
UPDATE public.tool_index
SET upgrade_stats_buff = COALESCE((
  SELECT jsonb_object_agg(stat.key, to_jsonb(upgrade_stat_buffs))
  FROM jsonb_each(base_stats_buff) AS stat(key, value)
), '{}'::jsonb)
WHERE upgrade_stats_buff = '{}'::jsonb
  AND upgrade_stat_buffs IS NOT NULL
  AND base_stats_buff IS NOT NULL;

-- Seeded catalog: the main stat grows faster than the secondary one.
UPDATE public.tool_index tool
SET upgrade_stats_buff = buffs.value
FROM (VALUES
  ('sword',  '{"strength": 2, "speed": 1}'::jsonb),
  ('bow',    '{"strength": 2, "speed": 1}'::jsonb),
  ('armor',  '{"hp": 3, "stamina": 1}'::jsonb),
  ('helm',   '{"knowledge": 2, "hp": 1}'::jsonb),
  ('boots',  '{"speed": 2, "stamina": 1}'::jsonb),
  ('amulet', '{"knowledge": 2, "hp": 2}'::jsonb)
) AS buffs(catalog_id, value)
WHERE tool.catalog_id = buffs.catalog_id;
