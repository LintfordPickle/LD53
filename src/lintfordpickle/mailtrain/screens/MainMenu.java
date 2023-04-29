package lintfordpickle.mailtrain.screens;

import lintfordpickle.mailtrain.screens.editor.EditorTrackSelectionScreen;
import lintfordpickle.mailtrain.screens.game.LevelSelectionScreen;
import lintfordpickle.mailtrain.screens.menu.HelpScreen;
import lintfordpickle.mailtrain.screens.menu.OptionsScreen;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.ScreenManagerConstants.LAYOUT_ALIGNMENT;
import net.lintford.library.screenmanager.ScreenManagerConstants.LAYOUT_WIDTH;
import net.lintford.library.screenmanager.layouts.ListLayout;

public class MainMenu extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String TITLE = null;

	private static final int SCREEN_BUTTON_PLAY = 11;
	private static final int SCREEN_BUTTON_EDITOR = 20;
	private static final int SCREEN_BUTTON_HELP = 12;
	private static final int SCREEN_BUTTON_OPTIONS = 13;
	private static final int SCREEN_BUTTON_EXIT = 15;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ListLayout mMainMenuListBox;

	private Texture mMenuLogoTexture;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public MainMenu(ScreenManager pScreenManager) {
		super(pScreenManager, TITLE);

		mLayoutAlignment = LAYOUT_ALIGNMENT.LEFT;

		mMainMenuListBox = new ListLayout(this);
		mMainMenuListBox.setDrawBackground(true, ColorConstants.getColor(.7f, .3f, .7f, .5f));
		mMainMenuListBox.layoutWidth(LAYOUT_WIDTH.HALF);
		mMainMenuListBox.layoutFillType(FILLTYPE.TAKE_WHATS_NEEDED);

		final var lPlayEntry = new MenuEntry(mScreenManager, this, "Start Game");
		lPlayEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);
		lPlayEntry.registerClickListener(this, SCREEN_BUTTON_PLAY);
		lPlayEntry.setToolTip("Starts a new game.");

		final var lEditorEntry = new MenuEntry(mScreenManager, this, "Editor");
		lEditorEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);
		lEditorEntry.registerClickListener(this, SCREEN_BUTTON_EDITOR);
		lEditorEntry.setToolTip("Launches the track and world editor");

		final var lHelpButton = new MenuEntry(mScreenManager, this, "Instructions");
		lHelpButton.horizontalFillType(FILLTYPE.FILL_CONTAINER);
		lHelpButton.registerClickListener(this, SCREEN_BUTTON_HELP);

		final var lOptionsEntry = new MenuEntry(mScreenManager, this, "Options");
		lOptionsEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);
		lOptionsEntry.registerClickListener(this, SCREEN_BUTTON_OPTIONS);

		final var lExitEntry = new MenuEntry(mScreenManager, this, "Exit");
		lExitEntry.horizontalFillType(FILLTYPE.FILL_CONTAINER);
		lExitEntry.registerClickListener(this, SCREEN_BUTTON_EXIT);

		mMainMenuListBox.addMenuEntry(lPlayEntry);
		mMainMenuListBox.addMenuEntry(MenuEntry.menuSeparator());
		mMainMenuListBox.addMenuEntry(lEditorEntry);
		mMainMenuListBox.addMenuEntry(MenuEntry.menuSeparator());
		mMainMenuListBox.addMenuEntry(lHelpButton);
		mMainMenuListBox.addMenuEntry(lOptionsEntry);
		mMainMenuListBox.addMenuEntry(MenuEntry.menuSeparator());
		mMainMenuListBox.addMenuEntry(lExitEntry);

		mLayouts.add(mMainMenuListBox);

		mSelectedLayoutIndex = mLayouts.size() - 1;
		mSelectedEntryIndex = 0;

		mScreenPaddingTop = 30.f;
		mLayoutPaddingHorizontal = 50.f;

		mIsPopup = false;
		mShowBackgroundScreens = true;
		mESCBackEnabled = false;

		mScreenManager.contextHintManager().drawContextBackground(true);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mMenuLogoTexture = resourceManager.textureManager().loadTexture("TEXTURE_MENU_LOGO", "res/textures/textureMenuLogo.png", entityGroupUid());
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mMenuLogoTexture = null;
	}

	@Override
	protected void handleOnClick() {

		switch (mClickAction.consume()) {
		case SCREEN_BUTTON_PLAY: {
			screenManager().addScreen(new LevelSelectionScreen(mScreenManager));
			//screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new GameScreen(screenManager())));
			break;
		}

		case SCREEN_BUTTON_EDITOR: {
			screenManager().addScreen(new EditorTrackSelectionScreen(mScreenManager));
			break;
		}

		case SCREEN_BUTTON_OPTIONS: {
			screenManager().addScreen(new OptionsScreen(mScreenManager));
			break;
		}

		case SCREEN_BUTTON_HELP: {
			screenManager().addScreen(new HelpScreen(mScreenManager));
			break;
		}

		case SCREEN_BUTTON_EXIT:
			screenManager().exitGame();
			break;
		}
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);

		final var lCanvasBox = core.gameCamera().boundingRectangle();
		final var lTextureBatch = rendererManager().uiSpriteBatch();

		if (mMenuLogoTexture != null) {
			lTextureBatch.begin(core.gameCamera());

			final float logoWidth = mMenuLogoTexture.getTextureWidth();
			final float logoHeight = mMenuLogoTexture.getTextureHeight();

			lTextureBatch.draw(mMenuLogoTexture, 0, 0, logoWidth, logoHeight, -logoWidth * .5f, lCanvasBox.top() + 5.f, logoWidth, logoHeight, -0.01f, screenColor);
			lTextureBatch.end();
		}
	}
}
