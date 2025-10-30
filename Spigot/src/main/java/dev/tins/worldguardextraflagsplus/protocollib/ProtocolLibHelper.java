package dev.tins.worldguardextraflagsplus.protocollib;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;

import lombok.Getter;
import dev.tins.worldguardextraflagsplus.WorldGuardExtraFlagsPlusPlugin;

public class ProtocolLibHelper
{
	@Getter private final WorldGuardExtraFlagsPlusPlugin plugin;
	@Getter private final Plugin protocolLibPlugin;
	
	public ProtocolLibHelper(WorldGuardExtraFlagsPlusPlugin plugin, Plugin protocolLibPlugin)
	{
		this.plugin = plugin;
		this.protocolLibPlugin = protocolLibPlugin;
	}

	public void onEnable()
	{
		ProtocolLibrary.getProtocolManager().addPacketListener(new RemoveEffectPacketListener());
	}
}



