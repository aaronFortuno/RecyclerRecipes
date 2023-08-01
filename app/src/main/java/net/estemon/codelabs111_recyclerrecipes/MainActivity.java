package net.estemon.codelabs111_recyclerrecipes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * MainActivity que muestra una lista de recetas en un RecyclerView.
 * Permite agregar nuevas recetas y ver los detalles de las recetas existentes.
 */
public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    // Lista para almacenar los datos de las recetas
    final List<Recipe> recipes = new ArrayList<>();

    private RecyclerView mRecyclerView;
    RecipeAdapter mAdapter;

    // URI de la foto capturada con la cámara
    private Uri photoUri;

    // ActivityResultLauncher para capturar la imagen
    private ActivityResultLauncher<Uri> captureImageLauncher;

    private AppDatabase appDatabase;
    private RecipeDao recipeDao;

    public MainActivity() {
    }

    /**
     * Crea y configura la actividad, inicializa la interfaz de usuario y las listas de recetas.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicialización de la BD en Room
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class,
                "recipe_database")
                        .build();
        recipeDao = appDatabase.recipeDao();
        insertExampleRecipe();

        setContentView(R.layout.activity_main);

        // Configura el RecyclerView y el adaptador
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecipeAdapter(recipes, this);

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

    private void insertExampleRecipe() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new InsertExampleRecipeRunnable(recipeDao, this));
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
            int recipeId = addRecipeId.getId();
            String recipeTitle = addRecipeTitle.getText().toString().trim();
            String recipeResume = addRecipeResume.getText().toString().trim();
            String recipeDetails = addRecipeDetails.getText().toString().trim();

            Recipe newRecipe = new Recipe();
            newRecipe.setId(recipeId);
            newRecipe.setTitle(recipeTitle);
            newRecipe.setResume(recipeResume);
            newRecipe.setDetails(recipeDetails);
            if (photoUri != null) {
                newRecipe.setPhoto(photoUri.toString());
            } else {
                newRecipe.setPhoto(null);
            }

            recipes.add(newRecipe);

            // Notificar al adaptador que se ha agregado un nuevo elemento en la última posición
            int newPos = recipes.size() - 1;
            mAdapter.notifyItemInserted(newPos);
            mAdapter.notifyDataSetChanged();

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