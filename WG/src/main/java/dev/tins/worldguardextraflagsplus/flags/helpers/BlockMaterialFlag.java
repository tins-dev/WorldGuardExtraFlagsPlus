package dev.tins.worldguardextraflagsplus.flags.helpers;

import org.bukkit.Material;

import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

public class BlockMaterialFlag extends MaterialFlag
{
	public BlockMaterialFlag(String name)
	{
		super(name);
	}

	@Override
	public Material parseInput(FlagContext context) throws InvalidFlagFormat
	{
		Material material = super.parseInput(context);
		if (material != null && !material.isBlock())
		{
			throw new InvalidFlagFormat("This material isn't a placeable block, use a valid block material");
		}
		return material;
	}
}
