ALTER TABLE public.player_inventory
  ADD COLUMN IF NOT EXISTS catalog_id text;

ALTER TABLE public.player_inventory
  ADD COLUMN IF NOT EXISTS quantity integer NOT NULL DEFAULT 1;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'player_inventory'
      AND column_name = 'type'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'player_inventory'
      AND column_name = 'slot'
  ) THEN
    ALTER TABLE public.player_inventory
      ALTER COLUMN type TYPE text USING type::text;
    ALTER TABLE public.player_inventory
      RENAME COLUMN type TO slot;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'player_inventory'
      AND column_name = 'is_equiped'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'player_inventory'
      AND column_name = 'equipped'
  ) THEN
    ALTER TABLE public.player_inventory
      RENAME COLUMN is_equiped TO equipped;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM public.player_inventory
    WHERE catalog_id IS NULL
      OR slot IS NULL
      OR character_id IS NULL
  ) THEN
    RAISE EXCEPTION
      'Existing player_inventory rows need catalog_id, slot, and character_id values before they can be used by the market';
  END IF;
END;
$$;

ALTER TABLE public.player_inventory
  ALTER COLUMN catalog_id SET NOT NULL;

ALTER TABLE public.player_inventory
  ALTER COLUMN slot SET NOT NULL,
  ALTER COLUMN character_id SET NOT NULL,
  ALTER COLUMN equipped SET NOT NULL,
  ALTER COLUMN equipped SET DEFAULT false,
  ALTER COLUMN level TYPE integer USING level::integer;

CREATE UNIQUE INDEX IF NOT EXISTS player_inventory_one_equipped_item_per_slot
  ON public.player_inventory(character_id, slot)
  WHERE equipped;
