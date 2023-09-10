/*    */ package br.com.devpaulo.simplenorelog;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.util.HashMap;
/*    */ import org.bukkit.ChatColor;
/*    */ import org.bukkit.configuration.file.YamlConfiguration;
/*    */ 
/*    */ public class MessageManager
/*    */ {
/* 10 */   private static HashMap<String, String> msgs = new HashMap<String, String>();
/*    */ 
/*    */   public static void loadMessages(Main m, String lang)
/*    */   {
/* 15 */     msgs.clear();
/* 16 */     YamlConfiguration msglist = YamlConfiguration.loadConfiguration(new File(m.getDataFolder(), "language_" + lang + ".yml"));
/* 17 */     for (String n : msglist.getConfigurationSection("").getKeys(false))
/* 18 */       msgs.put(n.toLowerCase(), msglist.getString(n));
/*    */   }
/*    */ 
/*    */   public static String getMessage(String msg) {
/* 22 */     return ChatColor.translateAlternateColorCodes('&', (String)msgs.get(msg.toLowerCase()));
/*    */   }
/*    */ }

/* Location:           C:\Users\Matheus\Desktop\SimpleNoRelog.jar
 * Qualified Name:     br.com.devpaulo.simplenorelog.MessageManager
 * JD-Core Version:    0.6.2
 */