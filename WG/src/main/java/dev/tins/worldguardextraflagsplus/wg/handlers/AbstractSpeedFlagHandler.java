package dev.tins.worldguardextraflagsplus.wg.handlers;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import org.bukkit.entity.Player;

import dev.tins.worldguardextraflagsplus.wg.WorldGuardUtils;

public abstract class AbstractSpeedFlagHandler extends FlagValueChangeHandler<Double>
{
	private Float originalSpeed;
	
	protected AbstractSpeedFlagHandler(Session session, DoubleFlag flag)
	{
		super(session, flag);
	}
	
	protected abstract float getSpeed(Player player);
	protected abstract void setSpeed(Player player, float speed);

	@Override
	protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Double value)
	{
		this.handleValue(player, player.getWorld(), value);
	}

	@Override
	protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Double currentValue, Double lastValue, MoveType moveType)
	{
		this.handleValue(player, (World) to.getExtent(), currentValue);
		return true;
	}

	@Override
	protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Double lastValue, MoveType moveType)
	{
		this.handleValue(player, (World) to.getExtent(), null);
		return true;
	}

	private void handleValue(LocalPlayer player, World world, Double speed)
	{
		Player bukkitPlayer = ((BukkitPlayer) player).getPlayer();

		// Don't schedule tasks during shutdown
		if (!WorldGuardUtils.isPluginEnabled() || !bukkitPlayer.isOnline())
		{
			return;
		}
		
		// Clamp speed value before lambda (must be final or effectively final)
		double clampedSpeed = speed != null ? Math.max(-1.0, Math.min(1.0, speed)) : 0.0;
		final double finalSpeed = clampedSpeed;
		final boolean hasSpeed = speed != null;

		WorldGuardUtils.getScheduler().getScheduler().runAtEntity(bukkitPlayer, task -> {
			if (!this.getSession().getManager().hasBypass(player, world) && hasSpeed)
			{
				if (this.getSpeed(bukkitPlayer) != finalSpeed)
				{
					if (this.originalSpeed == null)
					{
						this.originalSpeed = this.getSpeed(bukkitPlayer);
					}
					
					this.setSpeed(bukkitPlayer, (float) finalSpeed);
				}
			}
			else
			{
				if (this.originalSpeed != null)
				{
					this.setSpeed(bukkitPlayer, this.originalSpeed);
					
					this.originalSpeed = null;
				}
			}
		});
	}
}



