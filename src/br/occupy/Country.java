/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package br.occupy;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-11-1
 */
public class Country {
    

    private String Name;
    private String DisplayName;
    private String Permission;
    private ChatColor Color;
    private byte DyeColor;

    public Country(ConfigurationSection config) {
        this.Name = config.getName();
        this.DisplayName = config.getString("Name");
        this.Permission = config.getString("Permission");
        this.Color = ChatColor.getByChar(config.getString("Color").charAt(0));
        this.DyeColor = (byte) config.getInt("DyeColor");
    }

    public String getName() {
        return Name;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public String getPermission() {
        return Permission;
    }

    public ChatColor getColor() {
        return Color;
    }

    public byte getDyeColor() {
        return DyeColor;
    }

}
