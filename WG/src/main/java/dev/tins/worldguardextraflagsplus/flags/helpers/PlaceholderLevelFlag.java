package dev.tins.worldguardextraflagsplus.flags.helpers;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

public class PlaceholderLevelFlag extends Flag<String>
{
	public PlaceholderLevelFlag(String name)
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
		
		if (input.isEmpty())
		{
			throw new InvalidFlagFormat("Flag value cannot be empty. Format: <threshold> <source> where source is either 'XP' or a PlaceholderAPI placeholder (e.g., 10 XP or 10 %battlepass_tier%)");
		}
		
		// Parse the two arguments: threshold and source
		String[] parts = input.split("\\s+", 2);
		if (parts.length != 2)
		{
			throw new InvalidFlagFormat("Invalid format. Expected: <threshold> <source> where source is 'XP' or a placeholder (e.g., 10 XP or 10 %battlepass_tier%)");
		}
		
		// Validate threshold (first argument) is a number
		String thresholdStr = parts[0].trim();
		try
		{
			Integer.parseInt(thresholdStr);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidFlagFormat("Threshold must be a valid integer. Got: '" + thresholdStr + "'");
		}
		
		// Validate source (second argument) is either "XP" or a placeholder
		String source = parts[1].trim();
		if (!source.equalsIgnoreCase("XP") && (!source.startsWith("%") || !source.endsWith("%")))
		{
			throw new InvalidFlagFormat("Source must be either 'XP' or a PlaceholderAPI placeholder (e.g., %battlepass_tier%). Got: '" + source + "'");
		}
		
		return input; // Store as: "threshold source"
	}

	@Override
	public String unmarshal(Object o)
	{
		if (o == null)
		{
			return null;
		}
		
		return o.toString();
	}
}

