package lintfordpickle.mailtrain.controllers.world;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.data.world.scenes.SceneHeader;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class SceneIOController {

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public static SceneHeader loadSceneryFromFile(String pFilename) {
		final var lGson = new GsonBuilder().create();
		String lWorldSceneryRawFileContents = null;
		SceneHeader lWorldScenery = null;

		try {
			lWorldSceneryRawFileContents = FileUtils.loadString(pFilename);
			lWorldScenery = lGson.fromJson(lWorldSceneryRawFileContents, SceneHeader.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(SceneIOController.class.getSimpleName(), ex);

		}

		return lWorldScenery;
	}

	public static void saveSceneHeader(SceneHeader sceneHeader, String filename) {
		FileWriter lWriter = null;
		Gson gson = new Gson();
		try {
			lWriter = new FileWriter(filename);
			gson.toJson(sceneHeader, lWriter);

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

}
