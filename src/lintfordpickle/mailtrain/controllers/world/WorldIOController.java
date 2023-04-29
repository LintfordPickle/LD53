package lintfordpickle.mailtrain.controllers.world;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.data.world.WorldScenery;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class WorldIOController {

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public static WorldScenery loadSceneryFromFile(String pFilename) {
		final var lGson = new GsonBuilder().create();
		String lWorldSceneryRawFileContents = null;
		WorldScenery lWorldScenery = null;

		try {
			lWorldSceneryRawFileContents = FileUtils.loadString(pFilename);
			lWorldScenery = lGson.fromJson(lWorldSceneryRawFileContents, WorldScenery.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(WorldIOController.class.getSimpleName(), ex);

		}

		if (lWorldScenery == null) {
			System.out.println("Creating empty scenery file");
			lWorldScenery = new WorldScenery();
		}

		return lWorldScenery;
	}
}
