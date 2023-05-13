package lintfordpickle.mailtrain.screens.game;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.GameState;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.screens.editor.TrackList;
import lintfordpickle.mailtrain.services.GameWorldHeaderIOService;
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
				mStartButton.enabled(true);
			else
				mStartButton.enabled(false);
		}
	}

	private void populateDropDownListWithTrackFilenames(MenuDropDownEntry<GameWorldHeader> pEntry) {
		pEntry.clearItems();
		final var lListOfWorlds = TrackList.getListOfWorldFoldersSortedModified(ConstantsGame.WORLD_DIRECTORY);

		final int lTrackCount = lListOfWorlds.size();
		for (int i = 0; i < lTrackCount; i++) {
			final var lGameWorldHeaderFile = lListOfWorlds.get(i);
			final var lGameWorldHeader = GameWorldHeaderIOService.loadGameWorldHeaderFromFile(lGameWorldHeaderFile.getPath() + FileUtils.FILE_SEPARATOR + GameWorldHeader.WORLD_HEADER_FILE_NAME);
			lGameWorldHeader.worldDirectory(lGameWorldHeaderFile.getPath() + FileUtils.FILE_SEPARATOR);

			final var lNewEntry = pEntry.new MenuEnumEntryItem(lGameWorldHeaderFile.getName(), lGameWorldHeader);
			pEntry.addItem(lNewEntry);
		}

		if (lTrackCount == 0) {
			mStartButton.enabled(false);
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
			final var lSceneHeader = lGameWorldHeader.getStartSceneHeader();
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), new GameState(), lGameWorldHeader, lSceneHeader)));

			break;
		case BUTTON_BACK_ID:
			exitScreen();
			return;
		}
	}
}
