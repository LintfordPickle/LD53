package lintfordpickle.mailtrain.data.world;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.scene.GameSceneHeader;
import net.lintford.library.core.debug.Debug;

public class GameWorldHeader {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String WORLD_HEADER_FILE_EXTENSION = ".json";
	public static final String WORLD_HEADER_FILE_NAME = "worldheader" + WORLD_HEADER_FILE_EXTENSION;

	public static final FilenameFilter gameWorldHeaderFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File f, String name) {
			return name.endsWith(WORLD_HEADER_FILE_EXTENSION);
		}
	};

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private String mWorldName;
	private String mWorldDirectory;

	private transient final List<GameSceneHeader> mSceneHeaders = new ArrayList<>();
	private transient final Map<String, GameSceneHeader> mSceneEntryPointNames = new HashMap<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public String worldDirectory() {
		return mWorldDirectory;
	}

	public void worldDirectory(String newWorldDirectory) {
		mWorldDirectory = newWorldDirectory;
	}

	public String worldName() {
		return mWorldName;
	}

	public void worldName(String pNewSceneryFilename) {
		mWorldName = pNewSceneryFilename;

	}

	public GameSceneHeader getSceneByName(String sceneName) {
		final int lNumScenes = mSceneHeaders.size();
		for (int i = 0; i < lNumScenes; i++) {
			final var lScene = mSceneHeaders.get(i);
			if (lScene.sceneName() == null)
				continue;

			if (lScene.sceneName().equals(sceneName))
				return lScene;

		}
		return null;
	}

	public GameSceneHeader getSceneByEntryPoint(String entryPoint) {
		return mSceneEntryPointNames.get(entryPoint);
	}

	public void addSceneHeaderEntryPoint(String entryPointName, GameSceneHeader sceneHeader) {
		mSceneEntryPointNames.put(entryPointName, sceneHeader);

		if (mSceneHeaders.contains(sceneHeader) == false)
			mSceneHeaders.add(sceneHeader);

	}
	
	public void addSceneHeader(GameSceneHeader sceneHeader) {
		if (mSceneHeaders.contains(sceneHeader) == false)
			mSceneHeaders.add(sceneHeader);
	}

	public List<GameSceneHeader> sceneHeaders() {
		return mSceneHeaders;
	}

	public Map<String, GameSceneHeader> entryPointNames() {
		return mSceneEntryPointNames;
	}

	public boolean containsEntryPoint(String name) {
		return mSceneEntryPointNames.containsKey(name);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWorldHeader() {
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void clearSceneInformation() {
		mSceneHeaders.clear();
		mSceneEntryPointNames.clear();
	}

	public String getSceneHeaderFilepath(GameSceneHeader sceneHeader) {
		return worldDirectory() + ConstantsGame.SCENES_REL_DIRECTORY + sceneHeader.sceneFilename();
	}

	public GameSceneHeader getStartSceneHeader() {
		var lStartScene = entryPointNames().get("GAME_SPAWN");

		if (lStartScene == null) {
			Debug.debugManager().logger().w(getClass().getSimpleName(), "World doesn't contain any edges named 'GAME_SPAWN'");
			lStartScene = mSceneHeaders.get(0);
		}

		if (lStartScene == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "World doesn't contain any scenes");
		}

		return lStartScene;
	}

	public boolean isValid() {
		// TODO: Validate the GameWorldHeader
		return true;
	}

}
