package lintfordpickle.mailtrain.controllers.world;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.data.world.scenes.ScenePropList;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class WorldIOController {

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public static ScenePropList loadSceneryFromFile(String pFilename) {
		final var lGson = new GsonBuilder().create();
		String lWorldSceneryRawFileContents = null;
		ScenePropList lWorldScenery = null;

		try {
			lWorldSceneryRawFileContents = FileUtils.loadString(pFilename);
			lWorldScenery = lGson.fromJson(lWorldSceneryRawFileContents, ScenePropList.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(WorldIOController.class.getSimpleName(), ex);

		}

		if (lWorldScenery == null) {
			System.out.println("Creating empty scenery file");
			lWorldScenery = new ScenePropList();
		}

		return lWorldScenery;
	}

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

	public static GameWorldHeader loadGameWorldHeaderFromFile(String pFilename) {
		final var lGson = new GsonBuilder().create();
		String lWorldSceneryRawFileContents = null;
		GameWorldHeader lGameWorldHeader = null;

		try {
			lWorldSceneryRawFileContents = FileUtils.loadString(pFilename);
			lGameWorldHeader = lGson.fromJson(lWorldSceneryRawFileContents, GameWorldHeader.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(WorldIOController.class.getSimpleName(), ex);

		}

		return lGameWorldHeader;
	}

}
