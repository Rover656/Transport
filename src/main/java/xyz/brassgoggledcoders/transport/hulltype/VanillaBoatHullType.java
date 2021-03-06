package xyz.brassgoggledcoders.transport.hulltype;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import xyz.brassgoggledcoders.transport.api.entity.HullType;

public class VanillaBoatHullType extends HullType {
    public VanillaBoatHullType(NonNullSupplier<Item> boatSupplier, BoatEntity.Type type) {
        super(boatSupplier, () -> new ResourceLocation("textures/entity/boat/" + type.getName() + ".png"));
    }
}
