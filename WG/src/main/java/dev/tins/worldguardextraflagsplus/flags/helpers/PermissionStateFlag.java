package dev.tins.worldguardextraflagsplus.flags.helpers;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

public class PermissionStateFlag extends Flag<String>
{
	public PermissionStateFlag(String name)
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
			throw new InvalidFlagFormat("Permission cannot be empty. Provide a permission node (e.g., myPlugin.myPermission.1)");
		}

		// Permission nodes may contain letters, numbers, dots, hyphens, underscores, and wildcards (*)
		if (!input.matches("^[a-zA-Z0-9._*-]+$"))
		{
			throw new InvalidFlagFormat("Invalid permission format. Permission nodes may only contain letters, numbers, dots, hyphens, underscores, and wildcards (*). Got: '" + input + "'");
		}

		return input;
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
