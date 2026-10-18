package top.dreamfall.elytratrails.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import top.dreamfall.elytratrails.ElytraTrails;

/**
 * 自定义工作台配方——书 + 原料 → 尾迹附魔书。
 * 继承 ShapelessRecipe 是为了让 JEI / 知识之书的匹配通过。
 *
 * 支持 2 或 3 个原料：base + addition [+ extra]。
 * extra 为 Ingredient.EMPTY 时表示只有 2 个原料。
 */
public class TrailRecipe extends ShapelessRecipe {
    private final Ingredient base;
    private final Ingredient addition;
    private final Ingredient extra;
    private final String enchantId;

    public TrailRecipe(Ingredient base, Ingredient addition, String enchantId) {
        this(base, addition, Ingredient.EMPTY, enchantId);
    }

    public TrailRecipe(Ingredient base, Ingredient addition, Ingredient extra, String enchantId) {
        super("", CraftingBookCategory.MISC, new ItemStack(Items.ENCHANTED_BOOK),
              makeIngredients(base, addition, extra));
        this.base = base;
        this.addition = addition;
        this.extra = extra;
        this.enchantId = enchantId;
    }

    private static NonNullList<Ingredient> makeIngredients(Ingredient base, Ingredient addition, Ingredient extra) {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(base);
        list.add(addition);
        if (extra != null && extra != Ingredient.EMPTY && !extra.isEmpty()) {
            list.add(extra);
        }
        return list;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider lookup) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ElytraTrails.MOD_ID, enchantId));
        Holder<Enchantment> entry = lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        b.set(entry, 1);
        book.set(DataComponents.STORED_ENCHANTMENTS, b.toImmutable());
        return book;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ElytraTrails.MOD_ID, enchantId));
        var optEntry = lookup.lookupOrThrow(Registries.ENCHANTMENT).get(key);
        if (optEntry.isEmpty()) return super.getResultItem(lookup);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        b.set(optEntry.get(), 1);
        book.set(DataComponents.STORED_ENCHANTMENTS, b.toImmutable());
        return book;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ElytraTrails.TRAIL_RECIPE_SERIALIZER.get();
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    public Ingredient base() { return base; }
    public Ingredient addition() { return addition; }
    public Ingredient extra() { return extra; }
    public String enchantId() { return enchantId; }

    public static class Serializer implements RecipeSerializer<TrailRecipe> {
        public static final MapCodec<TrailRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("base").forGetter(TrailRecipe::base),
                Ingredient.CODEC.fieldOf("addition").forGetter(TrailRecipe::addition),
                Ingredient.CODEC.optionalFieldOf("extra", Ingredient.EMPTY).forGetter(TrailRecipe::extra),
                Codec.STRING.fieldOf("enchant_id").forGetter(TrailRecipe::enchantId)
            ).apply(instance, TrailRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, TrailRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.base);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.addition);
                boolean hasExtra = recipe.extra != null && recipe.extra != Ingredient.EMPTY && !recipe.extra.isEmpty();
                buf.writeBoolean(hasExtra);
                if (hasExtra) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.extra);
                }
                ByteBufCodecs.STRING_UTF8.encode(buf, recipe.enchantId);
            },
            buf -> {
                Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                Ingredient addition = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                boolean hasExtra = buf.readBoolean();
                Ingredient extra = hasExtra ? Ingredient.CONTENTS_STREAM_CODEC.decode(buf) : Ingredient.EMPTY;
                String enchantId = ByteBufCodecs.STRING_UTF8.decode(buf);
                return new TrailRecipe(base, addition, extra, enchantId);
            }
        );

        @Override
        public MapCodec<TrailRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TrailRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
