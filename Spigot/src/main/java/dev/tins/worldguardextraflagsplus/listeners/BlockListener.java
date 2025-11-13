package dev.tins.worldguardextraflagsplus.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

import com.sk89q.worldguard.protection.flags.StateFlag.State;

import lombok.RequiredArgsConstructor;
import dev.tins.worldguardextraflagsplus.flags.Flags;

import java.util.Set;

@RequiredArgsConstructor
public class BlockListener implements Listener
{
	private final WorldGuardPlugin worldGuardPlugin;
	private final RegionContainer regionContainer;
	private final SessionManager sessionManager;
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockPlaceEvent(PlaceBlockEvent event)
	{
		Event.Result originalResult = event.getResult();
		Object cause = event.getCause().getRootCause();
		
		if (!(cause instanceof Player player))
		{
			return;
		}
		
		LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
		if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
		{
			return;
		}
		
		for (Block block : event.getBlocks())
		{
			Material type = block.getType();
			if (type == Material.AIR)
			{
				type = event.getEffectiveMaterial();
			}
			
			Location location = BukkitAdapter.adapt(block.getLocation());
			ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(location);
			
			// Check allow-block-place first
			Set<Material> allowSet = regions.queryValue(localPlayer, Flags.ALLOW_BLOCK_PLACE);
			if (allowSet != null && !allowSet.isEmpty() && allowSet.contains(type))
			{
				event.setResult(Event.Result.ALLOW);
				continue;
			}
			
			// Check deny-block-place
			Set<Material> denySet = regions.queryValue(localPlayer, Flags.DENY_BLOCK_PLACE);
			if (denySet != null && !denySet.isEmpty() && denySet.contains(type))
			{
				event.setResult(Event.Result.DENY);
				return;
			}
			
			// Restore original result if no flags matched
			event.setResult(originalResult);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockBreakEvent(BreakBlockEvent event)
	{
		Event.Result originalResult = event.getResult();
		Object cause = event.getCause().getRootCause();
		
		if (!(cause instanceof Player player))
		{
			return;
		}
		
		LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
		if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
		{
			return;
		}
		
		for (Block block : event.getBlocks())
		{
			Material type = block.getType();
			Location location = BukkitAdapter.adapt(block.getLocation());
			ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(location);
			
			// Check allow-block-break first
			Set<Material> allowSet = regions.queryValue(localPlayer, Flags.ALLOW_BLOCK_BREAK);
			if (allowSet != null && !allowSet.isEmpty() && allowSet.contains(type))
			{
				event.setResult(Event.Result.ALLOW);
				continue;
			}
			
			// Check deny-block-break
			Set<Material> denySet = regions.queryValue(localPlayer, Flags.DENY_BLOCK_BREAK);
			if (denySet != null && !denySet.isEmpty() && denySet.contains(type))
			{
				event.setResult(Event.Result.DENY);
				return;
			}
			
			// Restore original result if no flags matched
			event.setResult(originalResult);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityBlockFormEvent(EntityBlockFormEvent event)
	{
		BlockState newState = event.getNewState();
		if (newState.getType() == Material.FROSTED_ICE)
		{
			Location location = BukkitAdapter.adapt(newState.getLocation());

			LocalPlayer localPlayer;
			if (event.getEntity() instanceof Player player)
			{
				localPlayer = this.worldGuardPlugin.wrapPlayer(player);
				if (this.sessionManager.hasBypass(localPlayer, (World) location.getExtent()))
				{
					return;
				}
			}
			else
			{
				localPlayer = null;
			}

			if (this.regionContainer.createQuery().queryValue(location, localPlayer, Flags.FROSTWALKER) == State.DENY)
			{
				event.setCancelled(true);
			}
		}
	}
}



