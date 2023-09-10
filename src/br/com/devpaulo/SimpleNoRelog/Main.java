package br.com.devpaulo.simplenorelog;

import br.com.devpaulo.simplenorelog.api.SimpleNoRelog;
import br.com.devpaulo.simplenorelog.api.events.PlayerEnterPvpEvent;
import br.com.devpaulo.simplenorelog.api.events.PlayerLeavePvpEvent;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Main
  extends JavaPlugin
  implements Listener
{
	protected HashMap<String, BukkitTask> players = new HashMap<String, BukkitTask>();
	protected HashMap<String, Integer> players2 = new HashMap<String, Integer>();
	protected HashMap<String, Integer> blocked = new HashMap<String, Integer>();
  
  public void onEnable()
  {
    getLogger().info("Ativando SimpleNoRelog (V" + getDescription().getVersion() + ") - Autor: SubZero0 & mchiappinam");
    getServer().getPluginManager().registerEvents(this, this);
    
    File file = new File(getDataFolder(), "config.yml");
    if (!file.exists()) {
      try
      {
        saveResource("config_template.yml", false);
        File file2 = new File(getDataFolder(), "config_template.yml");
        file2.renameTo(new File(getDataFolder(), "config.yml"));
      }
      catch (Exception localException) {}
    }
    reloadConfig();
    /**try
    {
      File file2 = new File(getDataFolder(), "language_br.yml");
      if (!file2.exists())
      {
        saveResource("language_br.yml", false);getLogger().info("Saved language_br.yml");
      }
    }
    catch (Exception localException1) {}
    try
    {
      File file2 = new File(getDataFolder(), "language_en.yml");
      if (!file2.exists())
      {
        saveResource("language_en.yml", false);getLogger().info("Saved language_en.yml");
      }
    }
    catch (Exception localException2) {}
    MessageManager.loadMessages(this, getConfig().getString("language").trim());*/
    
    new SimpleNoRelog(this);
    Bukkit.getLogger().addHandler(new Handler()
    {
      public void close()
        throws SecurityException
      {}
      
      public void flush() {}
      
      public void publish(LogRecord log)
      {
        if (log.getMessage() != null) {
          if (log.getMessage().toLowerCase().contains("disconnect")) {
            if (log.getMessage().toLowerCase().contains("genericreason")) {
              Main.this.players2.put(log.getMessage().split(" ")[0].toLowerCase(), Integer.valueOf(1));
            } else if (log.getMessage().toLowerCase().contains("endofstream")) {
              Main.this.players2.put(log.getMessage().split(" ")[0].toLowerCase(), Integer.valueOf(2));
            } else if (log.getMessage().toLowerCase().contains("quitting")) {
              Main.this.players2.put(log.getMessage().split(" ")[0].toLowerCase(), Integer.valueOf(3));
            } else if (log.getMessage().toLowerCase().contains("overflow")) {
              Main.this.players2.put(log.getMessage().split(" ")[0].toLowerCase(), Integer.valueOf(4));
            } else if (log.getMessage().toLowerCase().contains("timeout")) {
              Main.this.players2.put(log.getMessage().split(" ")[0].toLowerCase(), Integer.valueOf(5));
            }
          }
        }
      }
    });
  }
  
  public void onDisable()
  {
    getLogger().info("Desativando SimpleNoRelog - Autor: SubZero0 & mchiappinam");
  }
  
  @EventHandler(ignoreCancelled=false, priority=EventPriority.MONITOR)
  private void onHit(EntityDamageByEntityEvent e)
  {
    if ((!e.isCancelled()) && 
      ((e.getEntity() instanceof Player)))
    {
      addToPvp2((Player)e.getEntity(), 0);
      if ((e.getDamager() instanceof Player)) {
        addToPvp2((Player)e.getDamager(), 0);
      }
      if ((e.getDamager() instanceof Projectile))
      {
        Projectile proj = (Projectile)e.getDamager();
        if ((proj.getShooter() instanceof Player)) {
          addToPvp2((Player)proj.getShooter(), 2);
        }
      }
    }
  }
  
  @EventHandler(ignoreCancelled=false, priority=EventPriority.MONITOR)
  private void onPSplash(PotionSplashEvent e)
  {
    if (!e.isCancelled())
    {
      boolean pvp = false;
      for (PotionEffect pe : e.getPotion().getEffects()) {
        if ((pe.getType() == PotionEffectType.POISON) || (pe.getType() == PotionEffectType.HARM) || (pe.getType() == PotionEffectType.BLINDNESS) || (pe.getType() == PotionEffectType.CONFUSION) || (pe.getType() == PotionEffectType.HUNGER) || (pe.getType() == PotionEffectType.SLOW) || (pe.getType() == PotionEffectType.SLOW_DIGGING) || (pe.getType() == PotionEffectType.WEAKNESS) || (pe.getType() == PotionEffectType.WITHER))
        {
          pvp = true;
          break;
        }
      }
      if (e.getAffectedEntities().size() == 0) {
        pvp = false;
      } else if ((e.getAffectedEntities().size() == 1) && (e.getAffectedEntities().contains(e.getEntity().getShooter()))) {
        pvp = false;
      }
      if (pvp)
      {
        if ((e.getEntity().getShooter() instanceof Player)) {
          addToPvp2((Player)e.getEntity().getShooter(), 3);
        }
        for (LivingEntity le : e.getAffectedEntities()) {
          if ((le != null) && 
            (!le.isDead()) && (le.isValid()) && 
            ((le instanceof Player))) {
            addToPvp2((Player)le, 3);
          }
        }
      }
    }
  }
  
  @EventHandler
  private void onDeath(final PlayerDeathEvent e)
  {
    removeFromPvp(e.getEntity().getName(), 0);
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        Main.this.removeFromPvp(e.getEntity().getName(), 0);
      }
    }, 20L);
  }
  
  @EventHandler
  private void onJoin(PlayerJoinEvent e)
  {
    if (this.players2.containsKey(e.getPlayer().getName().toLowerCase())) {
      this.players2.remove(e.getPlayer().getName().toLowerCase());
    }
  }
  
  @EventHandler
  private void onQuit(PlayerQuitEvent e)
  {
    if (this.players.containsKey(e.getPlayer().getName().toLowerCase()))
    {
      boolean alrd_show = false;
      if (getConfig().getBoolean("whenRelog.killPlayer"))
      {
        if (this.players2.containsKey(e.getPlayer().getName().toLowerCase()))
        {
          int i = ((Integer)this.players2.get(e.getPlayer().getName().toLowerCase())).intValue();
          if (i == 1)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.genericreason"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 2)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.endofstream"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 3)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.quitting"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 4)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.overflow"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 5) {
            if (getConfig().getBoolean("executeWhenQuitReason.timeout"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          this.players2.remove(e.getPlayer().getName().toLowerCase());
        }
        else
        {
          removeFromPvp(e.getPlayer().getName(), 1);
          e.getPlayer().setHealth(0);
          getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
          alrd_show = true;
        }
      }
      else if (this.players2.containsKey(e.getPlayer().getName().toLowerCase())) {
        this.players2.remove(e.getPlayer().getName().toLowerCase());
      }
      if ((!alrd_show) && 
        (getConfig().getBoolean("whenRelog.showDontKillPlayerMsg"))) {
          getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP!");
      }
      if (getConfig().getString("whenRelog.executeCmd").length() > 0) {
        getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("whenRelog.executeCmd").replaceAll("@player", e.getPlayer().getName()));
      }
    }
  }
  
  public boolean isInPvp(String nome)
  {
    return this.players.containsKey(nome.toLowerCase());
  }
  
  public void removeFromPvp(String nome, int cause)
  {
    if (this.players.containsKey(nome.toLowerCase()))
    {
      getServer().getPluginManager().callEvent(new PlayerLeavePvpEvent(getServer().getPlayer(nome), cause));
      ((BukkitTask)this.players.get(nome.toLowerCase())).cancel();
      this.players.remove(nome.toLowerCase());
    }
  }
  
  private void addToPvp2(final Player p, int motive)
  {
    PlayerEnterPvpEvent event = new PlayerEnterPvpEvent(p, motive);
    boolean ja_tava = false;
    if (this.players.containsKey(p.getName().toLowerCase()))
    {
      ((BukkitTask)this.players.get(p.getName().toLowerCase())).cancel();
      this.players.remove(p.getName().toLowerCase());
      ja_tava = true;
    }
    if (!ja_tava)
    {
      getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
        p.sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cVocê entrou em PvP! Se sair do servidor será morto!");
      }
    }
    if (!event.isCancelled()) {
      this.players.put(p.getName().toLowerCase(), getServer().getScheduler().runTaskLater(this, new Runnable()
      {
        public void run()
        {
          Main.this.players.remove(p.getName().toLowerCase());
          p.sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §aLiberado do PvP! Agora você pode desconectar!");
        }
      }, getConfig().getInt("timeInCombat") * 20));
    }
  }
  
  public void addToPvp(Player p)
  {
    addToPvp2(p, 1);
  }
  
  @EventHandler
  private void onKick(PlayerKickEvent e)
  {
    if (this.players.containsKey(e.getPlayer().getName().toLowerCase()))
    {
      boolean alrd_show = false;
      if (getConfig().getBoolean("whenRelog.killPlayer"))
      {
        if (this.players2.containsKey(e.getPlayer().getName().toLowerCase()))
        {
          int i = ((Integer)this.players2.get(e.getPlayer().getName().toLowerCase())).intValue();
          if (i == 1)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.genericreason"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 2)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.endofstream"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 3)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.quitting"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 4)
          {
            if (getConfig().getBoolean("executeWhenQuitReason.overflow"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          else if (i == 5) {
            if (getConfig().getBoolean("executeWhenQuitReason.timeout"))
            {
              removeFromPvp(e.getPlayer().getName(), 1);
              e.getPlayer().setHealth(0);
              getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
              alrd_show = true;
            }
            else
            {
              removeFromPvp(e.getPlayer().getName(), 4);
            }
          }
          this.players2.remove(e.getPlayer().getName().toLowerCase());
        }
        else
        {
          removeFromPvp(e.getPlayer().getName(), 1);
          e.getPlayer().setHealth(0);
          getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP e foi morto!");
          alrd_show = true;
        }
      }
      else if (this.players2.containsKey(e.getPlayer().getName().toLowerCase())) {
        this.players2.remove(e.getPlayer().getName().toLowerCase());
      }
      if ((!alrd_show) && 
        (getConfig().getBoolean("whenRelog.showDontKillPlayerMsg"))) {
          getServer().broadcastMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §c"+e.getPlayer().getName()+" deslogou em PvP!");
      }
      if (getConfig().getString("whenRelog.executeCmd").length() > 0) {
        getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("whenRelog.executeCmd").replaceAll("@player", e.getPlayer().getName()));
      }
    }
  }
  
  @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
  private void onCommand(PlayerCommandPreprocessEvent e)
  {
    if ((getConfig().getBoolean("block.commands")) && (this.players.containsKey(e.getPlayer().getName().toLowerCase())) && (!e.getMessage().toLowerCase().startsWith("/ban")) && (!e.getMessage().toLowerCase().startsWith("/desbugar")) && (!e.getMessage().toLowerCase().startsWith("/d")) && (!e.getMessage().toLowerCase().startsWith("/arma")))
    {
      e.getPlayer().sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cComandos bloqueados em PvP!");
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
  private void onCommand2(PlayerCommandPreprocessEvent e)
  {
    if ((getConfig().getBoolean("block.commands")) && (this.players.containsKey(e.getPlayer().getName().toLowerCase())) && (!e.getMessage().toLowerCase().startsWith("/ban")) && (!e.getMessage().toLowerCase().startsWith("/desbugar")) && (!e.getMessage().toLowerCase().startsWith("/d")) && (!e.getMessage().toLowerCase().startsWith("/arma")))
    {
    	e.getPlayer().sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cComandos bloqueados em PvP!");
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
  private void onCommand3(PlayerCommandPreprocessEvent e)
  {
    if ((getConfig().getBoolean("block.commands")) && (this.players.containsKey(e.getPlayer().getName().toLowerCase())) && (!e.getMessage().toLowerCase().startsWith("/ban")) && (!e.getMessage().toLowerCase().startsWith("/desbugar")) && (!e.getMessage().toLowerCase().startsWith("/d")) && (!e.getMessage().toLowerCase().startsWith("/arma")))
    {
        e.getPlayer().sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cComandos bloqueados em PvP!");
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
  private void onCommand4(PlayerCommandPreprocessEvent e)
  {
    if ((getConfig().getBoolean("block.commands")) && (this.players.containsKey(e.getPlayer().getName().toLowerCase())) && (!e.getMessage().toLowerCase().startsWith("/ban")) && (!e.getMessage().toLowerCase().startsWith("/desbugar")) && (!e.getMessage().toLowerCase().startsWith("/d")) && (!e.getMessage().toLowerCase().startsWith("/arma")))
    {
        e.getPlayer().sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cComandos bloqueados em PvP!");
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
  private void onCommand5(PlayerCommandPreprocessEvent e)
  {
    if ((getConfig().getBoolean("block.commands")) && (this.players.containsKey(e.getPlayer().getName().toLowerCase())) && (!e.getMessage().toLowerCase().startsWith("/ban")) && (!e.getMessage().toLowerCase().startsWith("/desbugar")) && (!e.getMessage().toLowerCase().startsWith("/d")) && (!e.getMessage().toLowerCase().startsWith("/arma")))
    {
        e.getPlayer().sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cComandos bloqueados em PvP!");
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
  private void onCommand6(PlayerCommandPreprocessEvent e)
  {
    if ((getConfig().getBoolean("block.commands")) && (this.players.containsKey(e.getPlayer().getName().toLowerCase())) && (!e.getMessage().toLowerCase().startsWith("/ban")) && (!e.getMessage().toLowerCase().startsWith("/desbugar")) && (!e.getMessage().toLowerCase().startsWith("/d")) && (!e.getMessage().toLowerCase().startsWith("/arma")))
    {
        e.getPlayer().sendMessage("§3[Ⓒⓞⓜⓑⓐⓣⓔ] §cComandos bloqueados em PvP!");
      e.setCancelled(true);
    }
  }
}
