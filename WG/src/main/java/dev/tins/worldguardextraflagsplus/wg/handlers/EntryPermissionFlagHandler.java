package dev.tins.worldguardextraflagsplus.wg.handlers;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.session.handler.Handler;
import com.sk89q.worldguard.session.Session;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;

import dev.tins.worldguardextraflagsplus.flags.Flags;
import dev.tins.worldguardextraflagsplus.wg.WorldGuardUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class EntryPermissionFlagHandler extends Handler
{
	public static final Factory FACTORY(Plugin plugin)
	{
		return new Factory(plugin);
	}

	public static class Factory extends Handler.Factory<EntryPermissionFlagHandler>
	{
		private final Plugin plugin;

		public Factory(Plugin plugin)
		{
			this.plugin = plugin;
		}

		@Override
		public EntryPermissionFlagHandler create(Session session)
		{
			return new EntryPermissionFlagHandler(this.plugin, session);
		}
	}

	private final Plugin plugin;

	protected EntryPermissionFlagHandler(Plugin plugin, Session session)
	{
		super(session);
		this.plugin = plugin;
	}

	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType)
	{
		// Check if player has bypass
		if (this.getSession().getManager().hasBypass(player, (World) to.getExtent()))
		{
			return true; // Allow entry if player has bypass
		}

		Player bukkitPlayer = ((BukkitPlayer) player).getPlayer();
		if (bukkitPlayer == null || !bukkitPlayer.isOnline())
		{
			return true; // Allow entry if player is not available
		}

		// Check entry-permission flag (explicit allow — takes priority over entry-deny-permission)
		String entryPermission = toSet.queryValue(player, Flags.ENTRY_PERMISSION);
		if (entryPermission != null && !entryPermission.isEmpty())
		{
			if (!bukkitPlayer.hasPermission(entryPermission))
			{
				// Player is missing the required permission, deny entry
				sendDeniedMessage(bukkitPlayer, "entry-permission-denied", entryPermission);
				return false; // Deny entry
			}
		}

		// Check entry-deny-permission flag (denies entry to players holding the permission)
		// Only applies when entry-permission is NOT set — entry-permission takes priority
		String denyPermission = toSet.queryValue(player, Flags.ENTRY_DENY_PERMISSION);
		if (denyPermission != null && !denyPermission.isEmpty() && (entryPermission == null || entryPermission.isEmpty()))
		{
			if (bukkitPlayer.hasPermission(denyPermission))
			{
				// Player holds the denied permission, deny entry
				sendDeniedMessage(bukkitPlayer, "entry-deny-permission-denied", denyPermission);
				return false; // Deny entry
			}
		}

		return true; // Allow entry
	}

	private void sendDeniedMessage(Player player, String messageKey, String permission)
	{
		if (player == null || !player.isOnline())
		{
			return;
		}

		// Send message with cooldown using FoliaLib scheduler (runs on entity thread)
		WorldGuardUtils.getScheduler().runAtEntity(player, task -> {
			if (player.isOnline())
			{
				sendMessageWithCooldown(player, messageKey, new String[]{"permission", permission});
			}
		});
	}

	/**
	 * Sends a message to a player with cooldown using reflection (WG module can't depend on Spigot module).
	 * Uses Messages.sendMessageWithCooldown() from Spigot module.
	 */
	private void sendMessageWithCooldown(Player player, String key, String... replacements)
	{
		try
		{
			// Use reflection to call Messages.sendMessageWithCooldown() from Spigot module
			Class<?> messagesClass = Class.forName("dev.tins.worldguardextraflagsplus.Messages");
			java.lang.reflect.Method sendMessageMethod = messagesClass.getMethod("sendMessageWithCooldown",
				org.bukkit.entity.Player.class, String.class, String[].class);
			sendMessageMethod.invoke(null, player, key, replacements);
		}
		catch (Exception e)
		{
			// Fallback to sending message directly without cooldown if reflection fails
			String permission = replacements.length > 1 ? replacements[1] : "?";
			final String message;
			if ("entry-permission-denied".equals(key))
			{
				message = org.bukkit.ChatColor.RED + "You do not have the required permission to enter this area. " +
					org.bukkit.ChatColor.GRAY + "Required: " + permission;
			}
			else if ("entry-deny-permission-denied".equals(key))
			{
				message = org.bukkit.ChatColor.RED + "You may not enter this area with the permission " + permission + ".";
			}
			else
			{
				message = org.bukkit.ChatColor.RED + "Message not found: " + key;
			}

			if (player.isOnline())
			{
				player.sendMessage(message);
			}
		}
	}
}
