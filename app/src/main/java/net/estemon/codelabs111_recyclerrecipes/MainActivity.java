package net.estemon.codelabs111_recyclerrecipes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * MainActivity que muestra una lista de recetas en un RecyclerView.
 * Permite agregar nuevas recetas y ver los detalles de las recetas existentes.
 */
public class MainActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener, View.OnCreateContextMenuListener, LanguageFragment.LanguageFragmentListener {

    // Lista para almacenar los datos de las recetas
    final List<Recipe> recipes = new ArrayList<>();

    RecipeAdapter mAdapter;

    // URI de la foto capturada con la cámara
    private Uri photoUri;

    // ActivityResultLauncher para capturar la imagen
    private ActivityResultLauncher<Uri> captureImageLauncher;

    private RecipeDao recipeDao;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public MainActivity() {
    }

    /**
     * Crea y configura la actividad, inicializa la interfaz de usuario y las listas de recetas.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicialización de la BD en Room
        initDatabase();

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setHomeButtonEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_change_language) {
                openLanguageChangeFragment();
            } else if (item.getItemId() == R.id.menu_change_theme) {
                openThemeChangeFragment();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Configura el RecyclerView y el adaptador
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecipeAdapter(recipes);

        // Registra el OnRecipeClickListener en el adaptador
        mAdapter.setOnRecipeClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.add_recipe_dialog, null);
        final ImageView previewRecipePhoto = dialogView.findViewById(R.id.preview_recipe_photo);

        // Configura el lanzador para capturar imágenes de la cámara
        setupImageCaptureLauncher(previewRecipePhoto);

        // Configura el botón flotante para agregar nuevas recetas
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddRecipe.setOnClickListener(view -> showAddRecipeDialog());

        if (savedInstanceState == null) {
            initDatabase();
            insertExampleRecipe();
        }

        registerForContextMenu(mRecyclerView);
    }

    private void openLanguageChangeFragment() {
        LanguageFragment languageFragment = new LanguageFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout_content, languageFragment)
                .addToBackStack(null)
                .commit();

        drawerLayout.closeDrawer(GravityCompat.START);

    }

    private void openThemeChangeFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.recycler_view) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.recipe_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getOrder();
        switch (item.getItemId()) {
            case 0:
                showModifyRecipeDialog(position);
                return true;
            case 1:
                // Eliminar la receta de la base de datos
                Recipe recipeToDelete = recipes.get(position);
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    recipeDao.deleteRecipe(recipeToDelete);
                    loadRecipes();
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void initDatabase() {
        AppDatabase appDatabase = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class,
                        "recipe_database")
                .build();
        recipeDao = appDatabase.recipeDao();
        insertExampleRecipe();
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
    @SuppressLint({"QueryPermissionsNeeded", "NotifyDataSetChanged"})
    private void showAddRecipeDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_recipe_dialog, null);
        dialogBuilder.setView(dialogView);

        EditText addRecipeTitle = dialogView.findViewById(R.id.add_recipe_title);
        EditText addRecipeResume = dialogView.findViewById(R.id.add_recipe_resume);
        EditText addRecipeDetails = dialogView.findViewById(R.id.add_recipe_details);
        Button addRecipePhoto = dialogView.findViewById(R.id.add_recipe_photo);
        final ImageView previewRecipePhoto = dialogView.findViewById(R.id.preview_recipe_photo);

        addRecipePhotoAction(addRecipePhoto, previewRecipePhoto);

        dialogBuilder.setPositiveButton("Añadir receta", null);
        dialogBuilder.setNegativeButton("Cancelar", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        /*
          Define el comportamiento del botón de confirmar los datos del diálogo
          En este caso, mostrar en el listado de recetas los datos de la nueva receta
         */
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String recipeTitle = addRecipeTitle.getText().toString().trim();
            String recipeResume = addRecipeResume.getText().toString().trim();
            String recipeDetails = addRecipeDetails.getText().toString().trim();

            if (checkDataCompleteness(recipeTitle, recipeResume, recipeDetails))
                return; // Detener el proceso de guardado si falta información obligatoria

            Recipe newRecipe = new Recipe();
            newRecipe.setTitle(recipeTitle);
            newRecipe.setResume(recipeResume);
            newRecipe.setDetails(recipeDetails);
            if (photoUri != null) {
                newRecipe.setPhoto(photoUri.toString());
            } else {
                Uri defaultPhotoUri = Uri.parse("content://" + getPackageName() + "/res/drawable/ic_add.png");
                newRecipe.setPhoto(defaultPhotoUri.toString());
            }

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                recipeDao.insertRecipe(newRecipe);
                loadRecipes();
            });

            int newPos = recipes.size() - 1;
            mAdapter.notifyItemInserted(newPos);
            mAdapter.notifyDataSetChanged();

            alertDialog.dismiss();
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void addRecipePhotoAction(Button addRecipePhoto, ImageView previewRecipePhoto) {
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
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void showModifyRecipeDialog(int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_recipe_dialog, null);
        dialogBuilder.setView(dialogView);

        EditText addRecipeTitle = dialogView.findViewById(R.id.add_recipe_title);
        EditText addRecipeResume = dialogView.findViewById(R.id.add_recipe_resume);
        EditText addRecipeDetails = dialogView.findViewById(R.id.add_recipe_details);
        Button addRecipePhoto = dialogView.findViewById(R.id.add_recipe_photo);
        final ImageView previewRecipePhoto = dialogView.findViewById(R.id.preview_recipe_photo);

        Recipe recipe = recipes.get(position);
        addRecipeTitle.setText(recipe.getTitle());
        addRecipeResume.setText(recipe.getResume());
        addRecipeDetails.setText(recipe.getDetails());
        Glide.with(MainActivity.this)
                .load(photoUri)
                .into(previewRecipePhoto);

        addRecipePhotoAction(addRecipePhoto, previewRecipePhoto);

        dialogBuilder.setPositiveButton("Guardar cambios", null);
        dialogBuilder.setNegativeButton("Cancelar", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        /*
          Define el comportamiento del botón de confirmar los datos del diálogo
          En este caso, mostrar en el listado de recetas los datos de la nueva receta
         */
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String recipeTitle = addRecipeTitle.getText().toString().trim();
            String recipeResume = addRecipeResume.getText().toString().trim();
            String recipeDetails = addRecipeDetails.getText().toString().trim();

            if (checkDataCompleteness(recipeTitle, recipeResume, recipeDetails))
                return; // Detener el proceso de guardado si falta información obligatoria

            recipe.setTitle(recipeTitle);
            recipe.setResume(recipeResume);
            recipe.setDetails(recipeDetails);

            // Update the photo URI only if the user has taken a new photo
            if (photoUri != null) {
                recipe.setPhoto(photoUri.toString());
            }

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                recipeDao.insertRecipe(recipe);
                loadRecipes();
            });
        });


    }

    private boolean checkDataCompleteness(String recipeTitle, String recipeResume, String recipeDetails) {
        if (recipeTitle.trim().isEmpty() || recipeResume.trim().isEmpty() || recipeDetails.trim().isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    void loadRecipes() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Recipe> recipeList = recipeDao.getAllRecipes();
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(recipeList);
                mAdapter.notifyDataSetChanged();
            });
        });
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
            return File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onLanguageFragmentClosed() {
        // Cerrar el Fragment
        getSupportFragmentManager().popBackStack();
    }
}