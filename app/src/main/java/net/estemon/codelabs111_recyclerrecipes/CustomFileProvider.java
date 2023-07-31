package net.estemon.codelabs111_recyclerrecipes;

import androidx.core.content.FileProvider;

public class CustomFileProvider extends FileProvider {

    public CustomFileProvider() {
        super(R.xml.file_paths);
    }
}
