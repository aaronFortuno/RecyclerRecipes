package net.estemon.codelabs111_recyclerrecipes;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class LanguageFragment extends Fragment {

    private Spinner spinnerLanguages;
    private LanguageFragmentListener listener;

    public LanguageFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language, container, false);

        spinnerLanguages = view.findViewById(R.id.spinner_languages);
        Button btnApplyLanguage = view.findViewById(R.id.btn_apply_language);
        Button btnExitLanguage = view.findViewById(R.id.btn_exit_language);

        // Lista de idiomas disponibles para el spinner
        String[] languages = {"Español", "Català", "English"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguages.setAdapter(adapter);

        btnApplyLanguage.setOnClickListener(v -> applySelectedLanguage());
        btnExitLanguage.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;

    }

    private void applySelectedLanguage() {
        String selectedLanguage = (String) spinnerLanguages.getSelectedItem();
        Locale locale;
        switch (selectedLanguage) {
            case "Español":
                locale = new Locale("es");
                break;
            case "Català":
                locale = new Locale("ca");
                break;
            case "English":
            default:
                locale = new Locale("en");
                break;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Recarga la actividad actual para aplicar el idioma seleccionado
        requireActivity().recreate();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    public interface LanguageFragmentListener {

        void onLanguageFragmentClosed();
    }
}
