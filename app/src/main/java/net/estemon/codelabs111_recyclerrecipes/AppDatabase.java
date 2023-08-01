package net.estemon.codelabs111_recyclerrecipes;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Recipe.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RecipeDao recipeDao();

    public boolean isDatabaseEmpty() {
        RecipeDao recipeDao = recipeDao();
        int count = recipeDao.getRecipeCount();
        return count == 0;
    }

    public static AppDatabase getInstance(Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "recipe-database")
                .allowMainThreadQueries() // TODO: No permitido en producción, solo para simplificar el ejemplo
                .build();
    }
}
