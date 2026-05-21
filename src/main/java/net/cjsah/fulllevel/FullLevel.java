package net.cjsah.fulllevel;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class FullLevel implements ModInitializer {
    public static final String MOD_ID = "fulllevel";


    @Override
    public void onInitialize() {
    }

    public static Identifier of(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
