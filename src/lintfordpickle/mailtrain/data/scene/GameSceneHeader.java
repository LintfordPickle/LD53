package lintfordpickle.mailtrain.data.scene;

import java.io.File;
import java.io.FilenameFilter;

/**
 * The {@link GameSceneHeader} can contain the saved data for either a complete scene or a saved game scene (partially played out)
 */
public class GameSceneHeader {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String SCENE_FILE_EXTENSION = ".hdr";
	public static final String LEVEL_FILE_EXTENSION = ".dat";

	public static final FilenameFilter sceneFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File f, String name) {
			return name.endsWith(SCENE_FILE_EXTENSION);
		}
	};

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private String mSceneName;
	private String mSceneFilename;

	private transient boolean mIsValidated;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isValidated() {
		return mIsValidated;
	}

	public String sceneName() {
		return mSceneName;
	}

	public void sceneName(String sceneName) {
		mSceneName = sceneName;
	}

	public String sceneFilename() {
		return mSceneFilename;
	}

	public void sceneFilename(String sceneFilename) {
		mSceneFilename = sceneFilename;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameSceneHeader() {

	}

	public GameSceneHeader(String sceneName, String pSceneFilename) {
		this();

		mSceneName = sceneName;
		mSceneFilename = pSceneFilename;

		validateHeader();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void validateHeader() {
		// TODO: Unimplemented method
	}

}
