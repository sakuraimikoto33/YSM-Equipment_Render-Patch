package net.okitsu.ysmequipmentrenderpatch.launch;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.okitsu.ysmequipmentrenderpatch.YsmEquipmentRenderPatch;
import net.okitsu.ysmequipmentrenderpatch.runtime.YsmRuntimeSymbolCache;
import net.okitsu.ysmequipmentrenderpatch.runtime.YsmRuntimeSymbols;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class YsmSymbolDetectionService implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger(YsmEquipmentRenderPatch.MOD_ID);

    private Path gameDirectory = Path.of(".").toAbsolutePath().normalize();
    private YsmRuntimeSymbols symbols;

    @Override
    public String name() {
        return YsmEquipmentRenderPatch.MOD_ID;
    }

    @Override
    public void initialize(IEnvironment environment) {
        this.gameDirectory = environment.getProperty(IEnvironment.Keys.GAMEDIR.get())
                .orElseGet(() -> Path.of(".").toAbsolutePath().normalize());
        this.symbols = resolveSymbols();
    }

    @Override
    public void onLoad(IEnvironment environment, Set<String> otherServices) {
    }

    @Override
    public List<ITransformer> transformers() {
        if (this.symbols == null || !this.symbols.isComplete()) {
            LOGGER.warn("Yes Steve Model runtime symbols were not detected. YSM Equipment Render Patch transformers are disabled.");
            return List.of();
        }
        return List.of(new YsmEquipmentTransformer(this.symbols));
    }

    private YsmRuntimeSymbols resolveSymbols() {
        Optional<Path> ysmJar = YsmJarLocator.locate(this.gameDirectory);
        if (ysmJar.isEmpty()) {
            LOGGER.warn("Could not locate the Yes Steve Model jar. YSM Equipment Render Patch transformers are disabled.");
            return null;
        }

        Path jarPath = ysmJar.get();
        try {
            LOGGER.info("Using Yes Steve Model jar for runtime symbol detection: {}", jarPath);
            String ysmVersion = YsmJarLocator.readYsmVersion(jarPath).orElse("unknown");
            String jarSha256 = YsmRuntimeSymbolCache.sha256(jarPath);
            Path cachePath = YsmRuntimeSymbolCache.cachePath(this.gameDirectory);
            Optional<YsmRuntimeSymbols> cachedSymbols = YsmRuntimeSymbolCache.read(cachePath)
                    .filter(symbols -> symbols.matches(ysmVersion, jarSha256));
            if (cachedSymbols.isPresent()) {
                LOGGER.info("Loaded Yes Steve Model runtime symbols from cache for {}.", ysmVersion);
                return cachedSymbols.get();
            }

            YsmRuntimeSymbols analyzedSymbols = YsmSymbolAnalyzer.analyze(jarPath, ysmVersion, jarSha256);
            YsmRuntimeSymbolCache.write(cachePath, analyzedSymbols);
            LOGGER.info("Detected and cached Yes Steve Model runtime symbols for {}.", ysmVersion);
            return analyzedSymbols;
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Failed to analyze Yes Steve Model runtime symbols. YSM Equipment Render Patch transformers are disabled.", exception);
            return null;
        }
    }
}
