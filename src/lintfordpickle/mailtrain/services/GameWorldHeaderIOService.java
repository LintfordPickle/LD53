package lintfordpickle.mailtrain.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.screens.editor.TrackList;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class GameWorldHeaderIOService {

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public static void saveGameWorldHeader(GameWorldHeader gameWorldHeader, String filename) {
		FileWriter lWriter = null;
		Gson gson = new Gson();
		try {
			lWriter = new FileWriter(filename);
			gson.toJson(gameWorldHeader, lWriter);

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lWriter.flush();
				lWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static GameWorldHeader loadGameWorldHeaderFromFile(String filename) {
		final var lGson = new GsonBuilder().create();
		String lWorldSceneryRawFileContents = null;
		GameWorldHeader lGameWorldHeader = null;

		var lWorldHeaderFile = new File(filename);
		if (lWorldHeaderFile.exists() == false) {
			Debug.debugManager().logger().e(GameWorldHeader.class.getSimpleName(), "GameWorldHeader file doesn't exist");
			return null;
		}

		try {
			lWorldSceneryRawFileContents = FileUtils.loadString(filename);
			lGameWorldHeader = lGson.fromJson(lWorldSceneryRawFileContents, GameWorldHeader.class);
			lGameWorldHeader.worldDirectory(lWorldHeaderFile.getParent());
			if (lGameWorldHeader != null)
				populateWorldHeaderWithScenes(lGameWorldHeader);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(GameWorldHeaderIOService.class.getSimpleName(), ex);

		}

		return lGameWorldHeader;
	}

	private static void populateWorldHeaderWithScenes(GameWorldHeader gameWorldHeader) {
		final var lWorldDir = gameWorldHeader.worldDirectory();
		final var lSceneFiles = TrackList.getListOfSceneFiles(lWorldDir + FileUtils.FILE_SEPARATOR + ConstantsGame.SCENES_REL_DIRECTORY);

		final int lSceneCount = lSceneFiles.size();
		for (int i = 0; i < lSceneCount; i++) {
			final var lSceneFilename = lSceneFiles.get(i);
			final var lSceneHeader = GameSceneHeaderIOService.loadGameSceneHeaderFromFile(lSceneFilename.getPath());

			gameWorldHeader.addSceneHeader(lSceneHeader);
		}
	}

}
