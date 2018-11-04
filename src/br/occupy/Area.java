/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package br.occupy;

import Br.API.Data.BrConfigurationSerializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-11-1
 */
public class Area implements BrConfigurationSerializable {

    public static final String NEUTRAL = "+-Neutral-+";
    public static final int MAX_OCCUPY_TIME = 60;
    private static final long SEED = 0x001ACCD5F;

    @Config
    private Location Top;
    @Config
    private Location Bottom;
    @Config
    private String Name;
    @Config
    private String DisplayName;
    @Config
    private Location DisplayHolo;

    @Config
    private String Occupied = NEUTRAL;
    @Config
    private int OccupyTime = 0;
    private Queue<String> Occupying = new ArrayDeque<>();

    private Location[][] Blocks;

    public Area(Map<String, Object> args) {
        BrConfigurationSerializable.deserialize(args, this);
    }

    public Area(Location Top, Location Bottom, String Name, String DisplayName, Location displayholo) {
        this.Name = Name;
        this.DisplayName = DisplayName;
        this.setLocation(Top, Bottom);
        this.DisplayHolo = new Location(displayholo.getWorld(), displayholo.getBlockX(), displayholo.getBlockY(), displayholo.getBlockZ());
    }

    public final void setLocation(Location l1, Location l2) {
        if (l1.getWorld() != l2.getWorld()) {
            throw new IllegalArgumentException("提供的两个坐标不位于一个世界");
        }
        int[] x = {l1.getBlockX(), l2.getBlockX()},
                y = {l1.getBlockY(), l2.getBlockY()},
                z = {l1.getBlockZ(), l2.getBlockZ()};
        Arrays.sort(x);
        Arrays.sort(y);
        Arrays.sort(z);
        this.Bottom = new Location(l1.getWorld(), x[0], y[0], z[0]);
        this.Top = new Location(l2.getWorld(), x[1], y[1], z[1]);
    }

    public boolean inArea(Player p) {
        if (p.getWorld() != this.Top.getWorld()) {
            return false;
        }
        Location loc = p.getLocation();
        int[] arr = {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
        return loc.getWorld() == this.Top.getWorld()
                ? arr[0] <= this.Top.getBlockX()
                && arr[0] >= this.Bottom.getBlockX()
                && arr[1] <= this.Top.getBlockY()
                && arr[1] >= this.Bottom.getBlockY()
                && arr[2] <= this.Top.getBlockZ()
                && arr[2] >= this.Bottom.getBlockZ()
                : false;
    }

    public void init() {
        if (Occupied.equals(NEUTRAL)) {
            OccupyTime = 0;
        }
        Blocks = new Location[lengthX()][lengthZ()];
        int y = this.Bottom.getBlockY();
        for (int offx = 0; offx < lengthX(); offx++) {
            for (int offz = 0; offz < lengthZ(); offz++) {
                int x = offx + this.Bottom.getBlockX();
                int z = offz + this.Bottom.getBlockZ();
                Location glass = new Location(this.Bottom.getWorld(), x, y, z);
                glass.getBlock().setType(Material.GLASS);
                Blocks[offx][offz] = new Location(this.Bottom.getWorld(), x, y - 1, z);
                Blocks[offx][offz].getBlock().setType(Material.WOOL);
            }
        }
    }

    public Country getOccupyingCountry() {
        String name = Occupying.peek();
        if (name == null) {
            return null;
        }
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            Occupying.poll();
            return getOccupyingCountry();
        }
        Country c = Data.getCountry(p);
        if (c == null) {
            if (p.isOp()) {
                p.sendMessage("§c管理员无法参与占领 已跳过占领状态");
            } else {
                p.sendMessage("§c你不属于任何国家 无法参与");
            }
            Occupying.poll();
            return getOccupyingCountry();
        }
        if (!this.inArea(p)) {
            Occupying.poll();
            return getOccupyingCountry();
        }
        return c;
    }

