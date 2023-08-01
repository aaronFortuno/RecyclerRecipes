package net.estemon.codelabs111_recyclerrecipes;

import android.util.Log;

import java.util.List;

public class InsertExampleRecipeRunnable implements Runnable {

    private RecipeDao recipeDao;
    private MainActivity mainActivity;

    public InsertExampleRecipeRunnable(RecipeDao recipeDao, MainActivity mainActivity) {
        this.recipeDao = recipeDao;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        if (recipeDao.getRecipeCount() == 0) {
            Recipe recipe1 = new Recipe();
            recipe1.setId(1);
            recipe1.setTitle("Receta 1");
            recipe1.setResume("Resumen de la receta 1");
            recipe1.setDetails("Detalles de la receta 1");
            recipe1.setPhoto("content://net.estemon.codelabs111_recyclerrecipes/drawable/ic_add");
            recipeDao.insertRecipe(recipe1);
            Log.i("InsertExampleRecipeRunnable", "Inserted id " + recipe1.getId());

            List<Recipe> allRecipes = recipeDao.getAllRecipes();
            mainActivity.runOnUiThread(() -> {
                mainActivity.recipes.clear();
                mainActivity.recipes.addAll(allRecipes);
                mainActivity.mAdapter.notifyDataSetChanged();
            });
        }

        mainActivity.loadRecipes();
    }
}
