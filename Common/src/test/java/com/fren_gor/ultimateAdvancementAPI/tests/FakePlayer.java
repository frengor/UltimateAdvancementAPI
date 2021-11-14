package com.fren_gor.ultimateAdvancementAPI.tests;

import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class FakePlayer implements Player {

    final UUID uuid;

    @Override
    public String toString() {
        return "FakePlayer{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FakePlayer that = (FakePlayer) o;

        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(@Nullable String s) {

    }

    @NotNull
    @Override
    public String getPlayerListName() {
        return null;
    }

    @Override
    public void setPlayerListName(@Nullable String s) {

    }

    @Nullable
    @Override
    public String getPlayerListHeader() {
        return null;
    }

    @Nullable
    @Override
    public String getPlayerListFooter() {
        return null;
    }

    @Override
    public void setPlayerListHeader(@Nullable String s) {

    }

    @Override
    public void setPlayerListFooter(@Nullable String s) {

    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable String s, @Nullable String s1) {

    }

    @Override
    public void setCompassTarget(@NotNull Location location) {

    }

    @NotNull
    @Override
    public Location getCompassTarget() {
        return null;
    }

    @Nullable
    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public void sendRawMessage(@NotNull String s) {

    }

    @Override
    public void kickPlayer(@Nullable String s) {

    }

    @Override
    public void chat(@NotNull String s) {

    }

    @Override
    public boolean performCommand(@NotNull String s) {
        return false;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public void setSneaking(boolean b) {

    }

    @Override
    public boolean isSprinting() {
        return false;
    }

    @Override
    public void setSprinting(boolean b) {

    }

    @Override
    public void saveData() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void setSleepingIgnored(boolean b) {

    }

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Nullable
    @Override
    public Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location) {

    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location, boolean b) {

    }

    @Override
    public void playNote(@NotNull Location location, byte b, byte b1) {

    }

    @Override
    public void playNote(@NotNull Location location, @NotNull Instrument instrument, @NotNull Note note) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String s, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String s, @NotNull SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void stopSound(@NotNull Sound sound) {

    }

    @Override
    public void stopSound(@NotNull String s) {

    }

    @Override
    public void stopSound(@NotNull Sound sound, @Nullable SoundCategory soundCategory) {

    }

    @Override
    public void stopSound(@NotNull String s, @Nullable SoundCategory soundCategory) {

    }

    @Override
    public void playEffect(@NotNull Location location, @NotNull Effect effect, int i) {

    }

    @Override
    public <T> void playEffect(@NotNull Location location, @NotNull Effect effect, @Nullable T t) {

    }

    @Override
    public boolean breakBlock(@NotNull Block block) {
        return false;
    }

    @Override
    public void sendBlockChange(@NotNull Location location, @NotNull Material material, byte b) {

    }

    @Override
    public void sendBlockChange(@NotNull Location location, @NotNull BlockData blockData) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float v) {

    }

    @Override
    public boolean sendChunkChange(@NotNull Location location, int i, int i1, int i2, @NotNull byte[] bytes) {
        return false;
    }

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings, @NotNull DyeColor dyeColor) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings, @NotNull DyeColor dyeColor, boolean b) throws IllegalArgumentException {

    }

    @Override
    public void sendMap(@NotNull MapView mapView) {

    }

    @Override
    public void updateInventory() {

    }

    @Override
    public void setPlayerTime(long l, boolean b) {

    }

    @Override
    public long getPlayerTime() {
        return 0;
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public void resetPlayerTime() {

    }

    @Override
    public void setPlayerWeather(@NotNull WeatherType weatherType) {

    }

    @Nullable
    @Override
    public WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {

    }

    @Override
    public void giveExp(int i) {

    }

    @Override
    public void giveExpLevels(int i) {

    }

    @Override
    public float getExp() {
        return 0;
    }

    @Override
    public void setExp(float v) {

    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int i) {

    }

    @Override
    public int getTotalExperience() {
        return 0;
    }

    @Override
    public void setTotalExperience(int i) {

    }

    @Override
    public void sendExperienceChange(float v) {

    }

    @Override
    public void sendExperienceChange(float v, int i) {

    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public void setAllowFlight(boolean b) {

    }

    @Override
    public void hidePlayer(@NotNull Player player) {

    }

    @Override
    public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {

    }

    @Override
    public void showPlayer(@NotNull Player player) {

    }

    @Override
    public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {

    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public void setFlying(boolean b) {

    }

    @Override
    public void setFlySpeed(float v) throws IllegalArgumentException {

    }

    @Override
    public void setWalkSpeed(float v) throws IllegalArgumentException {

    }

    @Override
    public float getFlySpeed() {
        return 0;
    }

    @Override
    public float getWalkSpeed() {
        return 0;
    }

    @Override
    public void setTexturePack(@NotNull String s) {

    }

    @Override
    public void setResourcePack(@NotNull String s) {

    }

    @Override
    public void setResourcePack(@NotNull String s, @NotNull byte[] bytes) {

    }

    @NotNull
    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(@NotNull Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean b) {

    }

    @Override
    public void setHealthScale(double v) throws IllegalArgumentException {

    }

    @Override
    public double getHealthScale() {
        return 0;
    }

    @Nullable
    @Override
    public Entity getSpectatorTarget() {
        return null;
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {

    }

    @Override
    public void sendTitle(@Nullable String s, @Nullable String s1) {

    }

    @Override
    public void sendTitle(@Nullable String s, @Nullable String s1, int i, int i1, int i2) {

    }

    @Override
    public void resetTitle() {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, @Nullable T t) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, @Nullable T t) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, @Nullable T t) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, @Nullable T t) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, double v3) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, double v3, @Nullable T t) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, @Nullable T t) {

    }

    @NotNull
    @Override
    public AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
        return null;
    }

    @Override
    public int getClientViewDistance() {
        return 0;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @NotNull
    @Override
    public String getLocale() {
        return null;
    }

    @Override
    public void updateCommands() {

    }

    @Override
    public void openBook(@NotNull ItemStack itemStack) {

    }

    @NotNull
    @Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean b) {

    }

    @Nullable
    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {

    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(@NotNull String s) {

    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {

    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent conversationAbandonedEvent) {

    }

    @Override
    public void sendRawMessage(@Nullable UUID uuid, @NotNull String s) {

    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public PlayerInventory getInventory() {
        return null;
    }

    @NotNull
    @Override
    public Inventory getEnderChest() {
        return null;
    }

    @NotNull
    @Override
    public MainHand getMainHand() {
        return null;
    }

    @Override
    public boolean setWindowProperty(@NotNull InventoryView.Property property, int i) {
        return false;
    }

    @NotNull
    @Override
    public InventoryView getOpenInventory() {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openInventory(@NotNull Inventory inventory) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openWorkbench(@Nullable Location location, boolean b) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openEnchanting(@Nullable Location location, boolean b) {
        return null;
    }

    @Override
    public void openInventory(@NotNull InventoryView inventoryView) {

    }

    @Nullable
    @Override
    public InventoryView openMerchant(@NotNull Villager villager, boolean b) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openMerchant(@NotNull Merchant merchant, boolean b) {
        return null;
    }

    @Override
    public void closeInventory() {

    }

    @NotNull
    @Override
    public ItemStack getItemInHand() {
        return null;
    }

    @Override
    public void setItemInHand(@Nullable ItemStack itemStack) {

    }

    @NotNull
    @Override
    public ItemStack getItemOnCursor() {
        return null;
    }

    @Override
    public void setItemOnCursor(@Nullable ItemStack itemStack) {

    }

    @Override
    public boolean hasCooldown(@NotNull Material material) {
        return false;
    }

    @Override
    public int getCooldown(@NotNull Material material) {
        return 0;
    }

    @Override
    public void setCooldown(@NotNull Material material, int i) {

    }

    @Override
    public int getSleepTicks() {
        return 0;
    }

    @Override
    public boolean sleep(@NotNull Location location, boolean b) {
        return false;
    }

    @Override
    public void wakeup(boolean b) {

    }

    @NotNull
    @Override
    public Location getBedLocation() {
        return null;
    }

    @NotNull
    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public void setGameMode(@NotNull GameMode gameMode) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public boolean isHandRaised() {
        return false;
    }

    @Nullable
    @Override
    public ItemStack getItemInUse() {
        return null;
    }

    @Override
    public int getExpToLevel() {
        return 0;
    }

    @Override
    public float getAttackCooldown() {
        return 0;
    }

    @Override
    public boolean discoverRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public int discoverRecipes(@NotNull Collection<NamespacedKey> collection) {
        return 0;
    }

    @Override
    public boolean undiscoverRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public int undiscoverRecipes(@NotNull Collection<NamespacedKey> collection) {
        return 0;
    }

    @Override
    public boolean hasDiscoveredRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return null;
    }

    @Nullable
    @Override
    public Entity getShoulderEntityLeft() {
        return null;
    }

    @Override
    public void setShoulderEntityLeft(@Nullable Entity entity) {

    }

    @Nullable
    @Override
    public Entity getShoulderEntityRight() {
        return null;
    }

    @Override
    public void setShoulderEntityRight(@Nullable Entity entity) {

    }

    @Override
    public boolean dropItem(boolean b) {
        return false;
    }

    @Override
    public float getExhaustion() {
        return 0;
    }

    @Override
    public void setExhaustion(float v) {

    }

    @Override
    public float getSaturation() {
        return 0;
    }

    @Override
    public void setSaturation(float v) {

    }

    @Override
    public int getFoodLevel() {
        return 0;
    }

    @Override
    public void setFoodLevel(int i) {

    }

    @Override
    public int getSaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setSaturatedRegenRate(int i) {

    }

    @Override
    public int getUnsaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setUnsaturatedRegenRate(int i) {

    }

    @Override
    public int getStarvationRate() {
        return 0;
    }

    @Override
    public void setStarvationRate(int i) {

    }

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public double getEyeHeight(boolean b) {
        return 0;
    }

    @NotNull
    @Override
    public Location getEyeLocation() {
        return null;
    }

    @NotNull
    @Override
    public List<Block> getLineOfSight(@Nullable Set<Material> set, int i) {
        return null;
    }

    @NotNull
    @Override
    public Block getTargetBlock(@Nullable Set<Material> set, int i) {
        return null;
    }

    @NotNull
    @Override
    public List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> set, int i) {
        return null;
    }

    @Nullable
    @Override
    public Block getTargetBlockExact(int i) {
        return null;
    }

    @Nullable
    @Override
    public Block getTargetBlockExact(int i, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(double v) {
        return null;
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(double v, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public int getRemainingAir() {
        return 0;
    }

    @Override
    public void setRemainingAir(int i) {

    }

    @Override
    public int getMaximumAir() {
        return 0;
    }

    @Override
    public void setMaximumAir(int i) {

    }

    @Override
    public int getArrowCooldown() {
        return 0;
    }

    @Override
    public void setArrowCooldown(int i) {

    }

    @Override
    public int getArrowsInBody() {
        return 0;
    }

    @Override
    public void setArrowsInBody(int i) {

    }

    @Override
    public int getMaximumNoDamageTicks() {
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int i) {

    }

    @Override
    public double getLastDamage() {
        return 0;
    }

    @Override
    public void setLastDamage(double v) {

    }

    @Override
    public int getNoDamageTicks() {
        return 0;
    }

    @Override
    public void setNoDamageTicks(int i) {

    }

    @Nullable
    @Override
    public Player getKiller() {
        return null;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect potionEffect) {
        return false;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect potionEffect, boolean b) {
        return false;
    }

    @Override
    public boolean addPotionEffects(@NotNull Collection<PotionEffect> collection) {
        return false;
    }

    @Override
    public boolean hasPotionEffect(@NotNull PotionEffectType potionEffectType) {
        return false;
    }

    @Nullable
    @Override
    public PotionEffect getPotionEffect(@NotNull PotionEffectType potionEffectType) {
        return null;
    }

    @Override
    public void removePotionEffect(@NotNull PotionEffectType potionEffectType) {

    }

    @NotNull
    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return false;
    }

    @Override
    public void setRemoveWhenFarAway(boolean b) {

    }

    @Nullable
    @Override
    public EntityEquipment getEquipment() {
        return null;
    }

    @Override
    public void setCanPickupItems(boolean b) {

    }

    @Override
    public boolean getCanPickupItems() {
        return false;
    }

    @Override
    public boolean isLeashed() {
        return false;
    }

    @NotNull
    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        return null;
    }

    @Override
    public boolean setLeashHolder(@Nullable Entity entity) {
        return false;
    }

    @Override
    public boolean isGliding() {
        return false;
    }

    @Override
    public void setGliding(boolean b) {

    }

    @Override
    public boolean isSwimming() {
        return false;
    }

    @Override
    public void setSwimming(boolean b) {

    }

    @Override
    public boolean isRiptiding() {
        return false;
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public void setAI(boolean b) {

    }

    @Override
    public boolean hasAI() {
        return false;
    }

    @Override
    public void attack(@NotNull Entity entity) {

    }

    @Override
    public void swingMainHand() {

    }

    @Override
    public void swingOffHand() {

    }

    @Override
    public void setCollidable(boolean b) {

    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @NotNull
    @Override
    public Set<UUID> getCollidableExemptions() {
        return null;
    }

    @Nullable
    @Override
    public <T> T getMemory(@NotNull MemoryKey<T> memoryKey) {
        return null;
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T t) {

    }

    @NotNull
    @Override
    public EntityCategory getCategory() {
        return null;
    }

    @Override
    public void setInvisible(boolean b) {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Nullable
    @Override
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return null;
    }

    @Override
    public void damage(double v) {

    }

    @Override
    public void damage(double v, @Nullable Entity entity) {

    }

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public void setHealth(double v) {

    }

    @Override
    public double getAbsorptionAmount() {
        return 0;
    }

    @Override
    public void setAbsorptionAmount(double v) {

    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public void setMaxHealth(double v) {

    }

    @Override
    public void resetMaxHealth() {

    }

    @NotNull
    @Override
    public Location getLocation() {
        return null;
    }

    @Nullable
    @Override
    public Location getLocation(@Nullable Location location) {
        return null;
    }

    @Override
    public void setVelocity(@NotNull Vector vector) {

    }

    @NotNull
    @Override
    public Vector getVelocity() {
        return null;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @NotNull
    @Override
    public BoundingBox getBoundingBox() {
        return null;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @NotNull
    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public void setRotation(float v, float v1) {

    }

    @Override
    public boolean teleport(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause teleportCause) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Entity entity, @NotNull PlayerTeleportEvent.TeleportCause teleportCause) {
        return false;
    }

    @NotNull
    @Override
    public List<Entity> getNearbyEntities(double v, double v1, double v2) {
        return null;
    }

    @Override
    public int getEntityId() {
        return 0;
    }

    @Override
    public int getFireTicks() {
        return 0;
    }

    @Override
    public int getMaxFireTicks() {
        return 0;
    }

    @Override
    public void setFireTicks(int i) {

    }

    @Override
    public void setVisualFire(boolean b) {

    }

    @Override
    public boolean isVisualFire() {
        return false;
    }

    @Override
    public int getFreezeTicks() {
        return 0;
    }

    @Override
    public int getMaxFreezeTicks() {
        return 0;
    }

    @Override
    public void setFreezeTicks(int i) {

    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void remove() {

    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @NotNull
    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void setPersistent(boolean b) {

    }

    @Nullable
    @Override
    public Entity getPassenger() {
        return null;
    }

    @Override
    public boolean setPassenger(@NotNull Entity entity) {
        return false;
    }

    @NotNull
    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    @Override
    public boolean addPassenger(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean removePassenger(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public float getFallDistance() {
        return 0;
    }

    @Override
    public void setFallDistance(float v) {

    }

    @Override
    public void setLastDamageCause(@Nullable EntityDamageEvent entityDamageEvent) {

    }

    @Nullable
    @Override
    public EntityDamageEvent getLastDamageCause() {
        return null;
    }

    @NotNull
    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public int getTicksLived() {
        return 0;
    }

    @Override
    public void setTicksLived(int i) {

    }

    @Override
    public void playEffect(@NotNull EntityEffect entityEffect) {

    }

    @NotNull
    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public boolean isInsideVehicle() {
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        return false;
    }

    @Nullable
    @Override
    public Entity getVehicle() {
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean b) {

    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public void setGlowing(boolean b) {

    }

    @Override
    public boolean isGlowing() {
        return false;
    }

    @Override
    public void setInvulnerable(boolean b) {

    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void setSilent(boolean b) {

    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(boolean b) {

    }

    @Override
    public int getPortalCooldown() {
        return 0;
    }

    @Override
    public void setPortalCooldown(int i) {

    }

    @NotNull
    @Override
    public Set<String> getScoreboardTags() {
        return null;
    }

    @Override
    public boolean addScoreboardTag(@NotNull String s) {
        return false;
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String s) {
        return false;
    }

    @NotNull
    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    @NotNull
    @Override
    public BlockFace getFacing() {
        return null;
    }

    @NotNull
    @Override
    public Pose getPose() {
        return null;
    }

    @Nullable
    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(@Nullable String s) {

    }

    @Override
    public void sendMessage(@NotNull String s) {

    }

    @Override
    public void sendMessage(@NotNull String... strings) {

    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {

    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {

    }

    @Override
    public void setMetadata(@NotNull String s, @NotNull MetadataValue metadataValue) {

    }

    @NotNull
    @Override
    public List<MetadataValue> getMetadata(@NotNull String s) {
        return null;
    }

    @Override
    public boolean hasMetadata(@NotNull String s) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String s, @NotNull Plugin plugin) {

    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        return false;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return false;
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
        return null;
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return null;
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
        return null;
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        return null;
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @NotNull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean b) {

    }

    @NotNull
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
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

    @NotNull
    @Override
    public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> aClass) {
        return null;
    }

    @NotNull
    @Override
    public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> aClass, @Nullable Vector vector) {
        return null;
    }

    @Override
    public void stopAllSounds() {

    }
}
