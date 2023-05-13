package lintfordpickle.mailtrain.services;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.data.scene.GameSceneHeader;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class GameSceneHeaderIOService {

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public static void saveGameSceneHeader(GameSceneHeader gameSceneHeader, String filename) {
		FileWriter lWriter = null;
		Gson gson = new Gson();
		try {
			lWriter = new FileWriter(filename);
			gson.toJson(gameSceneHeader, lWriter);

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

	public static GameSceneHeader loadGameSceneHeaderFromFile(String filename) {
		final var lGson = new GsonBuilder().create();
		String lWorldSceneryRawFileContents = null;
		GameSceneHeader lGameSceneHeader = null;

		try {
			lWorldSceneryRawFileContents = FileUtils.loadString(filename);
			lGameSceneHeader = lGson.fromJson(lWorldSceneryRawFileContents, GameSceneHeader.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(GameSceneHeaderIOService.class.getSimpleName(), ex);

		}

		return lGameSceneHeader;
	}

}
