package be.seeseemelk.mockbukkit.plugin;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.jar.JarFile;

import be.seeseemelk.mockbukkit.ServerMock;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class MockBukkitConfiguredPluginClassLoader extends ClassLoader implements ConfiguredPluginClassLoader {

    private final ServerMock server;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File pluginFile;
    private JarFile jarFile;
    private JavaPlugin plugin;

    public MockBukkitConfiguredPluginClassLoader(ServerMock server, PluginDescriptionFile description, File dataFolder, File pluginFile) {
        super(MockBukkitConfiguredPluginClassLoader.class.getClassLoader());
        this.server = server;
        this.description = description;
        this.dataFolder = dataFolder;
        this.pluginFile = pluginFile;
    }

    public void setJarFile(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public PluginMeta getConfiguration() {
        return description;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                loaded = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve, boolean checkPluginEnabled, boolean checkPackageAccess) throws ClassNotFoundException {
        return loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";

        try (InputStream in = MockBukkitConfiguredPluginClassLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new ClassNotFoundException(name);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            byte[] bytes = out.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException ex) {
            throw new ClassNotFoundException(name, ex);
        }
    }

    public Class<? extends JavaPlugin> loadProxyClass(Class<? extends JavaPlugin> pluginClass) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends JavaPlugin> loaded = (Class<? extends JavaPlugin>) new ByteBuddy()
                .subclass(pluginClass)
                .name(pluginClass.getName() + "$MockBukkitProxy")
                .make()
                .load(this, ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
            return loaded;
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to create plugin proxy class " + pluginClass.getName(), ex);
        }
    }

    @Override
    public void init(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.init(server, description, dataFolder, pluginFile, this, description, java.util.logging.Logger.getLogger(description.getName()));
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public PluginClassLoaderGroup getGroup() {
        return null;
    }

    @Override
    public void close() throws IOException {
        if (jarFile != null) {
            jarFile.close();
        }
    }
}

