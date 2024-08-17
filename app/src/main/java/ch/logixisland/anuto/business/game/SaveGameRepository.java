package ch.logixisland.anuto.business.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.logixisland.anuto.util.container.KeyValueStore;

public class SaveGameRepository {

    private static final String TAG = SaveGameRepository.class.getSimpleName();

    private static final String AUTO_SAVE_STATE_FILE = "autosave.json";

    private static final String GAME_INFO_FILE = "info.json";
    private static final String GAME_STATE_FILE = "state.json";
    private static final String SCREENSHOT_FILE = "screen.png";

    private final Context mContext;
    private final List<SaveGameInfo> mSaveGameInfos;

    public SaveGameRepository(Context context) {
        mContext = context;
        mSaveGameInfos = new ArrayList<>();

        readSaveGameInfos();
    }

    public File getAutoSaveStateFile() {
        return new File(mContext.getFilesDir(), AUTO_SAVE_STATE_FILE);
    }

    public File getGameStateFile(SaveGameInfo saveGameInfo) {
        return new File(saveGameInfo.getFolder(), GAME_STATE_FILE);
    }

    public List<SaveGameInfo> getSaveGameInfos() {
        return Collections.unmodifiableList(mSaveGameInfos);
    }

    public SaveGameInfo createSaveGame(Bitmap screenshot, int score, int wave, int lives) {
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        File folder = new File(mContext.getFilesDir() + File.separator
                + "savegame" + File.separator
                + dateFormat.format(date));

        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();

        try {
            Log.i(TAG, "Saving screenshot...");
            FileOutputStream outputStream = new FileOutputStream(new File(folder, SCREENSHOT_FILE), false);

            int destWidth = 600;
            int origWidth = screenshot.getWidth();

            if (destWidth < origWidth) {
                int origHeight = screenshot.getHeight();

                int destHeight = (int) (((float) origHeight) / (((float) origWidth) / destWidth));
                screenshot = Bitmap.createScaledBitmap(screenshot, destWidth, destHeight, false);
            }

            screenshot.compress(Bitmap.CompressFormat.PNG, 30, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not save screenshot!", e);
        }

        try {
            Log.i(TAG, "Saving game info...");
            KeyValueStore saveGameInfo = new KeyValueStore();
            saveGameInfo.putInt("version", SaveGameMigrator.SAVE_GAME_VERSION);
            saveGameInfo.putDate("date", date);
            saveGameInfo.putInt("score", score);
            saveGameInfo.putInt("wave", wave);
            saveGameInfo.putInt("lives", lives);

            FileOutputStream outputStream = new FileOutputStream(new File(folder, GAME_INFO_FILE), false);
            saveGameInfo.toStream(outputStream);
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not save game info!", e);
        }

        SaveGameInfo saveGameInfo = new SaveGameInfo(folder, date, score, wave, lives, screenshot);
        mSaveGameInfos.add(0, saveGameInfo);
        return saveGameInfo;
    }

    public void deleteSaveGame(SaveGameInfo saveGameInfo) {
        if (!mSaveGameInfos.contains(saveGameInfo)) {
            throw new RuntimeException("Unknown save game!");
        }

        deleteSaveGame(saveGameInfo.getFolder());
        mSaveGameInfos.remove(saveGameInfo);
    }

    private static void deleteSaveGame(File folder) {
        Log.i(TAG, "Deleting save game: " + folder.getAbsolutePath());
        final List<String> files = Arrays.asList(GAME_STATE_FILE, GAME_INFO_FILE, SCREENSHOT_FILE);

        for (String file : files) {
            if (!new File(folder, file).delete()) {
                Log.e(TAG, "Failed to delete file: " + file);
            }
        }

        if (!folder.delete()) {
            Log.e(TAG, "Failed to delete save game: " + folder.getAbsolutePath());
        }
    }

    private void readSaveGameInfos() {
        File rootdir = new File(mContext.getFilesDir() + File.separator
                + "savegame" + File.separator);

        File[] fileArray = rootdir.listFiles();

        if (fileArray == null || fileArray.length == 0) {
            Log.i(TAG, "No save games found.");
            return;
        }

        List<File> fileList = Arrays.asList(fileArray);
        Collections.sort(fileList, Collections.reverseOrder());

        for (File file : fileList) {
            SaveGameInfo saveGameInfo = readSaveGameInfo(file);

            if (saveGameInfo != null) {
                mSaveGameInfos.add(saveGameInfo);
            }
        }
    }

    private static SaveGameInfo readSaveGameInfo(File folder) {
        try {
            Log.i(TAG, "Reading save game:" + folder.getName());
            KeyValueStore gameInfoStore = KeyValueStore.fromStream(new FileInputStream(new File(folder, GAME_INFO_FILE)));

            Date date = gameInfoStore.getDate("date");
            int score = gameInfoStore.getInt("score");
            int wave = gameInfoStore.getInt("wave");
            int lives = gameInfoStore.getInt("lives");

            Bitmap screenshot = BitmapFactory.decodeFile(new File(folder, SCREENSHOT_FILE).getAbsolutePath());

            return new SaveGameInfo(folder, date, score, wave, lives, screenshot);
        } catch (Exception e) {
            Log.w(TAG, "Failed to read save game!");
            return null;
        }
    }
}
