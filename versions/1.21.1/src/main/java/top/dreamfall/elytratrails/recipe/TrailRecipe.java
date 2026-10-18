package top.dreamfall.elytratrails.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
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
import net.minecraft.util.collection.DefaultedList;

/**
 * 自定义工作台配方——书 + 原料 → 尾迹附魔书。
 * 继承 {@link ShapelessRecipe} 是为了让 JEI 的 isHandled() 检查通过，
 * 否则 JEI 会把自定义 CraftingRecipe 归类为 unhandled 然后丢弃。
 * <p>
 * 支持 2 或 3 个原料：base + addition [+ extra]。
 * extra 为 {@link Ingredient#EMPTY} 时表示只有 2 个原料。
 */
public class TrailRecipe extends ShapelessRecipe {
    private final Ingredient base, addition, extra;
    private final String enchantId;

    /** 2-原料构造器（大多数配方） */
    public TrailRecipe(Ingredient base, Ingredient addition, String enchantId) {
        this(base, addition, Ingredient.EMPTY, enchantId);
    }

    /** 3-原料构造器（如 multi_trail：书 + 火药 + 燧石） */
    public TrailRecipe(Ingredient base, Ingredient addition, Ingredient extra, String enchantId) {
        super("", CraftingRecipeCategory.MISC, Items.ENCHANTED_BOOK.getDefaultStack(),
              makeIngredients(base, addition, extra));
        this.base = base;
        this.addition = addition;
        this.extra = extra;
        this.enchantId = enchantId;
    }

    private static DefaultedList<Ingredient> makeIngredients(Ingredient base, Ingredient addition, Ingredient extra) {
        DefaultedList<Ingredient> list = DefaultedList.of();
        list.add(base);
        list.add(addition);
        if (extra != Ingredient.EMPTY) {
            list.add(extra);
        }
        return list;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var key = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("elytratrails", enchantId));
        var entry = lookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(key);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var b = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        b.add(entry, 1);
        book.set(DataComponentTypes.STORED_ENCHANTMENTS, b.build());
        return book;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
        var key = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("elytratrails", enchantId));
        var optEntry = lookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOptional(key);
        if (optEntry.isEmpty()) return super.getResult(lookup);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var b = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        b.add(optEntry.get(), 1);
        book.set(DataComponentTypes.STORED_ENCHANTMENTS, b.build());
        return book;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ElytraTrailsRecipes.SERIALIZER;
    }

    @Override
    public boolean isIgnoredInRecipeBook() { return false; }

    // ---- getters for serializer ----

    public Ingredient base() { return base; }
    public Ingredient addition() { return addition; }
    public Ingredient extra() { return extra; }
    public String enchantId() { return enchantId; }

    // ---- serializer ----

    public static class Serializer implements RecipeSerializer<TrailRecipe> {
        public static final MapCodec<TrailRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("base").forGetter(TrailRecipe::base),
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("addition").forGetter(TrailRecipe::addition),
                Ingredient.DISALLOW_EMPTY_CODEC.optionalFieldOf("extra", Ingredient.EMPTY).forGetter(TrailRecipe::extra),
                Codec.STRING.fieldOf("enchant_id").forGetter(TrailRecipe::enchantId))
            .apply(instance, TrailRecipe::new));

        public static final PacketCodec<RegistryByteBuf, TrailRecipe> PACKET_CODEC =
            PacketCodec.tuple(Ingredient.PACKET_CODEC, TrailRecipe::base,
                Ingredient.PACKET_CODEC, TrailRecipe::addition,
                Ingredient.PACKET_CODEC, TrailRecipe::extra,
                PacketCodecs.STRING, TrailRecipe::enchantId, TrailRecipe::new);

        @Override public MapCodec<TrailRecipe> codec() { return CODEC; }
        @Override public PacketCodec<RegistryByteBuf, TrailRecipe> packetCodec() { return PACKET_CODEC; }
    }
}
