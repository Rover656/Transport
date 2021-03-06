package xyz.brassgoggledcoders.transport.compat.immersiveengineering;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.fml.ModList;
import xyz.brassgoggledcoders.transport.Transport;
import xyz.brassgoggledcoders.transport.api.entity.HullType;
import xyz.brassgoggledcoders.transport.content.TransportEntities;
import xyz.brassgoggledcoders.transport.content.TransportItemTags;
import xyz.brassgoggledcoders.transport.item.HulledBoatItem;
import xyz.brassgoggledcoders.transport.registrate.TransportRegistrateRecipes;

public class TransportIE {
    public static RegistryEntry<HullType> TREATED_WOOD_HULL = Transport.getRegistrate()
            .object("treated_wood")
            .hullType(itemSupplier -> new HullType(itemSupplier, Transport.rl("treated_wood_boat.png")))
            .lang("Treated Wood Hull")
            .defaultRecipe(TransportEntities.MODULAR_BOAT_ITEM::get)
            .item("boat", HulledBoatItem::new, itemBuilder -> itemBuilder
                    .tag(TransportItemTags.HULLS_BOAT)
                    .model((context, modelProvider) -> modelProvider.generated(context))
                    .recipe(TransportRegistrateRecipes.vehicleShape("forge:treated_wood")))
            .register();

    public static void setup() {
        if (ModList.get().isLoaded("immersiveengineering")) {
            IEDependentSetup.setup();
        }
    }
}