    public Player getOccupyingPlayer() {
        String name = Occupying.peek();
        if (name == null) {
            return null;
        }
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            Occupying.poll();
            return getOccupyingPlayer();
        }
        Country c = Data.getCountry(p);
        if (c == null) {
            if (p.isOp()) {
                p.sendMessage("§c管理员无法参与占领 已跳过占领状态");
            } else {
                p.sendMessage("§c你不属于任何国家 无法参与");
            }
            Occupying.poll();
            return getOccupyingPlayer();
        }
        if (!this.inArea(p)) {
            Occupying.poll();
            return getOccupyingPlayer();
        }
        return p;
    }

    public Country calcTime() {
        Country c = getOccupyingCountry();

        if (c == null) {
            if (!this.Occupied.equals(Area.NEUTRAL)) {
                this.OccupyTime += 5;
            } else {
                this.OccupyTime -= 5;
            }
            this.OccupyTime = this.OccupyTime > MAX_OCCUPY_TIME ? MAX_OCCUPY_TIME : this.OccupyTime < 0 ? 0 : this.OccupyTime;
            if (this.OccupyTime <= 0) {
                this.OccupyTime = 0;
                this.setOccupied(NEUTRAL);
            }
            return null;
        }
        if (this.Occupied.equals(NEUTRAL)) {
            this.OccupyTime++;
            if (this.OccupyTime >= 60) {
                this.setOccupied(c.getName());
            }
        } else if (c.getName().equals(this.Occupied)) {
            this.OccupyTime++;
        } else {
            this.OccupyTime--;
        }
        this.OccupyTime = this.OccupyTime > MAX_OCCUPY_TIME ? MAX_OCCUPY_TIME : this.OccupyTime < 0 ? 0 : this.OccupyTime;
        if (this.OccupyTime <= 0) {
            this.OccupyTime = 0;
            this.setOccupied(NEUTRAL);
        }
        return c;
    }

    public void run() {
        Country c = calcTime();
        if (c == null) {
            c = Data.Countrys.get(Occupied);
        }
        byte basedata = Occupied.equals(NEUTRAL) ? 0 : Data.Countrys.get(Occupied).getDyeColor();
        byte tar = c.getDyeColor();
        if (!this.Occupied.equals(c.getName()) && !this.Occupied.equals(Area.NEUTRAL)) {
            basedata = 0;
            tar = Data.Countrys.get(this.Occupied).getDyeColor();
        }
        Random random = new Random(SEED);
        double rate = this.OccupyTime / (double) MAX_OCCUPY_TIME;
        for (Location[] t : Blocks) {
            for (Location loc : t) {
                double r = random.nextDouble();
                Block b = loc.getBlock();
                b.setType(Material.WOOL, false);
                b.setData(r < rate ? tar : basedata, false);
            }
        }
    }

    public int lengthX() {
        return Top.getBlockX() - this.Bottom.getBlockX() + 1;
    }

    public int lengthZ() {
        return Top.getBlockZ() - this.Bottom.getBlockZ() + 1;
    }

    public Location getTop() {
        return Top;
    }

    public Location getBottom() {
        return Bottom;
    }

    public String getName() {
        return Name;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public String getOccupied() {
        return Occupied;
    }

    public int getOccupyTime() {
        return OccupyTime;
    }

    public Location[][] getBlocks() {
        return Blocks;
    }

    public void setDisplayName(String DisplayName) {
        this.DisplayName = DisplayName;
    }

    public void setOccupied(String Occupied) {
        this.Occupied = Occupied;
    }

    public void setOccupyTime(int OccupyTime) {
        this.OccupyTime = OccupyTime;
    }

    public Location getDisplayHolo() {
        return DisplayHolo.clone();
    }

    public void add(Player p) {
        if (!this.Occupying.contains(p)) {
            this.Occupying.add(p.getName());
        }
    }

}
