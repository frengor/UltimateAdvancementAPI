package org.bukkit.craftbukkit.serverVersion1_17_R1;

import org.bukkit.BanList;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.Tag;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class FakeServer implements Server {
    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public String getVersion() {
        return null;
    }

    @NotNull
    @Override
    public String getBukkitVersion() {
        return null;
    }

    @NotNull
    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return null;
    }

    @Override
    public int getMaxPlayers() {
        return 0;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @NotNull
    @Override
    public String getIp() {
        return null;
    }

    @NotNull
    @Override
    public String getWorldType() {
        return null;
    }

    @Override
    public boolean getGenerateStructures() {
        return false;
    }

    @Override
    public int getMaxWorldSize() {
        return 0;
    }

    @Override
    public boolean getAllowEnd() {
        return false;
    }

    @Override
    public boolean getAllowNether() {
        return false;
    }

    @Override
    public boolean hasWhitelist() {
        return false;
    }

    @Override
    public void setWhitelist(boolean b) {

    }

    @Override
    public boolean isWhitelistEnforced() {
        return false;
    }

    @Override
    public void setWhitelistEnforced(boolean b) {

    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        return null;
    }

    @Override
    public void reloadWhitelist() {

    }

    @Override
    public int broadcastMessage(@NotNull String s) {
        return 0;
    }

    @NotNull
    @Override
    public String getUpdateFolder() {
        return null;
    }

    @NotNull
    @Override
    public File getUpdateFolderFile() {
        return null;
    }

    @Override
    public long getConnectionThrottle() {
        return 0;
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerWaterSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerWaterAmbientSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerAmbientSpawns() {
        return 0;
    }

    @Nullable
    @Override
    public Player getPlayer(@NotNull String s) {
        return null;
    }

    @Nullable
    @Override
    public Player getPlayerExact(@NotNull String s) {
        return null;
    }

    @NotNull
    @Override
    public List<Player> matchPlayer(@NotNull String s) {
        return null;
    }

    @Nullable
    @Override
    public Player getPlayer(@NotNull UUID uuid) {
        return null;
    }

    @NotNull
    @Override
    public PluginManager getPluginManager() {
        return null;
    }

    @NotNull
    @Override
    public BukkitScheduler getScheduler() {
        return null;
    }

    @NotNull
    @Override
    public ServicesManager getServicesManager() {
        return null;
    }

    @NotNull
    @Override
    public List<World> getWorlds() {
        return null;
    }

    @Nullable
    @Override
    public World createWorld(@NotNull WorldCreator worldCreator) {
        return null;
    }

    @Override
    public boolean unloadWorld(@NotNull String s, boolean b) {
        return false;
    }

    @Override
    public boolean unloadWorld(@NotNull World world, boolean b) {
        return false;
    }

    @Nullable
    @Override
    public World getWorld(@NotNull String s) {
        return null;
    }

    @Nullable
    @Override
    public World getWorld(@NotNull UUID uuid) {
        return null;
    }

    @Nullable
    @Override
    public MapView getMap(int i) {
        return null;
    }

    @NotNull
    @Override
    public MapView createMap(@NotNull World world) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType, int i, boolean b) {
        return null;
    }

    @Override
    public void reload() {

    }

    @Override
    public void reloadData() {

    }

    @NotNull
    @Override
    public Logger getLogger() {
        return null;
    }

    @Nullable
    @Override
    public PluginCommand getPluginCommand(@NotNull String s) {
        return null;
    }

    @Override
    public void savePlayers() {

    }

    @Override
    public boolean dispatchCommand(@NotNull CommandSender commandSender, @NotNull String s) throws CommandException {
        return false;
    }

    @Override
    public boolean addRecipe(@Nullable Recipe recipe) {
        return false;
    }

    @NotNull
    @Override
    public List<Recipe> getRecipesFor(@NotNull ItemStack itemStack) {
        return null;
    }

    @Nullable
    @Override
    public Recipe getRecipe(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Nullable
    @Override
    public Recipe getCraftingRecipe(@NotNull ItemStack[] itemStacks, @NotNull World world) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack craftItem(@NotNull ItemStack[] itemStacks, @NotNull World world, @NotNull Player player) {
        return null;
    }

    @NotNull
    @Override
    public Iterator<Recipe> recipeIterator() {
        return null;
    }

    @Override
    public void clearRecipes() {

    }

    @Override
    public void resetRecipes() {

    }

    @Override
    public boolean removeRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @NotNull
    @Override
    public Map<String, String[]> getCommandAliases() {
        return null;
    }

    @Override
    public int getSpawnRadius() {
        return 0;
    }

    @Override
    public void setSpawnRadius(int i) {

    }

    @Override
    public boolean getOnlineMode() {
        return false;
    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int broadcast(@NotNull String s, @NotNull String s1) {
        return 0;
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer(@NotNull String s) {
        return null;
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer(@NotNull UUID uuid) {
        return null;
    }

    @NotNull
    @Override
    public Set<String> getIPBans() {
        return null;
    }

    @Override
    public void banIP(@NotNull String s) {

    }

    @Override
    public void unbanIP(@NotNull String s) {

    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        return null;
    }

    @NotNull
    @Override
    public BanList getBanList(@NotNull BanList.Type type) {
        return null;
    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getOperators() {
        return null;
    }

    @NotNull
    @Override
    public GameMode getDefaultGameMode() {
        return null;
    }

    @Override
    public void setDefaultGameMode(@NotNull GameMode gameMode) {

    }

    @NotNull
    @Override
    public ConsoleCommandSender getConsoleSender() {
        return null;
    }

    @NotNull
    @Override
    public File getWorldContainer() {
        return null;
    }

    @NotNull
    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        return new OfflinePlayer[0];
    }

    @NotNull
    @Override
    public Messenger getMessenger() {
        return null;
    }

    @NotNull
    @Override
    public HelpMap getHelpMap() {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType) {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType, @NotNull String s) {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i, @NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public Merchant createMerchant(@Nullable String s) {
        return null;
    }

    @Override
    public int getMonsterSpawnLimit() {
        return 0;
    }

    @Override
    public int getAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public int getAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public boolean isPrimaryThread() {
        return false;
    }

    @NotNull
    @Override
    public String getMotd() {
        return null;
    }

    @Nullable
    @Override
    public String getShutdownMessage() {
        return null;
    }

    @NotNull
    @Override
    public Warning.WarningState getWarningState() {
        return null;
    }

    @NotNull
    @Override
    public ItemFactory getItemFactory() {
        return null;
    }

    @Nullable
    @Override
    public ScoreboardManager getScoreboardManager() {
        return null;
    }

    @Nullable
    @Override
    public CachedServerIcon getServerIcon() {
        return null;
    }

    @NotNull
    @Override
    public CachedServerIcon loadServerIcon(@NotNull File file) throws IllegalArgumentException, Exception {
        return null;
    }

    @NotNull
    @Override
    public CachedServerIcon loadServerIcon(@NotNull BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
        return null;
    }

    @Override
    public void setIdleTimeout(int i) {

    }

    @Override
    public int getIdleTimeout() {
        return 0;
    }

    @NotNull
    @Override
    public ChunkGenerator.ChunkData createChunkData(@NotNull World world) {
        return null;
    }

    @NotNull
    @Override
    public BossBar createBossBar(@Nullable String s, @NotNull BarColor barColor, @NotNull BarStyle barStyle, @NotNull BarFlag... barFlags) {
        return null;
    }

    @NotNull
    @Override
    public KeyedBossBar createBossBar(@NotNull NamespacedKey namespacedKey, @Nullable String s, @NotNull BarColor barColor, @NotNull BarStyle barStyle, @NotNull BarFlag... barFlags) {
        return null;
    }

    @NotNull
    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        return null;
    }

    @Nullable
    @Override
    public KeyedBossBar getBossBar(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public boolean removeBossBar(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Nullable
    @Override
    public Entity getEntity(@NotNull UUID uuid) {
        return null;
    }

    @Nullable
    @Override
    public Advancement getAdvancement(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @NotNull
    @Override
    public Iterator<Advancement> advancementIterator() {
        return null;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull Material material) {
        return null;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull Material material, @Nullable Consumer<BlockData> consumer) {
        return null;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@Nullable Material material, @Nullable String s) throws IllegalArgumentException {
        return null;
    }

    @Nullable
    @Override
    public <T extends Keyed> Tag<T> getTag(@NotNull String s, @NotNull NamespacedKey namespacedKey, @NotNull Class<T> aClass) {
        return null;
    }

    @NotNull
    @Override
    public <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String s, @NotNull Class<T> aClass) {
        return null;
    }

    @Nullable
    @Override
    public LootTable getLootTable(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @NotNull
    @Override
    public List<Entity> selectEntities(@NotNull CommandSender commandSender, @NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public UnsafeValues getUnsafe() {
        return null;
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin plugin, @NotNull String s, @NotNull byte[] bytes) {

    }

    @NotNull
    @Override
    public Set<String> getListeningPluginChannels() {
        return null;
    }

    @Override
    public int getTicksPerWaterUndergroundCreatureSpawns() {
        return 0;
    }

    @Override
    public int getWaterUndergroundCreatureSpawnLimit() {
        return 0;
    }

    @NotNull
    @Override
    public StructureManager getStructureManager() {
        return null;
    }

    @Override
    public int getSimulationDistance() {
        return 0;
    }

    @Override
    public boolean getHideOnlinePlayers() {
        return false;
    }
}
