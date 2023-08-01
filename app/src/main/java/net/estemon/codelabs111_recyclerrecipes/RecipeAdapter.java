package net.estemon.codelabs111_recyclerrecipes;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter class for displaying recipes in a RecyclerView.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {

    private final List<Recipe> recipes;

    // Interface to handle clicks in the recipe elements.
    private OnRecipeClickListener recipeClickListener;

    /**
     * Constructor for the RecipeAdapter.
     *
     * @param recipes  The recipes of the DB
     */
    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    /**
     * Called when the RecyclerView needs a new {@link RecipeHolder} to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link RecipeHolder} that holds a View of the given view type.
     */
    @NonNull
    @Override
    public RecipeAdapter.RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new RecipeHolder(recipeView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeHolder holder, int position) {

        // Retrieve the data for the current position
        Recipe recipe = recipes.get(position);
        String title = recipe.getTitle();
        String resume = recipe.getResume();
        String detail = recipe.getDetails();
        String photoUriString = recipe.getPhoto();
        Uri photoUri = Uri.parse(photoUriString);

        // Set the id, title and resume in the corresponding views
        holder.itemView.setTag(position);
        holder.recipeTitleView.setText(title);
        holder.recipeResumeView.setText(resume);

        // Load the image using Glide, if available
        if (photoUri != null) {
            Log.d("PopulateLists Glide", "photoUri: " + photoUri);
            Glide.with(holder.itemView.getContext())
                    .load(photoUri)
                    .error(R.drawable.ic_add) // // Show a default image if the photoUri cannot be loaded
                    .into(holder.recipePhotoView);
        } else {
            // If there is no image, show a default image
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_add)
                    .into(holder.recipePhotoView);
        }

        // Set the click listener for the item view
        holder.itemView.setOnClickListener(view -> {
            if (recipeClickListener != null) {
                // Invoke the callback method when the item view is clicked
                recipeClickListener.onRecipeClick(title, photoUri, detail);
            }
        });

        holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> menu.add(0, 0, position, "Eliminar receta"));
    }

    /**
     * Returns the total number of recipes in the data set.
     *
     * @return The total number of recipes in the data set.
     */
    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Interface definition for a callback to be invoked when a recipe item is clicked in the RecyclerView.
     */
    public interface OnRecipeClickListener {

        /**
         * Called when a recipe item is clicked.
         *
         * @param title   The title of the clicked recipe.
         * @param photo   The Uri of the photo associated with the clicked recipe, or null if no photo is available.
         * @param content The content/details of the clicked recipe.
         */
        void onRecipeClick(String title, Uri photo, String content);
    }

    /**
     * Sets the listener for handling recipe item click events.
     *
     * @param listener The listener to be set for handling recipe item click events.
     */
    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.recipeClickListener = listener;
    }

    /**
     * ViewHolder for holding the views of a recipe item in the RecyclerView.
     */
    static class RecipeHolder extends RecyclerView.ViewHolder {

        /**
         * TextView and ImageView to display the title, resume,
         * photo and details of the recipe.
         */
        public final TextView recipeTitleView;
        public final TextView recipeResumeView;
        public final ImageView recipePhotoView;

        public final TextView recipeDetailTitleView;
        public final TextView recipeDetailContentView;
        public final ImageView recipeDetailPhotoView;

        /**
         * Constructor for the RecipeHolder.
         *
         * @param recipeView The View that contains the views for a recipe item.
         */
        public RecipeHolder(@NonNull View recipeView) {
            super(recipeView);
            LinearLayout layout = recipeView.findViewById(R.id.recipe_layout);
            recipeTitleView = layout.findViewById(R.id.recipe_title);
            recipeResumeView = layout.findViewById(R.id.recipe_resume);
            recipePhotoView = layout.findViewById(R.id.recipe_photo);

            recipeDetailTitleView = layout.findViewById(R.id.recipe_detail_title);
            recipeDetailContentView = layout.findViewById(R.id.recipe_detail_content);
            recipeDetailPhotoView = layout.findViewById(R.id.recipe_detail_photo);
        }
    }
}