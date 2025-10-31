package dev.tins.worldguardextraflagsplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Messages
{
	private static JavaPlugin plugin;
	private static FileConfiguration messages;
	private static File messagesFile;

	public static void initialize(JavaPlugin plugin)
	{
		Messages.plugin = plugin;
		
		// Get WorldGuard plugin data folder
		File worldGuardDataFolder = plugin.getServer().getPluginManager().getPlugin("WorldGuard").getDataFolder();
		
		// Create messages.yml in WorldGuard folder
		messagesFile = new File(worldGuardDataFolder, "messages.yml");
		
		// Copy default messages.yml if it doesn't exist
		if (!messagesFile.exists())
		{
			saveDefaultMessages(worldGuardDataFolder);
		}
		
		// Load messages.yml
		reloadMessages();
	}

	private static void saveDefaultMessages(File worldGuardDataFolder)
	{
		try
		{
			// Ensure WorldGuard folder exists
			if (!worldGuardDataFolder.exists())
			{
				worldGuardDataFolder.mkdirs();
			}
			
			// Check if messages.yml already exists in WorldGuard folder
			if (messagesFile.exists())
			{
				// File already exists, don't overwrite it (admin might have customized it)
				plugin.getLogger().info("messages.yml already exists in WorldGuard folder, skipping default copy.");
				return;
			}
			
			// Load default messages from plugin resources
			InputStream defaultStream = plugin.getResource("messages.yml");
			if (defaultStream == null)
			{
				plugin.getLogger().warning("Default messages.yml not found in plugin resources!");
				return;
			}
			
			// Copy file directly using streams (simpler than loading YAML)
			java.io.FileOutputStream outputStream = new java.io.FileOutputStream(messagesFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = defaultStream.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, length);
			}
			outputStream.close();
			defaultStream.close();
			
			plugin.getLogger().info("Created messages.yml in WorldGuard folder: " + messagesFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			plugin.getLogger().log(Level.SEVERE, "Failed to save default messages.yml", e);
		}
	}

	public static void reloadMessages()
	{
		try
		{
			messages = YamlConfiguration.loadConfiguration(messagesFile);
			
			// Load UTF-8 encoding properly
			InputStream defaultStream = plugin.getResource("messages.yml");
			if (defaultStream != null)
			{
				InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8);
				YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
				messages.setDefaults(defaultConfig);
			}
			
			plugin.getLogger().info("Loaded messages from: " + messagesFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			plugin.getLogger().log(Level.SEVERE, "Failed to load messages.yml", e);
			// Fallback: use in-memory configuration
			messages = new YamlConfiguration();
		}
	}

	/**
	 * Gets a message from the configuration and translates color codes.
	 * Supports placeholders: {key} will be replaced with values
	 * Returns null if message is empty (disabled)
	 */
	public static String getMessage(String key, String... replacements)
	{
		String message = messages.getString(key, "&cMessage not found: " + key);
		
		// If message is empty string, return null (message disabled)
		if (message == null || message.trim().isEmpty())
		{
			return null;
		}
		
		// Replace placeholders
		for (int i = 0; i < replacements.length; i += 2)
		{
			if (i + 1 < replacements.length)
			{
				message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
			}
		}
		
		// Translate color codes
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Gets a raw message from the configuration without color translation.
	 * Useful for combining multiple messages.
	 */
	public static String getRawMessage(String key)
	{
		return messages.getString(key, "");
	}

	public static File getMessagesFile()
	{
		return messagesFile;
	}
}

