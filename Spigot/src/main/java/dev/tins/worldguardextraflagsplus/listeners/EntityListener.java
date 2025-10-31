package dev.tins.worldguardextraflagsplus.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import dev.tins.worldguardextraflagsplus.flags.helpers.ForcedStateFlag;
import dev.tins.worldguardextraflagsplus.wg.WorldGuardUtils;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.ChatColor;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import lombok.RequiredArgsConstructor;
import dev.tins.worldguardextraflagsplus.flags.Flags;
import dev.tins.worldguardextraflagsplus.flags.helpers.BlockableItemFlag;

import java.util.Set;

@RequiredArgsConstructor
public class EntityListener implements Listener
{
	private final WorldGuardPlugin worldGuardPlugin;
	private final RegionContainer regionContainer;
	private final SessionManager sessionManager;

	// Get blockable items list from the flag helper (single source of truth)
	private static final Set<String> BLOCKABLE_ITEMS = BlockableItemFlag.getBlockableItems();

	@EventHandler(ignoreCancelled = true)
	public void onPortalCreateEvent(PortalCreateEvent event)
	{
		LocalPlayer localPlayer;
		if (event.getEntity() instanceof Player player)
		{
			localPlayer = this.worldGuardPlugin.wrapPlayer(player);
			if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
			{
				return;
			}
		}
		else
		{
			localPlayer = null;
		}

		for (BlockState block : event.getBlocks())
		{
			if (this.regionContainer.createQuery().queryState(BukkitAdapter.adapt(block.getLocation()), localPlayer, Flags.NETHER_PORTALS) == State.DENY)
			{
				event.setCancelled(true);
				break;
			}
		}
	}

    private boolean isBlocked(LocalPlayer localPlayer, Material material)
    {
        String name = material.name();
        
        // Early exit: only check flag if item is in our hardcoded blockable list
        if (!BLOCKABLE_ITEMS.contains(name))
        {
            return false;
        }
        
        // Check if flag is set in region
        ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(localPlayer.getLocation());
        java.util.Set<String> set = regions.queryValue(localPlayer, Flags.PERMIT_COMPLETELY);
        if (set == null || set.isEmpty())
        {
            return false;
        }
        
        // Case-insensitive check against flag set
        for (String item : set)
        {
            if (item != null && item.equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    private void sendBlocked(Player player, String itemName)
    {
        player.sendMessage(ChatColor.RED + "Hey!" + ChatColor.GRAY + " You can not use " + itemName + " in here!");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
        {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) return;
        Material mat = item.getType();
        if (mat == Material.AIR) return;
        if (this.isBlocked(localPlayer, mat))
        {
            event.setCancelled(true);
            this.sendBlocked(player, mat.name());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
        {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        Material mat = item != null ? item.getType() : Material.AIR;
        if (mat != Material.AIR && this.isBlocked(localPlayer, mat))
        {
            event.setCancelled(true);
            this.sendBlocked(player, mat.name());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player player))
        {
            return;
        }
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
        {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        Material mat = item != null ? item.getType() : Material.AIR;
        if (mat != Material.AIR && this.isBlocked(localPlayer, mat))
        {
            event.setCancelled(true);
            this.sendBlocked(player, mat.name());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectile(ProjectileLaunchEvent event)
    {
        if (!(event.getEntity().getShooter() instanceof Player player))
        {
            return;
        }
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
        {
            return;
        }
        // Try to infer from main hand item
        ItemStack item = player.getInventory().getItemInMainHand();
        Material mat = item != null ? item.getType() : Material.AIR;
        if (mat != Material.AIR && this.isBlocked(localPlayer, mat))
        {
            event.setCancelled(true);
            this.sendBlocked(player, mat.name());
        }
    }

	@EventHandler(ignoreCancelled = true)
	public void onEntityToggleGlideEvent(EntityToggleGlideEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player player)
		{
			LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
			if (this.sessionManager.hasBypass(localPlayer, localPlayer.getWorld()))
			{
				return;
			}

			ForcedStateFlag.ForcedState state = this.regionContainer.createQuery().queryValue(localPlayer.getLocation(), localPlayer, Flags.GLIDE);
			switch(state)
			{
				case ALLOW:
					break;
				case DENY:
				{
					if (!event.isGliding())
					{
						return;
					}

					event.setCancelled(true);

					//Prevent the player from being allowed to glide by spamming space
					// Push player down slightly to cancel upward momentum
					WorldGuardUtils.getScheduler().runAtEntity(player, task -> {
						org.bukkit.util.Vector velocity = player.getVelocity();
						velocity.setY(Math.min(velocity.getY(), -0.5));
						player.setVelocity(velocity);
					});

					break;
				}
				case FORCE:
				{
					if (event.isGliding())
					{
						return;
					}

					event.setCancelled(true);

					break;
				}
			}
		}
	}
}



