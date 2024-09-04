/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customcrops.api.core;

import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.action.ActionManager;
import net.momirealms.customcrops.api.context.Context;
import net.momirealms.customcrops.api.context.ContextKeys;
import net.momirealms.customcrops.api.core.block.CropBlock;
import net.momirealms.customcrops.api.core.mechanic.crop.BoneMeal;
import net.momirealms.customcrops.api.core.mechanic.crop.CropConfig;
import net.momirealms.customcrops.api.core.mechanic.crop.CropStageConfig;
import net.momirealms.customcrops.api.core.mechanic.pot.PotConfig;
import net.momirealms.customcrops.api.core.mechanic.sprinkler.SprinklerConfig;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.core.world.CustomCropsWorld;
import net.momirealms.customcrops.api.core.world.Pos3;
import net.momirealms.customcrops.api.event.BoneMealDispenseEvent;
import net.momirealms.customcrops.api.util.EventUtils;
import net.momirealms.customcrops.api.util.LocationUtils;
import net.momirealms.customcrops.common.helper.VersionHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractCustomEventListener implements Listener {

    private final HashSet<EntityType> entities = new HashSet<>();
    private final HashSet<Material> blocks = new HashSet<>();

    protected final AbstractItemManager itemManager;

    public AbstractCustomEventListener(AbstractItemManager itemManager) {
        this.itemManager = itemManager;
        this.entities.addAll(List.of(EntityType.ITEM_FRAME, EntityType.ARMOR_STAND));
        if (VersionHelper.isVersionNewerThan1_19_4()) {
            this.entities.addAll(List.of(EntityType.ITEM_DISPLAY, EntityType.INTERACTION));
        }
        this.blocks.addAll(List.of(
                Material.NOTE_BLOCK,
                Material.MUSHROOM_STEM, Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK,
                Material.TRIPWIRE,
                Material.CHORUS_PLANT, Material.CHORUS_FLOWER,
                Material.ACACIA_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.DARK_OAK_LEAVES, Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES,
                Material.CAVE_VINES, Material.TWISTING_VINES, Material.WEEPING_VINES,
                Material.KELP,
                Material.CACTUS
        ));
        if (VersionHelper.isVersionNewerThan1_19()) {
            this.blocks.add(Material.MANGROVE_LEAVES);
        }
        if (VersionHelper.isVersionNewerThan1_20()) {
            this.blocks.add(Material.CHERRY_LEAVES);
        }
    }

    @EventHandler
    public void onInteractAir(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR)
            return;
        this.itemManager.handlePlayerInteractAir(
                event.getPlayer(),
                event.getHand(),
                event.getItem()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block block = event.getClickedBlock();
        assert block != null;
        if (blocks.contains(block.getType())) {
            return;
        }
        this.itemManager.handlePlayerInteractBlock(
                event.getPlayer(),
                block,
                block.getType().name(), event.getBlockFace(),
                event.getHand(),
                event.getItem(),
                event
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        EntityType type = event.getRightClicked().getType();
        if (entities.contains(type)) {
            return;
        }
        this.itemManager.handlePlayerInteractFurniture(
                event.getPlayer(),
                event.getRightClicked().getLocation(), type.name(),
                event.getHand(), event.getPlayer().getInventory().getItem(event.getHand()),
                event
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlaceBlock(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (blocks.contains(block.getType())) {
            return;
        }
        this.itemManager.handlePlayerPlace(
                event.getPlayer(),
                block.getLocation(),
                block.getType().name(),
                event.getHand(),
                event.getItemInHand(),
                event
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (blocks.contains(block.getType())) {
            return;
        }
        this.itemManager.handlePlayerBreak(
                event.getPlayer(),
                block.getLocation(), event.getPlayer().getInventory().getItemInMainHand(), block.getType().name(),
                event
        );
    }

    @EventHandler (ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        String itemID = this.itemManager.id(itemStack);

        if (Registries.STAGE_TO_CROP_UNSAFE.containsKey(itemID)) {
            event.setCancelled(true);
            return;
        }

        SprinklerConfig sprinkler = Registries.ITEM_TO_SPRINKLER.get(itemID);
        if (sprinkler != null) {
            String twoD = sprinkler.twoDItem();
            if (twoD != null) {
                if (!twoD.equals(itemID)) {
                    ItemStack newItem = this.itemManager.build(null, twoD);
                    if (newItem != null && newItem.getType() != Material.AIR) {
                        newItem.setAmount(itemStack.getAmount());
                        item.setItemStack(newItem);
                    }
                    return;
                }
            }
        }

        PotConfig pot = Registries.ITEM_TO_POT.get(itemID);
        if (pot != null) {
            ItemStack newItem = this.itemManager.build(null, pot.getPotAppearance(false, null));
            if (newItem != null && newItem.getType() != Material.AIR) {
                newItem.setAmount(itemStack.getAmount());
                item.setItemStack(newItem);
            }
            return;
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onBlockChange(BlockFadeEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.FARMLAND) {
            Pos3 above = Pos3.from(block.getLocation()).add(0,1,0);
            BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(block.getWorld())
                    .flatMap(world -> world.getBlockState(above)).ifPresent(blockState -> {
                if (blockState.type() instanceof CropBlock) {
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTrampling(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.FARMLAND && event.getTo() == Material.DIRT) {
            if (ConfigManager.preventTrampling()) {
                event.setCancelled(true);
                return;
            }
            this.itemManager.handleEntityTrample(event.getEntity(), block.getLocation(), "FARMLAND", event);
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMoistureChange(MoistureChangeEvent event) {
        if (ConfigManager.disableMoistureMechanic())
            event.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Optional<CustomCropsWorld<?>> world = BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(event.getBlock().getWorld());
        if (world.isEmpty()){
            return;
        }
        CustomCropsWorld<?> w = world.get();
        for (Block block : event.getBlocks()) {
            if (w.getBlockState(Pos3.from(block.getLocation())).isPresent()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Optional<CustomCropsWorld<?>> world = BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(event.getBlock().getWorld());
        if (world.isEmpty()){
            return;
        }
        CustomCropsWorld<?> w = world.get();
        for (Block block : event.getBlocks()) {
            if (w.getBlockState(Pos3.from(block.getLocation())).isPresent()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack itemStack = event.getItem();
        String itemID = this.itemManager.id(itemStack);
        if (Registries.ITEM_TO_WATERING_CAN.containsKey(itemID)) {
            event.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        for (Block block : event.blockList()) {
            this.itemManager.handleEntityExplode(entity, block.getLocation(), this.itemManager.blockID(block), event);
            if (event.isCancelled()) {
                return;
            }
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplosion(BlockExplodeEvent event) {
        Block exploder = event.getBlock();
        for (Block block : event.blockList()) {
            this.itemManager.handleBlockExplode(exploder, block.getLocation(), this.itemManager.blockID(block), event);
            if (event.isCancelled()) {
                return;
            }
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDispenser(BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof org.bukkit.block.data.type.Dispenser directional)) {
            return;
        }
        Optional<CustomCropsWorld<?>> optionalWorld = BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(event.getBlock().getWorld());
        if (optionalWorld.isEmpty()){
            return;
        }
        CustomCropsWorld<?> world = optionalWorld.get();
        Block relative = block.getRelative(directional.getFacing());
        Location location = relative.getLocation();
        Pos3 pos3 = Pos3.from(location);
        world.getBlockState(pos3).ifPresent(state -> {
            if (state.type() instanceof CropBlock cropBlock) {
                ItemStack itemStack = event.getItem();
                String itemID = itemManager.id(itemStack);
                CropConfig cropConfig = cropBlock.config(state);
                int point = cropBlock.point(state);
                if (point < cropConfig.maxPoints()) {
                    for (BoneMeal boneMeal : cropConfig.boneMeals()) {
                        if (boneMeal.isDispenserAllowed()) {
                            if (EventUtils.fireAndCheckCancel(new BoneMealDispenseEvent(block, location, state, itemStack, boneMeal))) {
                                event.setCancelled(true);
                                return;
                            }
                            if (block.getState() instanceof Dispenser dispenser) {
                                event.setCancelled(true);
                                Inventory inventory = dispenser.getInventory();
                                for (ItemStack storage : inventory.getStorageContents()) {
                                    if (storage == null) continue;
                                    String id = itemManager.id(storage);
                                    if (id.equals(itemID)) {
                                        storage.setAmount(storage.getAmount() - 1);
                                        Context<Player> context = Context.player(null);
                                        context.arg(ContextKeys.LOCATION, location);
                                        boneMeal.triggerActions(context);
                                        int afterPoints = Math.min(point + boneMeal.rollPoint(), cropConfig.maxPoints());
                                        cropBlock.point(state, afterPoints);
                                        Context<CustomCropsBlockState> blockContext = Context.block(state);
                                        blockContext.arg(ContextKeys.LOCATION, LocationUtils.toBlockLocation(location));
                                        for (int i = point + 1; i <= afterPoints; i++) {
                                            CropStageConfig stage = cropConfig.stageByPoint(i);
                                            if (stage != null) {
                                                ActionManager.trigger(blockContext, stage.growActions());
                                            }
                                        }
                                        CropStageConfig currentStage = cropConfig.stageWithModelByPoint(point);
                                        CropStageConfig afterStage = cropConfig.stageWithModelByPoint(afterPoints);
                                        if (currentStage == afterStage) return;
                                        Location bukkitLocation = location.toLocation(world.bukkitWorld());
                                        FurnitureRotation rotation = BukkitCustomCropsPlugin.getInstance().getItemManager().remove(bukkitLocation, ExistenceForm.ANY);
                                        if (rotation == FurnitureRotation.NONE && cropConfig.rotation()) {
                                            rotation = FurnitureRotation.random();
                                        }
                                        BukkitCustomCropsPlugin.getInstance().getItemManager().place(bukkitLocation, afterStage.existenceForm(), Objects.requireNonNull(afterStage.stageID()), rotation);
                                    }
                                }
                            }
                            return;
                        }
                    }
                }
            }
        });
    }
}