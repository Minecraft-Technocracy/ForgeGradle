import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraftforge.gradle.GradleStartCommon;

import java.io.File;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GradleStart extends GradleStartCommon
{
    public static void main(String[] args) throws Throwable
    {
        // hack natives.
        hackNatives();

        // launch
        (new GradleStart()).launch(args);
    }

    @Override
    protected String getBounceClass()
    {
        return "@@BOUNCERCLIENT@@";
    }

    @Override
    protected String getTweakClass()
    {
        return "@@TWEAKERCLIENT@@";
    }

    @Override
    protected void setDefaultArguments(Map<String, String> argMap)
    {
        argMap.put("version",        "@@MCVERSION@@");
        argMap.put("assetIndex",     "@@ASSETINDEX@@");
        argMap.put("assetsDir",      "@@ASSETSDIR@@");
        argMap.put("accessToken",    "FML");
        argMap.put("userProperties", "{}");
        argMap.put("username",        null);
        argMap.put("password",        null);
    }

    @Override
    protected void preLaunch(Map<String, String> argMap, List<String> extras)
    {
        if (!Strings.isNullOrEmpty(argMap.get("password")))
        {
            GradleStartCommon.LOGGER.info("Password found, attempting login");
            attemptLogin(argMap);
        }

        if (!Strings.isNullOrEmpty(argMap.get("assetIndex")))
        {
            //setupAssets(argMap);
        }
    }

    private static void hackNatives()
    {
        String nativesDir = "@@NATIVESDIR@@";

        // old hack was rendered unusable through
        // http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/rev/1d666f78532a/
        // replacement hack:
        try {
            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            //get array of paths
            final String[] paths = (String[])usrPathsField.get(null);

            //check if the path to add is already present
            for(String path : paths) {
                if(path.equals(nativesDir)) {
                    return;
                }
            }

            //add the new path
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[newPaths.length-1] = nativesDir;
            usrPathsField.set(null, newPaths);
        } catch (Throwable t) {
            System.err.println("Error hacking the classloader. Loading platform libraries might not work.");
            System.err.println("See \"https://stackoverflow.com/questions/15409223/adding-new-paths-for-native" +
                    "-libraries-at-runtime-in-java/15409446#15409446\" for help");
            t.printStackTrace(System.err);
        }
    }

    private void attemptLogin(Map<String, String> argMap)
    {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(argMap.get("username"));
        auth.setPassword(argMap.get("password"));
        argMap.put("password", null);

        try {
            auth.logIn();
        }
        catch (AuthenticationException e)
        {
            LOGGER.error("-- Login failed!  " + e.getMessage());
            throw new RuntimeException(e);
        }

        LOGGER.info("Login Succesful!");
        argMap.put("accessToken", auth.getAuthenticatedToken());
        argMap.put("uuid", auth.getSelectedProfile().getId().toString().replace("-", ""));
        argMap.put("username", auth.getSelectedProfile().getName());
        argMap.put("userType", auth.getUserType().getName());

        // 1.8 only apperantly.. -_-
        argMap.put("userProperties", new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
    }
}
