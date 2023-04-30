package lintfordpickle.mailtrain.screens.game;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.tracks.TrackIOController;
import lintfordpickle.mailtrain.controllers.world.SceneIOController;
import lintfordpickle.mailtrain.controllers.world.WorldIOController;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.screens.editor.TrackList;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.storage.FileUtils;
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

	private static final int BUTTON_START_ID = 10;
	private static final int BUTTON_BACK_ID = 11;

	private static final int DROPDOWN_LIST_WORLD = 100;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private MenuDropDownEntry<GameWorldHeader> mTrackFilenameEntries;
	// TODO: Scene selection (DEBUG)?

	private MenuEntry mStartButton;
	private MenuEntry mBackButton;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public LevelSelectionScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		final var lListLayout = new ListLayout(this);

		mTrackFilenameEntries = new MenuDropDownEntry<GameWorldHeader>(screenManager(), this);
		populateDropDownListWithTrackFilenames(mTrackFilenameEntries);

		mStartButton = new MenuEntry(screenManager(), this, "Start");
		mBackButton = new MenuEntry(screenManager(), this, "Back");

		mStartButton.registerClickListener(this, BUTTON_START_ID);
		mBackButton.registerClickListener(this, BUTTON_BACK_ID);

		mTrackFilenameEntries.registerClickListener(this, DROPDOWN_LIST_WORLD);

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

	@Override
	public void onMenuEntryChanged(MenuEntry menuEntry) {
		super.onMenuEntryChanged(menuEntry);

		if (menuEntry == mTrackFilenameEntries) {
			final var lSelectedGameWorldHeader = mTrackFilenameEntries.selectedItem().value;
			if (lSelectedGameWorldHeader != null)
				populateGameWorldSceneEntryPoints(lSelectedGameWorldHeader);
		}

	}

	private void populateDropDownListWithTrackFilenames(MenuDropDownEntry<GameWorldHeader> pEntry) {
		pEntry.clearItems();
		final var lListOfWorlds = TrackList.getListOfWorldFoldersSortedModified(ConstantsGame.WORLD_DIRECTORY);

		final int lTrackCount = lListOfWorlds.size();
		for (int i = 0; i < lTrackCount; i++) {
			final var lGameWorldHeaderFile = lListOfWorlds.get(i);
			final var lGameWorldHeader = WorldIOController.loadGameWorldHeaderFromFile(lGameWorldHeaderFile.getPath() + FileUtils.FILE_SEPARATOR + GameWorldHeader.WORLD_HEADER_FILE_NAME);
			lGameWorldHeader.worldDirectory(lGameWorldHeaderFile.getPath() + FileUtils.FILE_SEPARATOR);

			final var lNewEntry = pEntry.new MenuEnumEntryItem(lGameWorldHeaderFile.getName(), lGameWorldHeader);
			pEntry.addItem(lNewEntry);
		}

		if (lTrackCount == 0) {
			mStartButton.enabled(false);
		} else {
			final var lSelectedGameWorldHeader = mTrackFilenameEntries.selectedItem().value;
			if (lSelectedGameWorldHeader != null)
				populateGameWorldSceneEntryPoints(lSelectedGameWorldHeader);
		}
	}

	// TODO: same code in EditorTrackSelectionScreen code - refactor
	private void populateGameWorldSceneEntryPoints(GameWorldHeader gameWorldHeader) {
		final var lScenesFolder = gameWorldHeader.worldDirectory() + FileUtils.FILE_SEPARATOR + ConstantsGame.SCENES_REL_DIRECTORY;
		final var lListOfSceneFiles = TrackList.getListOfSceneFiles(lScenesFolder);

		final int lSceneFileCount = lListOfSceneFiles.size();
		for (int i = 0; i < lSceneFileCount; i++) {
			final var lSceneFile = lListOfSceneFiles.get(i);
			final var lSceneHeader = SceneIOController.loadSceneryFromFile(lSceneFile.getPath());

			if (lSceneHeader != null) {
				// Load the track so we can poll for entry points
				final var lTrackFilename = lSceneFile.getParentFile() + FileUtils.FILE_SEPARATOR + lSceneHeader.trackFilename();
				final var lTrack = TrackIOController.loadTrackFromFile(lTrackFilename);

				final int lNumEdgesInTrack = lTrack.edges().size();
				for (int j = 0; j < lNumEdgesInTrack; j++) {
					final var lEdge = lTrack.edges().get(j);
					if (lEdge.segmentName == null)
						continue;

					lSceneHeader.addEntryPointName(lEdge.segmentName);
					gameWorldHeader.addSceneHeaderEntryPoint(lEdge.segmentName, lSceneHeader);
				}
			}
		}
	}

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_START_ID:
			final var lGameWorldHeaderEntry = mTrackFilenameEntries.selectedItem();
			if (lGameWorldHeaderEntry == null) {
				screenManager().toastManager().addMessage("Error", "Select a track to play", 1500);
				return;
			}

			final var lGameWorldHeader = lGameWorldHeaderEntry.value;
			if (!lGameWorldHeader.isValidated()) {
				// TODO: GameWorldValidation?
			}

			final var lSceneHeader = lGameWorldHeader.getStartSceneHeader();
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), lGameWorldHeader, lSceneHeader)));

			break;
		case BUTTON_BACK_ID:
			exitScreen();
			return;
		}
	}
}
