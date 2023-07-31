package net.estemon.codelabs111_recyclerrecipes;

import android.annotation.SuppressLint;
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

/**
 * MainActivity que muestra una lista de recetas en un RecyclerView.
 * Permite agregar nuevas recetas y ver los detalles de las recetas existentes.
 */
public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    // Listas para almacenar los datos de las recetas
    private final List<String> ids = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final List<String> resumes = new ArrayList<>();
    private final List<Uri> photos = new ArrayList<>();
    private final List<String> details = new ArrayList<>();


    private RecyclerView mRecyclerView;
    private RecipeAdapter mAdapter;

    // URI de la foto capturada con la cámara
    private Uri photoUri;

    // ActivityResultLauncher para capturar la imagen
    private ActivityResultLauncher<Uri> captureImageLauncher;


    public MainActivity() {
    }

    /**
     * Crea y configura la actividad, inicializa la interfaz de usuario y las listas de recetas.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa la lista de IDs de recetas desde los recursos
        ids.addAll(Arrays.asList(this.getResources().getStringArray(R.array.recipe_ids)));

        // Llena las listas de recetas con los datos de los recursos
        populateLists(titles, resumes, photos, details);

        // Configura el RecyclerView y el adaptador
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecipeAdapter(titles, resumes, details, photos, this);

        // Registra el OnRecipeClickListener en el adaptador
        mAdapter.setOnRecipeClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_recipe_dialog, null);
        final ImageView previewRecipePhoto = dialogView.findViewById(R.id.preview_recipe_photo);

        // Configura el lanzador para capturar imágenes de la cámara
        setupImageCaptureLauncher(previewRecipePhoto);

        // Configura el botón flotante para agregar nuevas recetas
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddRecipe.setOnClickListener(view -> showAddRecipeDialog());
    }

    /**
     * Configura el ActivityResultLauncher para capturar imágenes de la cámara.
     * Establece el resultado de la captura de la imagen en el ImageView de vista previa.
     */
    private void setupImageCaptureLauncher(ImageView previewRecipePhoto) {
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        previewRecipePhoto.setImageURI(photoUri);
                    } else {
                        Toast.makeText(MainActivity.this, "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Llena las listas de recetas con los datos de los recursos.
     *
     * @param titles   Lista para almacenar los títulos de las recetas.
     * @param resumes  Lista para almacenar los resúmenes de las recetas.
     * @param photos   Lista para almacenar las URIs de las fotos de las recetas.
     * @param details  Lista para almacenar los detalles de las recetas.
     */
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

    /**
     * Método llamado cuando se hace clic en una receta en la lista.
     * Muestra un cuadro de diálogo que muestra los detalles de la receta, incluido el título, contenido y una foto.
     *
     * @param title   Título de la receta.
     * @param photo   URI de la foto de la receta. Puede ser nulo si no hay foto disponible.
     * @param content Contenido o detalles de la receta.
     */
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

    /**
     * Muestra un cuadro de diálogo para agregar una nueva receta.
     * Permite al usuario ingresar los detalles de la receta y capturar una foto con la cámara.
     */
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

        /*
          Define el comportamiento del botón de confirmar los datos del diálogo
          En este caso, mostrar en el listado de recetas los datos de la nueva receta
         */
        dialogBuilder.setPositiveButton("Añadir receta", (dialogInterface, i) -> {
            String recipeId = addRecipeId.getText().toString().trim();
            String recipeTitle = addRecipeTitle.getText().toString().trim();
            String recipeResume = addRecipeResume.getText().toString().trim();
            String recipeDetails = addRecipeDetails.getText().toString().trim();

            ids.add(recipeId);
            titles.add(recipeTitle);
            resumes.add(recipeResume);
            details.add(recipeDetails);

            if (photoUri != null) {
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
        });

        dialogBuilder.setNegativeButton("Cancelar", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Crea un archivo de imagen temporal para guardar la foto capturada con la cámara.
     *
     * @return El objeto File que representa el archivo de imagen temporal.
     */
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