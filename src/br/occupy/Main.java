/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package br.occupy;

import Br.API.Utils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-11-1
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(Area.class);
        Data.init(this);
        Bukkit.getPluginManager().registerEvents(new Data(), this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Data.checkPlayer();
            Data.Areas.values().forEach(Area::run);
        }, 20, 20);

        Bukkit.getScheduler().runTaskTimer(this, Data::refreshHolo, 20, 20);
        Data.loadArea();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Country[] cos = Data.Countrys.values().toArray(new Country[Data.Countrys.size()]);
            Arrays.sort(cos, (a, b)
                    -> Data.Areas
                            .values()
                            .stream()
                            .filter(ar -> ar.getOccupied().equals(b.getName()))
                            .mapToInt(i -> 1)
                            .sum()
                    - Data.Areas
                            .values()
                            .stream()
                            .filter(ar -> ar.getOccupied().equals(a.getName()))
                            .mapToInt(i -> 1)
                            .sum()
            );
            Bukkit.broadcastMessage(Data.Message.get("AwardBC"));
            Bukkit.broadcastMessage(Data.Message.get("RankHead"));
            for (int i = 0; i < cos.length; i++) {
                Country co = cos[i];
                int oc = Data.Areas
                        .values()
                        .stream()
                        .filter(ar -> ar.getOccupied().equals(co.getName()))
                        .mapToInt(ii -> 1)
                        .sum();
                Bukkit.broadcastMessage(Data.Message.get("RankFormat")
                        .replaceAll("%rank%", String.valueOf(i + 1))
                        .replaceAll("%country%", co.getDisplayName())
                        .replaceAll("%count%", String.valueOf(oc))
                );
                if (oc == 0) {
                    continue;
                }
                List<String> award = Data.Awards.get(i + 1);
                if (award != null) {
                    for (Player p : Utils.getOnlinePlayers()) {
                        Country c = Data.getCountry(p);
                        if(c == co){
                            for (String cmd : award) {
                                cmd = cmd.replaceAll("%player%", p.getName());
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                            }
                        }
                    }
                }
            }
        }, 10 * 60 * 20, 10 * 60 * 20);
    }

    @Override
    public void onDisable() {
        Data.saveArea();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return false;
        }
        if (args[0].equalsIgnoreCase("get")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                Utils.safeGiveItem(p, Data.Select.clone());
                sender.sendMessage("§b物品已发送到你的背包了");
            } else {
                sender.sendMessage("§c后台不能执行这个命令");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("remove") && args.length > 1) {
            Area cz = Data.Areas.remove(args[1]);
            if (cz == null) {
                sender.sendMessage("§c移除失败 找不到这个区域");
            } else {
                Hologram h = Data.Holograms.remove(cz.getDisplayHolo().toString());
                if (h != null) {
                    h.delete();
                }
                sender.sendMessage("§b移除成功");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("create") && args.length >= 3 && (sender instanceof Player)) {
            Player p = (Player) sender;
            Object[] obj = Data.Selected.get(p.getName());
            if (obj == null || obj[0] == null || obj[1] == null) {
                p.sendMessage("§c你还没有选择完点");
                return true;
            }
            String name = args[1], displayname = ChatColor.translateAlternateColorCodes('&', args[2]);
            if (Data.Areas.containsKey(name)) {
                p.sendMessage("§c已存在同名区域");
                return true;
            }
            Area area = new Area((Location) obj[0], (Location) obj[1], name, displayname, p.getLocation());
            area.init();
            Data.Areas.put(name, area);
            Data.Selected.remove(p.getName());
            p.sendMessage("§b创建成功");
            return true;
        }
        return false;
    }

}
