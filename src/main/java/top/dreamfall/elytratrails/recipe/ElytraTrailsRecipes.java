package top.dreamfall.elytratrails.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ElytraTrailsRecipes {
    public static final RecipeSerializer<TrailRecipe> SERIALIZER =
        Registry.register(Registries.RECIPE_SERIALIZER,
            Identifier.of("elytratrails", "trail"), new TrailRecipe.Serializer());

    public static void init() {}
}
