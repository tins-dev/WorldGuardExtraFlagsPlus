package dev.tins.worldguardextraflagsplus.updater;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Update checker for WorldGuardExtraFlagsPlus
 * Checks for updates from GitHub releases, Spigot API, and Modrinth
 * Fully compatible with Folia (uses async operations)
 */
public class UpdateChecker
{
	private final JavaPlugin plugin;
	private final String currentVersion;
	private final int spigotResourceId;
	private final String githubRepository;
	private final String modrinthProjectId;
	
	/**
	 * Creates a new UpdateChecker instance
	 * 
	 * @param plugin The plugin instance
	 * @param spigotResourceId Spigot resource ID (0 to disable Spigot checking)
	 * @param githubRepository GitHub repository in format "owner/repo" (null to disable GitHub checking)
	 * @param modrinthProjectId Modrinth project ID (null to disable Modrinth checking)
	 */
	public UpdateChecker(JavaPlugin plugin, int spigotResourceId, String githubRepository, String modrinthProjectId)
	{
		this.plugin = plugin;
		this.currentVersion = plugin.getDescription().getVersion();
		this.spigotResourceId = spigotResourceId;
		this.githubRepository = githubRepository;
		this.modrinthProjectId = modrinthProjectId;
	}
	
	/**
	 * Checks for updates asynchronously
	 * Logs results to console if update is available
	 * Checks Spigot, GitHub, and Modrinth if configured
	 * Fully Folia-compatible (uses async operations)
	 */
	public void checkForUpdates()
	{
		// Check Spigot if resource ID is configured
		if (spigotResourceId > 0)
		{
			checkSpigotUpdates();
		}
		
		// Check GitHub if repository is configured
		if (githubRepository != null && !githubRepository.isEmpty())
		{
			checkGitHubUpdates();
		}
		
		// Check Modrinth if project ID is configured
		if (modrinthProjectId != null && !modrinthProjectId.isEmpty())
		{
			checkModrinthUpdates();
		}
		
		// If none are configured, log warning
		if (spigotResourceId <= 0 && (githubRepository == null || githubRepository.isEmpty()) && (modrinthProjectId == null || modrinthProjectId.isEmpty()))
		{
			plugin.getLogger().info("Update checker is disabled (no update sources configured)");
		}
	}
	
