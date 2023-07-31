package net.estemon.codelabs111_recyclerrecipes;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private final List<String> ids = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final List<String> resumes = new ArrayList<>();
    private final List<Uri> photos = new ArrayList<>();
    private final List<String> details = new ArrayList<>();


    private RecyclerView mRecyclerView;
    private RecipeAdapter mAdapter;

    private Uri photoUri;
    private ActivityResultLauncher<Uri> captureImageLauncher;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ids.addAll(Arrays.asList(this.getResources().getStringArray(R.array.recipe_ids)));

        populateLists(titles, resumes, photos, details);

        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecipeAdapter(titles, resumes, details, photos, this);

        // Registra el OnRecipeClickListener en el adaptador
        mAdapter.setOnRecipeClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_recipe_dialog, null);
        final ImageView previewRecipePhoto = dialogView.findViewById(R.id.preview_recipe_photo);

        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result) {
                            previewRecipePhoto.setImageURI(photoUri);
                        } else {
                            Toast.makeText(MainActivity.this, "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // FAB para añadir nuevas recetas
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddRecipe.setOnClickListener(view -> showAddRecipeDialog());
    }


    private void populateLists(List<String> titles, List<String> resumes, List<Uri> photos, List<String> details) {
        for (String id : ids) {
            // TODO: Verificar si existe el título
            @SuppressLint("DiscouragedApi") int titleResourceId = this.getResources().getIdentifier(id + "_recipe_title", "string", this.getPackageName());
            String title = this.getString(titleResourceId);
            titles.add(title);

            // TODO: Verificar si existe el resumen
            @SuppressLint("DiscouragedApi")int resumeResourceId = this.getResources().getIdentifier(id + "_recipe_resume", "string", this.getPackageName());
            String resume = this.getString(resumeResourceId);
            resumes.add(resume);

            // TODO: Verificar si existe la imagen
            @SuppressLint("DiscouragedApi")int photoResourceId = this.getResources().getIdentifier(id + "_recipe_photo", "drawable", this.getPackageName());
            if (photoResourceId != 0) {
                Uri photoUri = Uri.parse("android.resource://" + this.getPackageName() + "/" + photoResourceId);
                photos.add(photoUri);
            } else {
                photos.add(null);
            }
            Log.d("PhotosList", "Photo URI for ID " + id + ": " + photoResourceId);

            // TODO: Verificar si existe el detalle
            @SuppressLint("DiscouragedApi")int detailResourceId = this.getResources().getIdentifier(id + "_recipe_details", "string", this.getPackageName());
            String detail = this.getString(detailResourceId);
            details.add(detail);
        }
    }

    @Override
    public void onRecipeClick(String title, Uri photo, String content) {

        // Crear y mostrar el AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.recipe_detail_dialog, null);
        dialogBuilder.setView(dialogView);

        // Asignar los detalles de la receta al AlertDialog
        TextView titleTextView = dialogView.findViewById(R.id.recipe_detail_title);
        ImageView photoImageView = dialogView.findViewById(R.id.recipe_detail_photo);
        TextView contentTextView = dialogView.findViewById(R.id.recipe_detail_content);
        titleTextView.setText(title);
        contentTextView.setText(content);

        // Utiliza Glide para cargar la imagen desde la URI de cadena
        if (photo != null) {
            Glide.with(this)
                    .load(Uri.parse(String.valueOf(photo)))
                    .into(photoImageView);
        } else {
            // Si no hay imagen, muestra una imagen predeterminada
            Glide.with(this)
                    .load(R.drawable.ic_add) // Aquí debes tener un recurso de imagen predeterminada
                    .into(photoImageView);
        }

        // Mostrar el AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        // Obtener la referencia al botón de cierre y establecer el listener para cerrar el diálogo de la receta
        ImageButton closeButton = dialogView.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(view -> alertDialog.dismiss());
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_recipe_dialog, null);
        dialogBuilder.setView(dialogView);

        EditText addRecipeId = dialogView.findViewById(R.id.add_recipe_id);
        EditText addRecipeTitle = dialogView.findViewById(R.id.add_recipe_title);
        EditText addRecipeResume = dialogView.findViewById(R.id.add_recipe_resume);
        EditText addRecipeDetails = dialogView.findViewById(R.id.add_recipe_details);
        Button addRecipePhoto = dialogView.findViewById(R.id.add_recipe_photo);
        final ImageView previewRecipePhoto = dialogView.findViewById(R.id.preview_recipe_photo);

        addRecipePhoto.setOnClickListener(view -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = CustomFileProvider.getUriForFile(
                            MainActivity.this,
                            "net.estemon.codelabs111_recyclerrecipes.fileprovider",
                            photoFile
                    );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    captureImageLauncher.launch(photoUri);
                    Glide.with(MainActivity.this)
                            .load(photoUri)
                            .into(previewRecipePhoto);
                }
            }
        });

        dialogBuilder.setPositiveButton("Añadir receta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String recipeId = addRecipeId.getText().toString().trim();
                String recipeTitle = addRecipeTitle.getText().toString().trim();
                String recipeResume = addRecipeResume.getText().toString().trim();
                String recipeDetails = addRecipeDetails.getText().toString().trim();

                ids.add(recipeId);
                titles.add(recipeTitle);
                resumes.add(recipeResume);
                // photos.add(Integer.valueOf(String.valueOf(photoUri)));
                details.add(recipeDetails);

                if (photoUri != null) {
                    // int photoResId = R.drawable.ic_add;
                    photos.add(photoUri);
                } else {
                    photos.add(null);
                }

                // Notificar al adaptador que se ha agregado un nuevo elemento en la última posición
                int newPos = titles.size() - 1;
                mAdapter.notifyItemInserted(newPos);
                mAdapter.notifyDataSetChanged();

                //
                mRecyclerView.smoothScrollToPosition(newPos);
            }
        });

        dialogBuilder.setNegativeButton("Cancelar", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}