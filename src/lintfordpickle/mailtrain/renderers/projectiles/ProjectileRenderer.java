package lintfordpickle.mailtrain.renderers.projectiles;

import lintfordpickle.mailtrain.controllers.scanline.ScanlineProjectileController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class ProjectileRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Projectile Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private AudioFireAndForgetManager mTrainSoundManager;
	private ScanlineProjectileController mScanlineProjectileController;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 2;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public ProjectileRenderer(RendererManager pRendererManager, int pEntityGroupUid) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupUid);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mScanlineProjectileController = (ScanlineProjectileController) pCore.controllerManager().getControllerByNameRequired(ScanlineProjectileController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		// resourceManager.audioManager().loadAudioFile("SOUND_SIGNAL_CHANGE", "res/sounds/soundSignalChange.wav", false);

		mTrainSoundManager = new AudioFireAndForgetManager(resourceManager.audioManager());
		mTrainSoundManager.acquireAudioSources(2);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mTrainSoundManager.unassign();
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore pCore) {

		final var lTextureBatch = mRendererManager.uiSpriteBatch();

		final var z = -0.01f;

		lTextureBatch.begin(pCore.gameCamera());

//		for (int i = 0; i < lNumLiveScanlines; i++) {
//			final var lScanline = lScanlineIntances.get(i);
//			if (lScanline.hasRendered)
//				continue;
//
//		}

		lTextureBatch.end();

	}
}