	/**
	 * Checks for updates from Spigot API
	 */
	private void checkSpigotUpdates()
	{
		CompletableFuture.supplyAsync(() -> {
			try
			{
				URL url = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + spigotResourceId).toURL();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
				{
					String latestVersion = reader.readLine();
					if (latestVersion != null && !latestVersion.isEmpty())
					{
						return latestVersion.trim();
					}
				}
			}
			catch (Exception e)
			{
				plugin.getLogger().log(Level.WARNING, "Failed to check for updates from Spigot: " + e.getMessage());
			}
			return null;
		}).thenAccept(latestVersion -> {
			if (latestVersion != null && !latestVersion.equals(currentVersion))
			{
				plugin.getLogger().info("=========================================");
				plugin.getLogger().info("[Spigot] An update is available!");
				plugin.getLogger().info("Current version: " + currentVersion);
				plugin.getLogger().info("Latest version: " + latestVersion);
				plugin.getLogger().info("Download: https://www.spigotmc.org/resources/" + spigotResourceId);
				plugin.getLogger().info("=========================================");
			}
			else if (latestVersion != null)
			{
				plugin.getLogger().info("[Spigot] You are running the latest version (" + currentVersion + ")");
			}
		});
	}
	
	/**
	 * Checks for updates from GitHub releases
	 */
	private void checkGitHubUpdates()
	{
		CompletableFuture.supplyAsync(() -> {
			try
			{
				URL url = URI.create("https://api.github.com/repos/" + githubRepository + "/releases/latest").toURL();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
				{
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null)
					{
						response.append(line);
					}
					
					// Simple JSON parsing for "tag_name" field
					String json = response.toString();
					int tagIndex = json.indexOf("\"tag_name\"");
					if (tagIndex != -1)
					{
						int startIndex = json.indexOf("\"", tagIndex + 10) + 1;
						int endIndex = json.indexOf("\"", startIndex);
						if (startIndex > 0 && endIndex > startIndex)
						{
							String tag = json.substring(startIndex, endIndex);
							// Remove 'v' prefix if present
							if (tag.startsWith("v"))
							{
								tag = tag.substring(1);
							}
							return tag;
						}
					}
				}
			}
			catch (Exception e)
			{
				plugin.getLogger().log(Level.WARNING, "Failed to check for updates from GitHub: " + e.getMessage());
			}
			return null;
		}).thenAccept(latestVersion -> {
			if (latestVersion != null && !latestVersion.equals(currentVersion))
			{
				plugin.getLogger().info("=========================================");
				plugin.getLogger().info("[GitHub] An update is available!");
				plugin.getLogger().info("Current version: " + currentVersion);
				plugin.getLogger().info("Latest version: " + latestVersion);
				plugin.getLogger().info("Download: https://github.com/" + githubRepository + "/releases/latest");
				plugin.getLogger().info("=========================================");
			}
			else if (latestVersion != null)
			{
				plugin.getLogger().info("[GitHub] You are running the latest version (" + currentVersion + ")");
			}
		});
	}
	
	/**
	 * Checks for updates from Modrinth API
	 */
	private void checkModrinthUpdates()
	{
		CompletableFuture.supplyAsync(() -> {
			try
			{
				URL url = URI.create("https://api.modrinth.com/v2/project/" + modrinthProjectId).toURL();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestProperty("User-Agent", "WorldGuardExtraFlagsPlus/" + currentVersion);
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
				{
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null)
					{
						response.append(line);
					}
					
					// Parse JSON for "versions" array - get the latest version
					String json = response.toString();
					
					// Find "versions" array
					int versionsIndex = json.indexOf("\"versions\"");
					if (versionsIndex != -1)
					{
						// Get versions array
						int arrayStart = json.indexOf("[", versionsIndex);
						int arrayEnd = json.indexOf("]", arrayStart);
						if (arrayStart != -1 && arrayEnd != -1)
						{
							String versionsArray = json.substring(arrayStart + 1, arrayEnd);
							// Get the first version ID (latest version is usually first)
							int firstQuote = versionsArray.indexOf("\"");
							int secondQuote = versionsArray.indexOf("\"", firstQuote + 1);
							if (firstQuote != -1 && secondQuote != -1)
							{
								String versionId = versionsArray.substring(firstQuote + 1, secondQuote);
								
								// Now fetch the version details to get the version number
								return fetchModrinthVersionNumber(versionId);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				plugin.getLogger().log(Level.WARNING, "Failed to check for updates from Modrinth: " + e.getMessage());
			}
			return null;
		}).thenAccept(latestVersion -> {
			if (latestVersion != null && !latestVersion.equals(currentVersion))
			{
				plugin.getLogger().info("=========================================");
				plugin.getLogger().info("[Modrinth] An update is available!");
				plugin.getLogger().info("Current version: " + currentVersion);
				plugin.getLogger().info("Latest version: " + latestVersion);
				plugin.getLogger().info("Download: https://modrinth.com/plugin/" + modrinthProjectId);
				plugin.getLogger().info("=========================================");
			}
			else if (latestVersion != null)
			{
				plugin.getLogger().info("[Modrinth] You are running the latest version (" + currentVersion + ")");
			}
		});
	}
	
	/**
	 * Fetches the version number from Modrinth version ID
	 */
	private String fetchModrinthVersionNumber(String versionId)
	{
		try
		{
			URL url = URI.create("https://api.modrinth.com/v2/version/" + versionId).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("User-Agent", "WorldGuardExtraFlagsPlus/" + currentVersion);
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null)
				{
					response.append(line);
				}
				
				// Parse JSON for "version_number" field
				String json = response.toString();
				int versionNumberIndex = json.indexOf("\"version_number\"");
				if (versionNumberIndex != -1)
				{
					int startIndex = json.indexOf("\"", versionNumberIndex + 17) + 1;
					int endIndex = json.indexOf("\"", startIndex);
					if (startIndex > 0 && endIndex > startIndex)
					{
						String version = json.substring(startIndex, endIndex);
						// Remove 'v' prefix if present
						if (version.startsWith("v"))
						{
							version = version.substring(1);
						}
						return version;
					}
				}
			}
		}
		catch (Exception e)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to fetch Modrinth version details: " + e.getMessage());
		}
		return null;
	}
	
}

