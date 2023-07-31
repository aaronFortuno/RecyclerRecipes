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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {

    private final List<String> titles;
    private final List<String> resumes;
    private final List<String> details;
    private final List<Uri> photos;

    private OnRecipeClickListener recipeClickListener;

    public RecipeAdapter(List<String> titles, List<String> resumes, List<String> details, List<Uri> photos, OnRecipeClickListener listener) {
        this.titles = titles;
        this.resumes = resumes;
        this.details = details;
        this.photos = photos;
        recipeClickListener = listener;
    }

    @NonNull
    @Override
    public RecipeAdapter.RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new RecipeHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeHolder holder, int position) {
        String title = titles.get(holder.getAdapterPosition());
        String resume = resumes.get(holder.getAdapterPosition());
        String detail = details.get(holder.getAdapterPosition());
        Uri photoUri = photos.get(holder.getAdapterPosition());

        holder.recipeTitleView.setText(title);
        holder.recipeResumeView.setText(resume);

        if (photoUri != null) {
            // photoUri = Uri.parse(String.valueOf(photoResId));
            Log.d("PopulateLists Glide", "photoUri: " + photoUri);
            Glide.with(holder.itemView.getContext())
                    .load(photoUri)
                    .error(R.drawable.ic_add) // Imagen predeterminada si no se puede cargar la imagen
                    .into(holder.recipePhotoView);
        } else {
            // Si no hay imagen, muestra una imagen predeterminada
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_add)
                    .into(holder.recipePhotoView);
        }
        holder.itemView.setOnClickListener(view -> {
            if (recipeClickListener != null) {
                recipeClickListener.onRecipeClick(title, photoUri, detail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(String title, Uri photo, String content);
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.recipeClickListener = listener;
    }

    static class RecipeHolder extends RecyclerView.ViewHolder {
        public final TextView recipeTitleView;
        public final TextView recipeResumeView;
        public final ImageView recipePhotoView;

        public final TextView recipeDetailTitleView;
        public final TextView recipeDetailContentView;
        public final ImageView recipeDetailPhotoView;

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