package net.estemon.codelabs111_recyclerrecipes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(Recipe recipe);

    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipes();

    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();
}
