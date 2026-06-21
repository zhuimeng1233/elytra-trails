package top.dreamfall.elytratrails.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TrailRecipe implements CraftingRecipe {
    private final Ingredient base;
    private final Ingredient addition;
    private final String enchantId;

    private static final String[] COLORS = {
        "white","orange","magenta","light_blue","yellow","lime",
        "pink","gray","light_gray","cyan","purple","blue",
        "brown","green","red","black"
    };

    private static final Item[] DYE_ITEMS = {
        Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
        Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
        Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE,
        Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE
    };

    public TrailRecipe(Ingredient base, Ingredient addition, String enchantId) {
        this.base = base;
        this.addition = addition;
        this.enchantId = enchantId;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean hasBase = false, hasAddition = false;
        int nonEmpty = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            nonEmpty++;
            if (nonEmpty > 2) return false;

            if (base.test(stack)) {
                if (hasBase) return false;
                hasBase = true;
            } else if (addition.test(stack)) {
                if (hasAddition) return false;
                hasAddition = true;
            } else {
                return false;
            }
        }
        return nonEmpty == 2 && hasBase && hasAddition;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        String id = enchantId;
        if ("dye".equals(id)) {
            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.getStackInSlot(i);
                if (stack.isEmpty() || base.test(stack)) continue;
                int ci = getColorIndex(stack);
                if (ci >= 0) { id = COLORS[ci] + "_trail"; break; }
            }
        }

        var key = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("elytratrails", id));
        var enchantEntry = lookup.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(key);

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        builder.add(enchantEntry, 1);
        book.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        return book;
    }

    @Override
    public RecipeSerializer<TrailRecipe> getSerializer() { return ElytraTrailsRecipes.TRAIL_RECIPE_SERIALIZER; }

    @Override
    public CraftingRecipeCategory getCategory() { return CraftingRecipeCategory.MISC; }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.forShapeless(java.util.List.of(base, addition));
    }

    // getType() / getRecipeBookCategory() inherit from CraftingRecipe (default)

    private int getColorIndex(ItemStack stack) {
        for (int i = 0; i < DYE_ITEMS.length; i++) if (stack.isOf(DYE_ITEMS[i])) return i;
        return -1;
    }

    public Ingredient base() { return base; }
    public Ingredient addition() { return addition; }
    public String enchantId() { return enchantId; }

    public static class Serializer implements RecipeSerializer<TrailRecipe> {
        public static final MapCodec<TrailRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("base").forGetter(TrailRecipe::base),
                Ingredient.CODEC.fieldOf("addition").forGetter(TrailRecipe::addition),
                Codec.STRING.fieldOf("enchant_id").forGetter(TrailRecipe::enchantId)
            ).apply(instance, TrailRecipe::new)
        );

        public static final PacketCodec<RegistryByteBuf, TrailRecipe> PACKET_CODEC =
            PacketCodec.tuple(
                Ingredient.PACKET_CODEC, TrailRecipe::base,
                Ingredient.PACKET_CODEC, TrailRecipe::addition,
                PacketCodecs.STRING, TrailRecipe::enchantId,
                TrailRecipe::new
            );

        @Override public MapCodec<TrailRecipe> codec() { return CODEC; }
        @Override public PacketCodec<RegistryByteBuf, TrailRecipe> packetCodec() { return PACKET_CODEC; }
    }
}
