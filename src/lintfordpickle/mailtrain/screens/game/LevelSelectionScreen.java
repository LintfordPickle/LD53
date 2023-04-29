package lintfordpickle.mailtrain.screens.game;

import lintfordpickle.mailtrain.data.GameWorldHeader;
import lintfordpickle.mailtrain.screens.editor.TrackList;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.MenuDropDownEntry;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class LevelSelectionScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int BUTTON_START_ID = 0;
	private static final int BUTTON_BACK_ID = 1;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private MenuDropDownEntry<GameWorldHeader> mTrackFilenameEntries;
	private MenuEntry mStartButton;
	private MenuEntry mBackButton;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public LevelSelectionScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		final var lListLayout = new ListLayout(this);

		mTrackFilenameEntries = new MenuDropDownEntry<GameWorldHeader>(screenManager(), this, "Track Filename");
		populateDropDownListWithTrackFilenames(mTrackFilenameEntries);

		mStartButton = new MenuEntry(screenManager(), this, "Start");
		mBackButton = new MenuEntry(screenManager(), this, "Back");

		mStartButton.registerClickListener(this, BUTTON_START_ID);
		mBackButton.registerClickListener(this, BUTTON_BACK_ID);

		lListLayout.addMenuEntry(mTrackFilenameEntries);
		lListLayout.addMenuEntry(mStartButton);
		lListLayout.addMenuEntry(mBackButton);

		addLayout(lListLayout);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void populateDropDownListWithTrackFilenames(MenuDropDownEntry<GameWorldHeader> pEntry) {
		final var lListOfTracks = TrackList.getListWithTrackFilesSortedModified(GameWorldHeader.TRACKS_DIRECTORY);

		final var lFolderName = "res/tracks/";

		final int lTrackCount = lListOfTracks.size();
		for (int i = 0; i < lTrackCount; i++) {
			final var lTrackFile = lListOfTracks.get(i);
			final var lTrackName = lFolderName + lTrackFile.getName();

			final var lNewTrackHeader = new GameWorldHeader(lTrackName, lTrackName);
			final var lNewEntry = pEntry.new MenuEnumEntryItem(lTrackFile.getName(), lNewTrackHeader);
			pEntry.addItem(lNewEntry);

		}
	}

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_START_ID:
			// Validate input
			final var lSelectedTrackHeader = mTrackFilenameEntries.selectedItem();

			if (lSelectedTrackHeader == null) {
				screenManager().toastManager().addMessage("sadsad", "You select a track to play", 1500);
				return;
			}

			final var lTrackHeader = lSelectedTrackHeader.value;

			if (!lTrackHeader.isValidated()) {
				screenManager().toastManager().addMessage("Error", "Trackfile doesn't exist: " + lTrackHeader.trackFilename(), 1500);
				return;
			}

			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), lTrackHeader)));

			break;
		case BUTTON_BACK_ID:
			exitScreen();
			return;
		}
	}
}
