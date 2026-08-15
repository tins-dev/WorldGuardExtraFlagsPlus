package dev.tins.worldguardextraflagsplus.wg.handlers;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;

import com.tcoded.folialib.wrapper.task.WrappedTask;

import dev.tins.worldguardextraflagsplus.flags.Flags;
import dev.tins.worldguardextraflagsplus.wg.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the {@code console-command-timer} flag.
 *
 * <p>Each entry in the flag set has the format {@code <seconds> <command>}. The handler
 * validates that the interval is between 1 and 300 seconds, then schedules a repeating
 * Folia-safe task on the player's entity thread that dispatches the command from console
 * with placeholder replacement.</p>
 *
 * <p><b>Reset on exit:</b> Unlike {@link ConsoleCommandRepeatFlagHandler}, this handler
 * does not persist cooldown across region exits. When the player leaves the region (or the
 * flag becomes absent), all timers are cancelled and state is cleared. On re-entry the
 * player must wait the full interval again before the first payout — hold-to-earn semantics
 * for KOTH-style rewards.</p>
 *
 * <p>On the very first entry, the command does <b>not</b> fire immediately — the player
 * must wait the full interval once.</p>
 */
public class ConsoleCommandTimerFlagHandler extends FlagValueChangeHandler<Set<String>>
{
	public static final Factory FACTORY()
	{
		return new Factory();
	}

	public static class Factory extends Handler.Factory<ConsoleCommandTimerFlagHandler>
	{
		@Override
		public ConsoleCommandTimerFlagHandler create(Session session)
		{
			return new ConsoleCommandTimerFlagHandler(session);
		}
	}

	/**
	 * Map of entry string -> WrappedRunnable for each active repeating task.
	 * Key is the raw flag entry (e.g. "60 give %player% diamond 1") so we can
	 * identify and cancel individual entries when the set changes.
	 */
	private final Map<String, WrappedRunnable> runnables;

	protected ConsoleCommandTimerFlagHandler(Session session)
	{
		super(session, Flags.CONSOLE_COMMAND_TIMER);

		this.runnables = new ConcurrentHashMap<>();
	}

	@Override
	protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Set<String> value)
	{
		this.handleValue(player, value);
	}

	@Override
	protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
			Set<String> currentValue, Set<String> lastValue, MoveType moveType)
	{
		this.handleValue(player, currentValue);
		return true;
	}

	@Override
	protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
			Set<String> lastValue, MoveType moveType)
	{
		this.handleValue(player, null);
		return true;
	}

	@Override
	public void tick(LocalPlayer player, ApplicableRegionSet set)
	{
		this.handleValue(player, set.queryValue(player, Flags.CONSOLE_COMMAND_TIMER));
	}

	// -------------------------------------------------------------------------
	// Core logic
	// -------------------------------------------------------------------------

	/**
	 * Compares the new value set against currently running timers and starts or
	 * stops entries as needed.
	 *
	 * <p>Each active entry always waits the full interval before firing — there is
	 * no cooldown persistence across region exits or re-entries.</p>
	 */
	private void handleValue(LocalPlayer player, Set<String> value)
	{
		if ((value == null || value.isEmpty()) && this.runnables.isEmpty())
		{
			return;
		}

		Player bukkitPlayer = ((BukkitPlayer) player).getPlayer();
		if (bukkitPlayer == null || !bukkitPlayer.isOnline())
		{
			return;
		}

		if (value != null && !value.isEmpty())
		{
			for (String entry : value)
			{
				if (entry == null || entry.isEmpty() || this.runnables.containsKey(entry))
				{
					continue;
				}

				TimerCommand parsed = parseEntry(entry);
				if (parsed == null)
				{
					continue; // Invalid entry, skip silently
				}

				final TimerCommand parsedCommand = parsed;

				WrappedRunnable runnable = new WrappedRunnable()
				{
					private WrappedTask wrappedTask;

					@Override
					public void run()
					{
						String processed = CommandPlaceholderUtil.prepareForDispatch(player, parsedCommand.command);
						if (!processed.isEmpty())
						{
							WorldGuardUtils.getScheduler().runNextTick(task ->
							{
								CommandSender console = Bukkit.getServer().getConsoleSender();
								Bukkit.getServer().dispatchCommand(console, processed);
							});
						}
					}

					@Override
					public void cancel()
					{
						if (wrappedTask != null)
						{
							wrappedTask.cancel();
						}
					}

					@Override
					public void setWrappedTask(WrappedTask task)
					{
						this.wrappedTask = task;
					}
				};

				WrappedTask task = WorldGuardUtils.getScheduler().runAtEntityTimer(
						bukkitPlayer,
						runnable,
						parsedCommand.intervalMillis,
						parsedCommand.intervalMillis,
						TimeUnit.MILLISECONDS);
				runnable.setWrappedTask(task);

				this.runnables.put(entry, runnable);
			}
		}

		// Cancel timers for entries no longer in the set, or when the flag is absent.
		// All state is cleared — no cooldown carries across exits.
		Iterator<Entry<String, WrappedRunnable>> iterator = this.runnables.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, WrappedRunnable> entry = iterator.next();

			if (value != null && value.contains(entry.getKey()))
			{
				continue;
			}

			entry.getValue().cancel();
			iterator.remove();
		}
	}

	// -------------------------------------------------------------------------
	// Entry parsing
	// -------------------------------------------------------------------------

	/**
	 * Parses a flag entry of the format {@code "<seconds> <command>"}.
	 *
	 * @param entry the raw flag value
	 * @return parsed data, or {@code null} if the entry is invalid
	 */
	private static TimerCommand parseEntry(String entry)
	{
		if (entry == null || entry.isEmpty())
		{
			return null;
		}

		int firstSpace = entry.indexOf(' ');
		if (firstSpace <= 0 || firstSpace >= entry.length() - 1)
		{
			return null;
		}

		String secondsStr = entry.substring(0, firstSpace).trim();
		String command = entry.substring(firstSpace + 1).trim();

		if (command.isEmpty())
		{
			return null;
		}

		int seconds;
		try
		{
			seconds = Integer.parseInt(secondsStr);
		}
		catch (NumberFormatException e)
		{
			return null;
		}

		if (seconds < 1 || seconds > 300)
		{
			return null;
		}

		return new TimerCommand(command, seconds * 1000L);
	}

	/**
	 * Holds the parsed command and pre-computed interval in milliseconds.
	 */
	private record TimerCommand(String command, long intervalMillis) {}

	// -------------------------------------------------------------------------
	// Runnable wrapper (same pattern as PlaySoundsFlagHandler)
	// -------------------------------------------------------------------------

	interface WrappedRunnable extends Runnable
	{
		void cancel();

		@Override
		void run();

		void setWrappedTask(WrappedTask wrappedTask);
	}
}
