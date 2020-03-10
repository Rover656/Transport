package xyz.brassgoggledcoders.transport.item;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.brassgoggledcoders.transport.Transport;
import xyz.brassgoggledcoders.transport.api.TransportAPI;
import xyz.brassgoggledcoders.transport.api.cargo.Cargo;
import xyz.brassgoggledcoders.transport.api.cargoinstance.CargoInstance;
import xyz.brassgoggledcoders.transport.content.TransportEntities;
import xyz.brassgoggledcoders.transport.entity.CargoCarrierMinecartEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.entity.item.minecart.AbstractMinecartEntity.Type.CHEST;

public class CargoCarrierMinecartItem extends MinecartItem {
    private final static Object2FloatMap<ResourceLocation> cargoModels = loadModels();

    public CargoCarrierMinecartItem() {
        this(new Item.Properties()
                .group(Transport.ITEM_GROUP));
        this.addPropertyOverride(new ResourceLocation(Transport.ID, "cargo"),
                (itemStack, world, livingEntity) -> Optional.ofNullable(getCargo(itemStack.getChildTag("cargo")))
                        .map(ForgeRegistryEntry::getRegistryName)
                        .map(TransportAPI.CARGO::getID)
                        .map(id -> id / 1000F)
                        .orElse(0.000F));
    }

    public CargoCarrierMinecartItem(Properties properties) {
        super(CHEST, properties);
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getPos();
        BlockState blockstate = world.getBlockState(blockPos);
        if (!blockstate.isIn(BlockTags.RAILS)) {
            return ActionResultType.FAIL;
        } else {
            ItemStack itemstack = context.getItem();
            if (!world.isRemote) {
                RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockPos, null) : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;
                if (railshape.isAscending()) {
                    d0 = 0.5D;
                }

                CargoCarrierMinecartEntity cargoCarrierMinecartEntity = new CargoCarrierMinecartEntity(world,
                        getCargo(itemstack.getChildTag("cargo")), blockPos.getX() + 0.5D,
                        blockPos.getY() + 0.0625D + d0, blockPos.getZ() + 0.5D);
                if (itemstack.hasDisplayName()) {
                    cargoCarrierMinecartEntity.setCustomName(itemstack.getDisplayName());
                }

                world.addEntity(cargoCarrierMinecartEntity);
            }

            itemstack.shrink(1);
            return ActionResultType.SUCCESS;
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            TransportAPI.CARGO.getValues()
                    .stream()
                    .filter(cargo -> !cargo.isEmpty())
                    .map(Cargo::getRegistryName)
                    .filter(Objects::nonNull)
                    .map(ResourceLocation::toString)
                    .map(resourceLocation -> {
                        ItemStack itemStack = new ItemStack(this);
                        itemStack.getOrCreateChildTag("cargo").putString("name", resourceLocation);
                        return itemStack;
                    })
                    .forEach(items::add);
        }
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        return new TranslationTextComponent("text.transport.with",
                Items.MINECART.getDisplayName(stack),
                getCargo(stack.getChildTag("cargo")).getDisplayName());
    }

    public static ItemStack getCartStack(CargoInstance cargoInstance) {
        ItemStack itemStack = new ItemStack(TransportEntities.CARGO_MINECART_ITEM
                .map(Item::asItem)
                .orElse(Items.MINECART));
        itemStack.getOrCreateChildTag("cargo").putString("name", Objects.requireNonNull(
                cargoInstance.getCargo().getRegistryName()).toString());
        return itemStack;
    }

    public static Cargo getCargo(@Nullable CompoundNBT cargo) {
        return Optional.ofNullable(cargo)
                .map(compoundNBT -> compoundNBT.getString("name"))
                .map(ResourceLocation::new)
                .map(TransportAPI.CARGO::getValue)
                .orElseGet(TransportAPI.EMPTY_CARGO);
    }


    private static Object2FloatMap<ResourceLocation> loadModels() {
        Object2FloatMap<ResourceLocation> models = new Object2FloatOpenHashMap<>();
        models.put(new ResourceLocation(Transport.ID, "item"), 0.001F);
        models.put(new ResourceLocation(Transport.ID, "energy"), 0.002F);
        models.put(new ResourceLocation(Transport.ID, "fluid"), 0.003F);
        return models;
    }
}
