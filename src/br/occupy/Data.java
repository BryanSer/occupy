/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package br.occupy;

import Br.API.Utils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-11-1
 */
public class Data {

    public static Map<String, Country> Countrys = new HashMap<>();
    public static Map<String, Area> Areas = new HashMap<>();
    public static Map<String,Hologram> Holograms = new HashMap<>();
    public static Main Plugin;
    
    public static void refreshHolo(){
        for (Area area : Areas.values()) {
            Location loc = area.getDisplayHolo();
                    
        }
    }

    public static Country getCountry(Player p) {
        if (p.isOp()) {
            return null;
        }
        for (Country c : Countrys.values()) {
            if (p.hasPermission(c.getPermission())) {
                return c;
            }
        }
        return null;
    }

    public static void init(Main plugin) {
        Plugin = plugin;
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            try {
                Utils.OutputFile(plugin, "config.yml", null);
            } catch (IOException ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        File f = new File(folder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection cs = config.getConfigurationSection("Countrys");
        for (String key : cs.getKeys(false)) {
            Country c = new Country(cs.getConfigurationSection(key));
            Countrys.put(key, c);
        }
    }
}
