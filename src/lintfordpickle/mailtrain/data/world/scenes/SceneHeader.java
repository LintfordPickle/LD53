package lintfordpickle.mailtrain.data.world.scenes;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.debug.Debug;

public class SceneHeader {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String SCENE_FILE_EXTENSION = ".json";
	public static final String TRACK_FILE_EXTENSION = ".trk";

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

	private String mTrackFilename;
	private String mSceneryFilename;

	private String startEntryPointName;
	private List<String> mEntryPointNames;

	private transient boolean mIsValidated;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public String startEntryPointName() {
		return startEntryPointName;
	}

	public void startEntryPointName(String newStartEntryPoint) {
		if (newStartEntryPoint == null) {
			startEntryPointName = null;
			return;
		}

		if (mEntryPointNames.contains(newStartEntryPoint) == false) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "Cannot set scene entry point - entry point doesn't exist in scene: " + newStartEntryPoint);
			return;
		}

		startEntryPointName = newStartEntryPoint;
	}

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

	public String trackFilename() {
		return mTrackFilename;
	}

	public void trackFilename(String pNewTrackFilename) {
		mTrackFilename = pNewTrackFilename;
	}

	public String sceneryFilename() {
		return mSceneryFilename;
	}

	public void sceneryFilename(String pNewSceneryFilename) {
		mSceneryFilename = pNewSceneryFilename;

	}

	public void addEntryPointName(String name) {
		if (mEntryPointNames.contains(name) == false)
			mEntryPointNames.add(name);
	}

	public List<String> entryPointNames() {
		return mEntryPointNames;
	}

	public boolean containsEntryPoint(String name) {
		final int lNumEntries = mEntryPointNames.size();
		for (int i = 0; i < lNumEntries; i++) {
			if (mEntryPointNames.get(i).equals(name))
				return true;
		}
		return false;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public SceneHeader() {
		mEntryPointNames = new ArrayList<>();
	}

	public SceneHeader(String pTrackFilename, String pSceneryFilename) {
		this();

		mTrackFilename = pTrackFilename;
		mTrackFilename = pTrackFilename;

		validateHeader();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void validateHeader() {

		final var lFile = new File(mTrackFilename);

		// TODO: Actually validate the file maybe?

		mIsValidated = lFile.exists();

	}

}
