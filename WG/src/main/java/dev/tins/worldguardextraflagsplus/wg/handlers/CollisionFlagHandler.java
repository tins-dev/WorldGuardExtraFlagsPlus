package dev.tins.worldguardextraflagsplus.wg.handlers;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import dev.tins.worldguardextraflagsplus.flags.Flags;
import dev.tins.worldguardextraflagsplus.wg.WorldGuardUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class CollisionFlagHandler extends FlagValueChangeHandler<Boolean>
{
	private static final String TEAM_NAME = "WGEFP_COLLISION";
	
	public static final Factory FACTORY()
	{
		return new Factory();
	}
	
	public static class Factory extends Handler.Factory<CollisionFlagHandler>
	{
		@Override
		public CollisionFlagHandler create(Session session)
		{
			return new CollisionFlagHandler(session);
		}
	}
	
	protected CollisionFlagHandler(Session session)
	{
		super(session, Flags.DISABLE_COLLISION);
	}
	
	@Override
	protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Boolean value)
	{
		this.handleValue(player, player.getWorld(), value);
	}
	
	@Override
	protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean currentValue, Boolean lastValue, MoveType moveType)
	{
		this.handleValue(player, (World) to.getExtent(), currentValue);
		return true;
	}
	
	@Override
	protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean lastValue, MoveType moveType)
	{
		this.handleValue(player, (World) to.getExtent(), null);
		return true;
	}
	
	private void handleValue(LocalPlayer player, World world, Boolean disableCollision)
	{
		Player bukkitPlayer = ((BukkitPlayer) player).getPlayer();
		
		// Don't schedule tasks during shutdown
		if (!WorldGuardUtils.isPluginEnabled() || !bukkitPlayer.isOnline())
		{
			return;
		}
		
		// Check if player has bypass
		if (this.getSession().getManager().hasBypass(player, world))
		{
			// Remove from collision team if has bypass
			WorldGuardUtils.getScheduler().runAtEntity(bukkitPlayer, task -> {
				removeFromCollisionTeam(bukkitPlayer);
			});
			return;
		}
		
		// Use FoliaLib scheduler to run on entity thread
		WorldGuardUtils.getScheduler().runAtEntity(bukkitPlayer, task -> {
			if (disableCollision != null && disableCollision)
			{
				// Add to collision-disabled team
				addToCollisionTeam(bukkitPlayer);
			}
			else
			{
				// Remove from collision-disabled team
				removeFromCollisionTeam(bukkitPlayer);
			}
		});
	}
	
	/**
	 * Adds player to collision-disabled team
	 */
	private void addToCollisionTeam(Player player)
	{
		Scoreboard scoreboard = player.getScoreboard();
		
		// Get or create team
		Team team = scoreboard.getTeam(TEAM_NAME);
		if (team == null)
		{
			team = scoreboard.registerNewTeam(TEAM_NAME);
		}
		// Set collision rule to NEVER (disable collision)
		team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		
		// Add player to team if not already added
		if (!team.hasEntry(player.getName()))
		{
			team.addEntry(player.getName());
		}
	}
	
	/**
	 * Removes player from collision-disabled team
	 */
	private void removeFromCollisionTeam(Player player)
	{
		Scoreboard scoreboard = player.getScoreboard();
		Team team = scoreboard.getTeam(TEAM_NAME);
		
		if (team != null && team.hasEntry(player.getName()))
		{
			team.removeEntry(player.getName());
		}
	}
}

