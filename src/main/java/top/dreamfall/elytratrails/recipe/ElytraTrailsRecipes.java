package top.dreamfall.elytratrails.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ElytraTrailsRecipes {
    public static final RecipeSerializer<TrailRecipe> TRAIL_RECIPE_SERIALIZER =
        Registry.register(Registries.RECIPE_SERIALIZER,
            Identifier.of("elytratrails", "trail"),
            new TrailRecipe.Serializer());

    public static final RecipeType<TrailRecipe> TRAIL_RECIPE_TYPE =
        Registry.register(Registries.RECIPE_TYPE,
            Identifier.of("elytratrails", "trail"),
            new RecipeType<TrailRecipe>() {});

    public static void init() {} // 触发类加载
}
