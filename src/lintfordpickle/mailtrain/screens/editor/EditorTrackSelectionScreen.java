package lintfordpickle.mailtrain.screens.editor;

import lintfordpickle.mailtrain.data.GameWorldHeader;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.MenuDropDownEntry;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class EditorTrackSelectionScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int BUTTON_CREATE_ID = 0;
	private static final int BUTTON_LOAD_ID = 1;
	private static final int BUTTON_BACK_ID = 2;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private MenuDropDownEntry<GameWorldHeader> mTrackFilenameEntries;
	private MenuEntry mLoadButton;
	private MenuEntry mCreateButton;
	private MenuEntry mBackButton;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorTrackSelectionScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		final var lListLayout = new ListLayout(this);

		mTrackFilenameEntries = new MenuDropDownEntry<GameWorldHeader>(screenManager(), this, "Track Filename");
		mCreateButton = new MenuEntry(pScreenManager, this, "Create New");
		mLoadButton = new MenuEntry(pScreenManager, this, "Load");
		mBackButton = new MenuEntry(pScreenManager, this, "Back");

		mCreateButton.registerClickListener(this, BUTTON_CREATE_ID);
		mLoadButton.registerClickListener(this, BUTTON_LOAD_ID);
		mBackButton.registerClickListener(this, BUTTON_BACK_ID);

		lListLayout.addMenuEntry(mCreateButton);
		lListLayout.addMenuEntry(mTrackFilenameEntries);
		lListLayout.addMenuEntry(mLoadButton);
		lListLayout.addMenuEntry(mBackButton);

		addLayout(lListLayout);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		populateDropDownListWithTrackFilenames(mTrackFilenameEntries);
	}

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

		if (lTrackCount == 0) {
			mTrackFilenameEntries.enabled(false);
			mLoadButton.enabled(false);
		}

	}

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_CREATE_ID:
			final var lNewTrackHeader = new GameWorldHeader();
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new TrackEditorScreen(screenManager(), lNewTrackHeader)));

			break;
		case BUTTON_LOAD_ID:
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

			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new TrackEditorScreen(screenManager(), lTrackHeader)));
			break;

		case BUTTON_BACK_ID:
			exitScreen();
			return;
		}

	}

}
