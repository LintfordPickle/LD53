package lintfordpickle.mailtrain.screens.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.scene.GameSceneHeader;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import lintfordpickle.mailtrain.screens.dialogs.CreateWorldDialog;
import lintfordpickle.mailtrain.services.GameSceneHeaderIOService;
import lintfordpickle.mailtrain.services.GameWorldHeaderIOService;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.storage.FileUtils;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.HorizontalEntryGroup;
import net.lintford.library.screenmanager.entries.MenuDropDownEntry;
import net.lintford.library.screenmanager.entries.MenuLabelEntry;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class EditorTrackSelectionScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int BUTTON_WORLD_CREATE_ID = 10;
	private static final int BUTTON_WORLD_DELETE_ID = 11;

	private static final int BUTTON_SCENE_CREATE_ID = 20;
	private static final int BUTTON_SCENE_LOAD_ID = 21;
	private static final int BUTTON_SCENE_DELETE_ID = 22;

	private static final int BUTTON_BACK_ID = 50;

	private static final int DROPDOWN_LIST_WORLD = 100;
	private static final int DROPDOWN_LIST_SCENES = 101;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private MenuDropDownEntry<GameWorldHeader> mWorldEntries;
	private MenuDropDownEntry<GameSceneHeader> mSceneEntries;

	private MenuEntry mNewWorldButton;
	private MenuEntry mDeleteWorldButton;

	private MenuEntry mNewSceneButton;
	private MenuEntry mLoadSceneButton;
	private MenuEntry mDeleteSceneButton;

	private MenuEntry mBackButton;

	private CreateWorldDialog mCreateWorldDialog;

	private GameWorldHeader mSelectedGameWorldHeader;
	private GameSceneHeader mSelectedSceneHeader;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorTrackSelectionScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		final var lListLayout = new ListLayout(this);

		mWorldEntries = new MenuDropDownEntry<GameWorldHeader>(screenManager(), this);
		mWorldEntries.registerClickListener(this, DROPDOWN_LIST_WORLD);
		mWorldEntries.noItemsFoundText("No Worlds Available");

		mSceneEntries = new MenuDropDownEntry<GameSceneHeader>(screenManager(), this);
		mSceneEntries.registerClickListener(this, DROPDOWN_LIST_SCENES);
		mSceneEntries.noItemsFoundText("No Scenes Available");

		mNewWorldButton = new MenuEntry(pScreenManager, this, "New World");
		mDeleteWorldButton = new MenuEntry(pScreenManager, this, "Delete World");

		var worldLabelEntry = new MenuLabelEntry(pScreenManager, this);
		worldLabelEntry.label("Worlds");
		worldLabelEntry.drawButtonBackground(true);

		var worldHorizontalEntrySelection = new HorizontalEntryGroup(pScreenManager, this);

		mDeleteSceneButton = new MenuEntry(pScreenManager, this, "Delete Scene");
		mLoadSceneButton = new MenuEntry(pScreenManager, this, "Load Scene");
		mNewSceneButton = new MenuEntry(pScreenManager, this, "New Scene");

		var sceneHorizontalEntrySelection = new HorizontalEntryGroup(pScreenManager, this);

		mBackButton = new MenuEntry(pScreenManager, this, "Back");

		mDeleteWorldButton.registerClickListener(this, BUTTON_WORLD_DELETE_ID);
		mNewWorldButton.registerClickListener(this, BUTTON_WORLD_CREATE_ID);

		mDeleteSceneButton.registerClickListener(this, BUTTON_SCENE_DELETE_ID);
		mLoadSceneButton.registerClickListener(this, BUTTON_SCENE_LOAD_ID);
		mNewSceneButton.registerClickListener(this, BUTTON_SCENE_CREATE_ID);

		mBackButton.registerClickListener(this, BUTTON_BACK_ID);

		lListLayout.addMenuEntry(worldLabelEntry);
		lListLayout.addMenuEntry(mWorldEntries);

		worldHorizontalEntrySelection.addEntry(mDeleteWorldButton);
		worldHorizontalEntrySelection.addEntry(mNewWorldButton);
		lListLayout.addMenuEntry(worldHorizontalEntrySelection);

		lListLayout.addMenuEntry(MenuEntry.menuSeparator());

		var seriesLabelEntry = new MenuLabelEntry(pScreenManager, this);
		seriesLabelEntry.label("Scenes");
		seriesLabelEntry.drawButtonBackground(true);

		lListLayout.addMenuEntry(seriesLabelEntry);
		lListLayout.addMenuEntry(mSceneEntries);

		sceneHorizontalEntrySelection.addEntry(mDeleteSceneButton);
		sceneHorizontalEntrySelection.addEntry(mLoadSceneButton);
		lListLayout.addMenuEntry(mNewSceneButton);
		lListLayout.addMenuEntry(sceneHorizontalEntrySelection);

		lListLayout.addMenuEntry(MenuEntry.menuSeparator());

		lListLayout.addMenuEntry(mBackButton);

		addLayout(lListLayout);

		mCreateWorldDialog = new CreateWorldDialog(pScreenManager, this);
		mCreateWorldDialog.confirmEntry().registerClickListener(this, CreateWorldDialog.CREATE_WORLD_BUTTON_CONFIRM_YES);
		mCreateWorldDialog.cancelEntry().registerClickListener(this, CreateWorldDialog.CREATE_WORLD_BUTTON_CONFIRM_NO);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		populateWorldDropDownListWithTrackFilenames(mWorldEntries);
	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void populateWorldDropDownListWithTrackFilenames(MenuDropDownEntry<GameWorldHeader> pEntry) {
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
			mWorldEntries.enabled(false);
			mSceneEntries.enabled(false);

			mSelectedGameWorldHeader = null;
		} else {
			mSelectedGameWorldHeader = pEntry.items().get(0).value;
			populateSceneDropDownListWithScenes(mSceneEntries, mSelectedGameWorldHeader);
		}
	}

	private void populateSceneDropDownListWithScenes(MenuDropDownEntry<GameSceneHeader> pEntry, GameWorldHeader gameWorldHeader) {
		pEntry.clearItems();
		final var lListOfSceneFiles = TrackList.getListOfSceneFiles(gameWorldHeader.worldDirectory() + FileUtils.FILE_SEPARATOR + ConstantsGame.SCENES_REL_DIRECTORY);

		final int lSceneFileCount = lListOfSceneFiles.size();
		for (int i = 0; i < lSceneFileCount; i++) {
			final var lSceneFile = lListOfSceneFiles.get(i);
			final var lSceneHeader = GameSceneHeaderIOService.loadGameSceneHeaderFromFile(lSceneFile.getPath());

			gameWorldHeader.sceneHeaders().add(lSceneHeader);
			
			final var lNewEntry = pEntry.new MenuEnumEntryItem(lSceneFile.getName(), lSceneHeader);
			pEntry.addItem(lNewEntry);
		}

		if (lSceneFileCount == 0) {
			mSelectedSceneHeader = null;
		} else {
			mSelectedSceneHeader = pEntry.items().get(0).value;
		}

	}

	@Override
	public void onMenuEntryChanged(MenuEntry menuEntry) {
		super.onMenuEntryChanged(menuEntry);

		if (menuEntry == mWorldEntries) {
			final var lSelectedGameWorld = mWorldEntries.selectedItem().value;
			mSelectedGameWorldHeader = lSelectedGameWorld;

			mSceneEntries.clearItems();

			populateSceneDropDownListWithScenes(mSceneEntries, lSelectedGameWorld);

			return;
		}

		if (menuEntry == mSceneEntries) {
			final var lSelectedSceneHeader = mSceneEntries.selectedItem().value;
			mSelectedSceneHeader = lSelectedSceneHeader;
		}

	}

	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {

		case BUTTON_WORLD_CREATE_ID:
			screenManager().addScreen(mCreateWorldDialog);
			break;

		case BUTTON_WORLD_DELETE_ID:
			if (mSelectedGameWorldHeader != null) {
				final var lWorldDirectory = new File(mSelectedGameWorldHeader.worldDirectory());
				if (lWorldDirectory.exists()) {
					try {
						deleteFolder(lWorldDirectory);
					} catch (IOException e) {
						e.printStackTrace();
					}

					mSelectedGameWorldHeader = null;

					populateWorldDropDownListWithTrackFilenames(mWorldEntries);
				}
			}
			break;

		case BUTTON_SCENE_CREATE_ID:
			createNewSceneHeader();
			break;

		case BUTTON_SCENE_LOAD_ID:
			final var lSelectedSceneEntry = mSceneEntries.selectedItem();
			if(lSelectedSceneEntry == null)
				return;
			
			mSelectedSceneHeader.sceneFilename(lSelectedSceneEntry.value.sceneFilename());
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new TrackEditorScreen(screenManager(), mSelectedGameWorldHeader, mSelectedSceneHeader)));
			break;

		case BUTTON_SCENE_DELETE_ID:
			if (mSelectedSceneHeader != null) {
				// TODO: Need to delete all supporting files from this scene (track, scenery etc.)
			}
			break;

		case BUTTON_BACK_ID:
			exitScreen();
			return;

		// --- Dialogs

		case CreateWorldDialog.CREATE_WORLD_BUTTON_CONFIRM_NO:
			if (mScreenManager.getTopScreen() instanceof CreateWorldDialog) {
				mScreenManager.removeScreen(mCreateWorldDialog);
			}
			break;

		case CreateWorldDialog.CREATE_WORLD_BUTTON_CONFIRM_YES:
			createNewGameWorldHeader();

			populateWorldDropDownListWithTrackFilenames(mWorldEntries);

			break;
		}

	}

	private void createNewGameWorldHeader() {
		if (mScreenManager.getTopScreen() instanceof CreateWorldDialog) {
			mScreenManager.removeScreen(mCreateWorldDialog);
		}

		final var lNewWorldName = mCreateWorldDialog.worldName();

		var lNewGameWorldHeader = new GameWorldHeader();
		lNewGameWorldHeader.worldName(lNewWorldName);

		final var lWorldDirectory = ConstantsGame.WORLD_DIRECTORY + lNewWorldName + FileUtils.FILE_SEPARATOR;

		var dir = new File(lWorldDirectory);
		if (!dir.exists())
			dir.mkdirs();

		GameWorldHeaderIOService.saveGameWorldHeader(lNewGameWorldHeader, lWorldDirectory + GameWorldHeader.WORLD_HEADER_FILE_NAME);
	}

	private void createNewSceneHeader() {
		if (mSelectedGameWorldHeader == null) {
			return;
		}

		final var lNewSceneHeader = new GameSceneHeader();
		screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new TrackEditorScreen(screenManager(), mSelectedGameWorldHeader, lNewSceneHeader)));
	}

	private void deleteFolder(File fileToDelete) throws IOException {
		if (fileToDelete.isDirectory()) {
			for (File lFile : fileToDelete.listFiles())
				deleteFolder(lFile);
		}

		if (!fileToDelete.delete())
			throw new FileNotFoundException("Failed to delete file: " + fileToDelete);
	}

}
