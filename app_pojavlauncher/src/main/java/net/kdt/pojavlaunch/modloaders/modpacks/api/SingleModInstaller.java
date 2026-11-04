package net.kdt.pojavlaunch.modloaders.modpacks.api;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.io.IOException;

/**
 * Downloads one standalone mod (not a modpack) into the "mods" folder
 * of the currently selected profile.
 */
public class SingleModInstaller {
    private SingleModInstaller() {}

    public static void install(ModDetail modDetail, int selectedVersion) throws IOException {
        String url = modDetail.versionUrls[selectedVersion];
        if (url == null) throw new IOException("This file has no public download link");
        String hash = modDetail.versionHashes[selectedVersion];
        String fileName = FileUtils.getFileName(url);

        File modsDir = new File(getCurrentProfileDir(), "mods");
        if (!modsDir.exists() && !modsDir.mkdirs())
            throw new IOException("Cannot create " + modsDir);

        try {
            ModDownloader downloader = new ModDownloader(modsDir, true);
            downloader.submitDownload(() -> new ModDownloader.FileInfo(url, fileName, hash));
            downloader.awaitFinish((c, m) -> ProgressKeeper.submitProgress(
                    ProgressLayout.INSTALL_MODPACK, (int) Math.max((float) c / m * 100, 0),
                    R.string.modpack_download_downloading_mods_fc, c, m));
        } finally {
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
        }
    }

    /** Game directory of the profile that is selected on the launcher main screen */
    public static File getCurrentProfileDir() {
        String current = LauncherPreferences.DEFAULT_PREF.getString(
                LauncherPreferences.PREF_KEY_CURRENT_PROFILE, null);
        if (!Tools.isValidString(current)) return new File(Tools.DIR_GAME_NEW);
        LauncherProfiles.load();
        MinecraftProfile profile = LauncherProfiles.mainProfileJson.profiles.get(current);
        if (profile == null) return new File(Tools.DIR_GAME_NEW);
        return Tools.getGameDirPath(profile);
    }
}
