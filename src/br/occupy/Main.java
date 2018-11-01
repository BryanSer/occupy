/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */

package br.occupy;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-11-1
 */
public class Main extends JavaPlugin{

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(Area.class);
    }

}
