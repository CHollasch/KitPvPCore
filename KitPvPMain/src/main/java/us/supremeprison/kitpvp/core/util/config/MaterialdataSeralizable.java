package us.supremeprison.kitpvp.core.util.config;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author Connor Hollasch
 * @since 6/21/2015
 */
public class MaterialdataSeralizable implements ConfigSerializable<MaterialData> {

    @Override
    public MaterialData load(String in) {
        return Material.getMaterial(Integer.parseInt(in.split(":")[0]))
                .getNewData(Byte.parseByte(in.split(":")[1]));
    }

    @Override
    public String save(MaterialData materialData) {
        return materialData.getItemTypeId() + ":" + materialData.getData();
    }

    @Override
    public Class<? extends MaterialData> getWrappedType() {
        return MaterialData.class;
    }
}
