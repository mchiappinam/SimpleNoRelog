package br.com.devpaulo.simplenorelog.api;

import br.com.devpaulo.simplenorelog.Main;
import org.bukkit.entity.Player;

public class SimpleNoRelog
{
  private static Main plugin;
  
  public SimpleNoRelog(Main main)
  {
    plugin = main;
  }
  
  public static boolean isInPvp(String name)
  {
    return plugin.isInPvp(name);
  }
  
  public static boolean isInPvp(Player player)
  {
    return plugin.isInPvp(player.getName());
  }
  
  public static void removeFromPvp(String name)
  {
    if (isInPvp(name)) {
      plugin.removeFromPvp(name, 2);
    }
  }
  
  public static void removeFromPvp(Player player)
  {
    removeFromPvp(player.getName());
  }
  
  public static void addToPvp(Player player)
  {
    plugin.addToPvp(player);
  }
}
