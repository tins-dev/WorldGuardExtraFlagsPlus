package dev.tins.worldguardextraflagsplus.flags.helpers;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

/**
 * Element flag for the {@code deny-mobspawn} set flag.
 *
 * <p>Accepts the category tokens {@code ALL}, {@code ALL_HOSTILE}, {@code ALL_NEUTRAL},
 * {@code ALL_PEACEFUL} or any valid {@link EntityType} name for per-mob granularity.
 * The hostile set is derived at class-load from the {@link Enemy} interface so newly
 * added hostile mobs are covered automatically.</p>
 */
public class MobSpawnDenyCategoryFlag extends Flag<String>
{
	public static final String ALL = "ALL";
	public static final String ALL_HOSTILE = "ALL_HOSTILE";
	public static final String ALL_NEUTRAL = "ALL_NEUTRAL";
	public static final String ALL_PEACEFUL = "ALL_PEACEFUL";

	private static final Set<String> CATEGORY_TOKENS = Set.of(ALL, ALL_HOSTILE, ALL_NEUTRAL, ALL_PEACEFUL);

	/** Mobs that attack on sight — every entity type whose class implements {@link Enemy} (bosses included). */
	private static final Set<EntityType> HOSTILE = new HashSet<>();

	/** Mobs that only attack when provoked and are not {@link Enemy}. */
	private static final Set<EntityType> NEUTRAL = new HashSet<>();

	/** Every alive entity type that is neither hostile nor neutral (villagers, animals, fish, bats, allays, armor stands, ...). */
	private static final Set<EntityType> PEACEFUL = new HashSet<>();

	static
	{
		for (EntityType type : EntityType.values())
		{
			Class<? extends Entity> clazz = type.getEntityClass();
			if (clazz != null && Enemy.class.isAssignableFrom(clazz))
			{
				HOSTILE.add(type);
			}
		}

		for (String name : new String[] {
				"WOLF", "BEE", "IRON_GOLEM", "SNOW_GOLEM", "POLAR_BEAR", "GOAT", "PANDA",
				"LLAMA", "TRADER_LLAMA", "DOLPHIN", "FOX"})
		{
			try
			{
				NEUTRAL.add(EntityType.valueOf(name));
			}
			catch (IllegalArgumentException ignored)
			{
			}
		}

		for (EntityType type : EntityType.values())
		{
			if (type.isAlive() && !HOSTILE.contains(type) && !NEUTRAL.contains(type))
			{
				PEACEFUL.add(type);
			}
		}
	}

	public MobSpawnDenyCategoryFlag(String name)
	{
		super(name);
	}

	@Override
	public Object marshal(String o)
	{
		return o;
	}

	@Override
	public String parseInput(FlagContext context) throws InvalidFlagFormat
	{
		String input = context.getUserInput().trim();

		// Handle empty input
		if (input.isEmpty())
		{
			throw new InvalidFlagFormat("Mob category cannot be empty");
		}

		// Convert to uppercase for comparison
		String upperInput = input.toUpperCase();

		// Validate against category tokens
		if (CATEGORY_TOKENS.contains(upperInput))
		{
			return upperInput;
		}

		// Validate against known EntityType names
		try
		{
			EntityType.valueOf(upperInput);
			return upperInput;
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidFlagFormat("Invalid mob category '" + input + "'. Accepted values: ALL, ALL_HOSTILE, ALL_NEUTRAL, ALL_PEACEFUL, or any EntityType name (e.g. ZOMBIE, CREEPER)");
		}
	}

	@Override
	public String unmarshal(Object o)
	{
		if (o instanceof String)
		{
			String category = ((String) o).toUpperCase();
			// Validate on unmarshal too (for config loading)
			if (CATEGORY_TOKENS.contains(category))
			{
				return category;
			}
			try
			{
				EntityType.valueOf(category);
				return category;
			}
			catch (IllegalArgumentException ignored)
			{
			}
		}
		return null;
	}

	/**
	 * Returns whether a mob type is blocked by the given flag values.
	 */
	public static boolean isBlockedBy(EntityType type, Set<String> values)
	{
		if (type == null || values == null || values.isEmpty())
		{
			return false;
		}

		for (String value : values)
		{
			if (value == null)
			{
				continue;
			}
			switch (value)
			{
				case ALL:
					return true;
				case ALL_HOSTILE:
					if (HOSTILE.contains(type))
					{
						return true;
					}
					break;
				case ALL_NEUTRAL:
					if (NEUTRAL.contains(type))
					{
						return true;
					}
					break;
				case ALL_PEACEFUL:
					if (PEACEFUL.contains(type))
					{
						return true;
					}
					break;
				default:
					try
					{
						if (EntityType.valueOf(value) == type)
						{
							return true;
						}
					}
					catch (IllegalArgumentException ignored)
					{
					}
					break;
			}
		}
		return false;
	}
}
