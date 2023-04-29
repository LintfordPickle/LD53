package lintfordpickle.mailtrain.data;

import java.io.File;
import java.io.FilenameFilter;

public class GameWorldHeader {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String TRACKS_DIRECTORY = "res/tracks/";
	public static final String TRACK_FILE_EXTENSION = ".json";

	public static final FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File f, String name) {
			return name.endsWith(TRACK_FILE_EXTENSION);
		}
	};

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private String mTrackFilename;
	private String mSceneryFilename;
	
	
	private boolean mIsValidated;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isValidated() {
		return mIsValidated;
	}

	public String trackFilename() {
		return mTrackFilename;
	}

	public void trackFilename(String pNewTrackFilename) {
		mIsValidated = mTrackFilename.contentEquals(pNewTrackFilename);
		mTrackFilename = pNewTrackFilename;

	}

	public String sceneryFilename() {
		return mSceneryFilename;
	}

	public void sceneryFilename(String pNewSceneryFilename) {
		mSceneryFilename = pNewSceneryFilename;

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWorldHeader() {

	}

	public GameWorldHeader(String pTrackFilename, String pSceneryFilename) {
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
