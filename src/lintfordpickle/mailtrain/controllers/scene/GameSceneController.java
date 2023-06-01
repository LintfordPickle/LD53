package lintfordpickle.mailtrain.controllers.scene;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import lintfordpickle.mailtrain.data.scene.GameSceneHeader;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.savedefinitions.GameSceneSaveDefinition;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.storage.FileUtils;

public class GameSceneController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Game Scene Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private boolean mIsGameLoaded;
	private GameSceneInstance mGameScene;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isGameLoaded() {
		return mIsGameLoaded;
	}

	@Override
	public boolean isInitialized() {
		return mGameScene != null;
	}

	public GameSceneInstance gameScene() {
		return mGameScene;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameSceneController(ControllerManager controllerManager, GameWorldHeader gameWorldHeader, GameSceneHeader sceneHeader, GameSceneInstance gameScene, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mIsGameLoaded = false;
		mGameScene = gameScene;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mGameScene.initializeScene();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void createEmptyScene() {
		mGameScene.createNewScene();

		mIsGameLoaded = true;
	}

	public void saveSceneToFile(String filename) {
		final var lSceneDefinition = mGameScene.getSceneSaveDefinition();

		try (Writer writer = new FileWriter(filename)) {
			final var gson = new GsonBuilder().create();
			gson.toJson(lSceneDefinition, writer);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return;
	}

	public void loadGameScene(String filename) {
		final var lGson = new GsonBuilder().create();

		String lTrackRawFileContents = null;
		GameSceneSaveDefinition lTrackDefinition = null;

		try {
			lTrackRawFileContents = FileUtils.loadString(filename);
			lTrackDefinition = lGson.fromJson(lTrackRawFileContents, GameSceneSaveDefinition.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(getClass().getSimpleName(), ex);
		}

		if (lTrackDefinition == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "There was an error reading the scene data from file (" + filename + ")");
			return;
		}

		mGameScene.loadGameScene(lTrackDefinition);
		mGameScene.finalizeAfterLoading();
	}
}
