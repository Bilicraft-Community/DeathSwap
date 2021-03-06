package net.mcxk.deathswap.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SwapEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
