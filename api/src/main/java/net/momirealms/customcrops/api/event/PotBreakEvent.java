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

package net.momirealms.customcrops.api.event;

import net.momirealms.customcrops.api.core.block.BreakReason;
import net.momirealms.customcrops.api.core.mechanic.pot.PotConfig;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An event that is triggered when a pot block is broken in the CustomCrops plugin.
 */
public class PotBreakEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Location location;
    private final PotConfig config;
    private final CustomCropsBlockState blockState;
    private final Entity entityBreaker;
    private final Block blockBreaker;
    private final BreakReason reason;

    /**
     * Constructor for the PotBreakEvent.
     *
     * @param entityBreaker The entity that caused the pot to break, if applicable (can be null).
     * @param blockBreaker  The block that caused the pot to break, if applicable (can be null).
     * @param location      The location of the pot block being broken.
     * @param config        The configuration of the pot that is being broken.
     * @param blockState    The state of the pot block before it was broken.
     * @param reason        The reason why the pot was broken.
     */
    public PotBreakEvent(
            @Nullable Entity entityBreaker,
            @Nullable Block blockBreaker,
            @NotNull Location location,
            @NotNull PotConfig config,
            @NotNull CustomCropsBlockState blockState,
            @NotNull BreakReason reason
    ) {
        this.entityBreaker = entityBreaker;
        this.blockBreaker = blockBreaker;
        this.location = location;
        this.blockState = blockState;
        this.reason = reason;
        this.config = config;
    }

    /**
     * Returns whether the event is cancelled.
     *
     * @return true if the event is cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of the event.
     *
     * @param cancel true to cancel the event, false otherwise.
     */
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return the static handler list.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the list of handlers for this event instance.
     *
     * @return the handler list.
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Gets the location of the pot block being broken.
     *
     * @return the location of the pot block.
     */
    @NotNull
    public Location location() {
        return location;
    }

    /**
     * Gets the entity responsible for breaking the pot, if applicable.
     *
     * @return the entity that caused the break, or null if not applicable.
     */
    @Nullable
    public Entity entityBreaker() {
        return entityBreaker;
    }

    /**
     * Gets the reason for the pot breakage.
     *
     * @return the reason for the break.
     */
    @NotNull
    public BreakReason reason() {
        return reason;
    }

    /**
     * Gets the configuration of the pot that is being broken.
     *
     * @return the pot configuration.
     */
    @NotNull
    public PotConfig potConfig() {
        return config;
    }

    /**
     * Gets the state of the pot block before it was broken.
     *
     * @return the block state of the pot.
     */
    @NotNull
    public CustomCropsBlockState blockState() {
        return blockState;
    }

    /**
     * Gets the block responsible for breaking the pot, if applicable.
     *
     * @return the block that caused the break, or null if not applicable.
     */
    @Nullable
    public Block blockBreaker() {
        return blockBreaker;
    }
}