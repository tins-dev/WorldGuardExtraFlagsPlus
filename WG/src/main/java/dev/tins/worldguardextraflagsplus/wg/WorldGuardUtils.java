package dev.tins.worldguardextraflagsplus.wg;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;

public class WorldGuardUtils
{
	public static final String PREVENT_TELEPORT_LOOP_META = "WGEFP: TLP";
	
	private static FoliaLib foliaLib;
	private static SchedulerWrapper schedulerWrapper;
	
	public static void initializeScheduler(Plugin plugin)
	{
		WorldGuardUtils.foliaLib = new FoliaLib(plugin);
		WorldGuardUtils.schedulerWrapper = new SchedulerWrapper(foliaLib);
	}
	
	public static SchedulerWrapper getScheduler()
	{
		return schedulerWrapper;
	}
	
	public static class SchedulerWrapper
	{
		private final FoliaLib foliaLib;
		
		private SchedulerWrapper(FoliaLib foliaLib)
		{
			this.foliaLib = foliaLib;
		}
		
		public FoliaLib getImpl()
		{
			return foliaLib;
		}
		
		public com.tcoded.folialib.impl.ServerImplementation getScheduler()
		{
			return foliaLib.getScheduler();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static boolean hasNoTeleportLoop(Plugin plugin, Player player, Object location)
	{
		MetadataValue result = player.getMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META).stream()
				.filter((p) -> p.getOwningPlugin().equals(plugin))
				.findFirst()
				.orElse(null);
		
		if (result == null)
		{
			result = new FixedMetadataValue(plugin, new HashSet<>());
			
			player.setMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META, result);
			
			if (schedulerWrapper != null && foliaLib != null)
			{
				schedulerWrapper.getScheduler().runAtEntity(player, (wrappedTask) ->
				{
					player.removeMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META, plugin);
				});
			}
			else
			{
				// Fallback if scheduler not initialized
				plugin.getServer().getScheduler().runTask(plugin, () ->
				{
					player.removeMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META, plugin);
				});
			}
		}
		
		Set<Object> set = (Set<Object>)result.value();
		if (set.add(location))
		{
			return true;
		}
		
		return false;
	}
}



