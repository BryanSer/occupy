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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-11-1
 */
public class Data implements Listener {

    public static Map<String, Country> Countrys = new HashMap<>();
    public static Map<String, Area> Areas = new HashMap<>();
    public static Map<String, Hologram> Holograms = new HashMap<>();
    public static Main Plugin;
    public static ItemStack Select;

    public static void checkPlayer() {
        for (Player p : Utils.getOnlinePlayers()) {
            Area in = in(p);
            if (in != null) {
                in.add(p);
            }
        }
    }

    public static Area in(Player p) {
        for (Area a : Areas.values()) {
            if (a.inArea(p)) {
                return a;
            }
        }
        return null;
    }

    public static void refreshHolo() {
        for (Area area : Areas.values()) {
            Location loc = area.getDisplayHolo();
            String key = loc.toString();
            Hologram holo = Holograms.get(key);
            if (holo == null) {
                holo = HologramsAPI.createHologram(Plugin, loc);
                Holograms.put(key, holo);
            }
            holo.clearLines();
            Country c = area.getOccupyingCountry();
            holo.appendTextLine("§6区域: " + area.getDisplayName());
            String owner = area.getOccupied();
            Country own = Countrys.get(owner);
            holo.appendTextLine("§b§l拥有国家: " + (own == null ? "§0中立" : own.getColor() + own.getName()));
            if (c != null) {
                holo.appendTextLine("§6目前正在被" + c.getName() + " 重新确立控制中");
            }
            holo.appendTextLine("§b占领计时: " + area.getOccupyTime() + "/60");
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
        Select = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta im = Select.getItemMeta();
        im.setDisplayName("§e§lOccupy 选择工具");;
        Select.setItemMeta(im);
    }

    public static Map<String, Object[]> Selected = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent evt) {
        Player p = evt.getPlayer();
        if (!p.isOp() || p.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        if (!p.getItemInHand().isSimilar(Data.Select)) {
            return;
        }
        Object[] obj = Selected.get(p.getName());
        if (obj == null) {
            obj = new Object[]{null, null, 2};
            Selected.put(p.getName(), obj);
        }
        int last = (int) obj[2];
        switch (last) {
            case 1:
                obj[1] = evt.getBlock().getLocation();
                obj[2] = 2;
                p.sendMessage("§b您已选择了第2个点");
                break;
            case 2:
                obj[0] = evt.getBlock().getLocation();
                obj[2] = 1;
                p.sendMessage("§b您已选择了第1个点");
                break;
        }
        evt.setCancelled(true);
    }

    public static void saveArea() {
        File f = new File(Plugin.getDataFolder(), "Areas.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Area> e : Areas.entrySet()) {
            config.set(e.getKey(), e.getValue());
        }
        try {
            config.save(f);
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loadArea() {
        File f = new File(Plugin.getDataFolder(), "Areas.yml");
        if (!f.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        for (String key : config.getKeys(false)) {
            Area a = (Area) config.get(key);
            a.init();
            Areas.put(key, a);
        }
    }
}
