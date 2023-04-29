package lintfordpickle.mailtrain.renderers;

import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.fonts.FontUnit;
import net.lintford.library.core.graphics.linebatch.LineBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrackSignalRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Debug Constants
	// ---------------------------------------------

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Track Signal Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrackController mTrackController;

	private FontUnit mGameTextFont;
	private LineBatch mLineBatch;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 11;
	}

	@Override
	public boolean isInitialized() {
		return mTrackController != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackSignalRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mLineBatch = new LineBatch();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mGameTextFont = pResourceManager.fontManager().getFontUnit("FONT_GAME_TEXT");

		mLineBatch.loadResources(pResourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mLineBatch.unloadResources();
		mGameTextFont = null;
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackController.isInitialized())
			return;

		drawSignalBlocks(pCore);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawSignalBlocks(LintfordCore pCore) {
//		final var lTrack = mTrackController.track();
//		final var lTextureBatch = mRendererManager.uiTextureBatch();
	}

}
