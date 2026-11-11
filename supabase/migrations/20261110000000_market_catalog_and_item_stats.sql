ALTER TABLE public.tool_index
  ADD COLUMN catalog_id text,
  ADD COLUMN icon text,
  ADD COLUMN tool_type text,
  ADD COLUMN rarity text,
  ADD COLUMN base_level integer NOT NULL DEFAULT 1,
  ADD COLUMN heal integer,
  ADD COLUMN stamina_restore integer;

ALTER TABLE public.tool_index
  ADD CONSTRAINT tool_index_catalog_id_key UNIQUE (catalog_id);

ALTER TABLE public.player_inventory
  ADD COLUMN stats jsonb NOT NULL DEFAULT '{}'::jsonb;

INSERT INTO public.tool_index (
  catalog_id, name, icon, tool_type, rarity, base_level, max_level,
  purchase_price, base_upgrade_price, upgrade_stat_buffs, base_stats_buff,
  heal, stamina_restore
) VALUES
  ('sword', 'Espada del Alba', '⚔️', 'Arma', 'Rare', 3, 100, 260, 55, 1,
   '{"strength": 8, "speed": 2}'::jsonb, NULL, NULL),
  ('bow', 'Arco del Rastreador', '🏹', 'Arma', 'Epic', 5, 100, 480, 55, 1,
   '{"strength": 11, "speed": 6}'::jsonb, NULL, NULL),
  ('armor', 'Coraza del Guardián', '🛡️', 'Armadura', 'Rare', 4, 100, 340, 55, 1,
   '{"hp": 18, "stamina": 5}'::jsonb, NULL, NULL),
  ('helm', 'Yelmo de Claridad', '⛑️', 'Casco', 'Epic', 5, 100, 410, 55, 1,
   '{"knowledge": 12, "hp": 6}'::jsonb, NULL, NULL),
  ('boots', 'Botas del Viento', '🥾', 'Botas', 'Mythic', 7, 100, 690, 55, 1,
   '{"speed": 16, "stamina": 8}'::jsonb, NULL, NULL),
  ('amulet', 'Amuleto Celestial', '🔮', 'Accesorio', 'Celestial', 10, 100, 1250, 55, 1,
   '{"knowledge": 24, "hp": 15}'::jsonb, NULL, NULL),
  ('health-potion', 'Poción de vida', '🧪', 'Poción', 'Common', 1, 100, 55, 55, 1,
   '{}'::jsonb, 35, NULL),
  ('greater-health-potion', 'Poción de vida mayor', '⚗️', 'Poción', 'Rare', 4, 100, 120, 55, 1,
   '{}'::jsonb, 70, NULL),
  ('stamina-potion', 'Poción de estamina', '🍵', 'Poción', 'Common', 1, 100, 45, 55, 1,
   '{}'::jsonb, NULL, 45)
ON CONFLICT (catalog_id) DO UPDATE SET
  name = EXCLUDED.name,
  icon = EXCLUDED.icon,
  tool_type = EXCLUDED.tool_type,
  rarity = EXCLUDED.rarity,
  base_level = EXCLUDED.base_level,
  max_level = EXCLUDED.max_level,
  purchase_price = EXCLUDED.purchase_price,
  base_upgrade_price = EXCLUDED.base_upgrade_price,
  upgrade_stat_buffs = EXCLUDED.upgrade_stat_buffs,
  base_stats_buff = EXCLUDED.base_stats_buff,
  heal = EXCLUDED.heal,
  stamina_restore = EXCLUDED.stamina_restore;

UPDATE public.player_inventory inventory
SET stats = COALESCE((
  SELECT jsonb_object_agg(
    stat.key,
    to_jsonb(
      (stat.value #>> '{}')::integer
      + GREATEST(inventory.level - tool.base_level, 0) * tool.upgrade_stat_buffs
    )
  )
  FROM jsonb_each(tool.base_stats_buff) AS stat(key, value)
), '{}'::jsonb)
FROM public.tool_index tool
WHERE inventory.catalog_id = tool.catalog_id
  AND inventory.stats = '{}'::jsonb;
