package com.dabomstew.pkrandom.newgui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

class PersonalCustomBundleLoader extends ResourceBundle.Control {
    private ResourceBundle replacementBundle;

    public PersonalCustomBundleLoader() {
        // Load the replacement bundle
        try
        {
            replacementBundle = ResourceBundle.getBundle("com.dabomstew.pkrandom.newgui.CustomAlterations");
        }
        catch(Exception e)
        {
            // Locale not found file, etc.
            replacementBundle = null;
        }
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        // Load the original bundle
        ResourceBundle originalBundle = super.newBundle(baseName, locale, format, loader, reload);

        if (originalBundle != null) {
            // Create a custom ResourceBundle that wraps the original bundle
            return new CustomResourceBundle(originalBundle, replacementBundle);
        }

        return null;
    }
}





