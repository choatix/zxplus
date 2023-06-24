package com.dabomstew.pkrandom.newgui;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class CustomResourceBundle extends ResourceBundle {
    private ResourceBundle originalBundle;
    private ResourceBundle replacementBundle;

    public CustomResourceBundle(ResourceBundle originalBundle, ResourceBundle replacementBundle) {
        this.originalBundle = originalBundle;
        this.replacementBundle = replacementBundle;
    }

    @Override
    protected Object handleGetObject(String key) {
        // Retrieve the replacement word from the replacement bundle

        String baseResult = (String) originalBundle.getObject(key);

        if(replacementBundle != null)
        {
            if(replacementBundle.containsKey(key))
            {
                return replacementBundle.getObject(key);
            }

            Enumeration<String> keys = replacementBundle.getKeys();

            while (keys.hasMoreElements()) {
                String foundKey = keys.nextElement();

                if(foundKey.startsWith("@"))
                {
                    if(baseResult.contains(foundKey.replace("@", "")))
                    {
                        baseResult = baseResult.replace(foundKey.replace("@",""), replacementBundle.getString(foundKey));
                    }
                }
            }
        }

        // If a replacement is found, return it; otherwise, return the original value from the original bundle
        return baseResult;
    }

    @Override
    public Enumeration<String> getKeys() {
        return originalBundle.getKeys();
    }
}