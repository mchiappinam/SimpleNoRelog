package br.com.devpaulo.simplenorelog.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeavePvpEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled;
  private Player p;
  private LeavePvpReason r = null;
  
  public PlayerLeavePvpEvent(Player p, int type)
  {
    this.p = p;
    if (type == 0) {
      this.r = LeavePvpReason.DEATH;
    } else if (type == 1) {
      this.r = LeavePvpReason.PLUGIN_KILL;
    } else if (type == 2) {
      this.r = LeavePvpReason.CUSTOM;
    } else if (type == 3) {
      this.r = LeavePvpReason.KICK;
    } else if (type == 4) {
      this.r = LeavePvpReason.DISCONNECT;
    } else if (type == 5) {
      this.r = LeavePvpReason.PLUGIN_DISABLE;
    } else {
      this.r = LeavePvpReason.UNKNOWN;
    }
  }
  
  public Player getPlayer()
  {
    return this.p;
  }
  
  public LeavePvpReason getReason()
  {
    return this.r;
  }
  
  public boolean isCancelled()
  {
    return this.cancelled;
  }
  
  public void setCancelled(boolean cancel)
  {
    this.cancelled = cancel;
  }
  
  public HandlerList getHandlers()
  {
    return handlers;
  }
  
  public static HandlerList getHandlerList()
  {
    return handlers;
  }
  
  public static enum LeavePvpReason
  {
    DEATH,  PLUGIN_KILL,  CUSTOM,  KICK,  DISCONNECT,  PLUGIN_DISABLE,  UNKNOWN;
  }
}
