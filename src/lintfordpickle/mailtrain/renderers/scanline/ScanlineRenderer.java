package lintfordpickle.mailtrain.renderers.scanline;

import lintfordpickle.mailtrain.controllers.scanline.ScanlineProjectileController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.renderers.RendererManager;

public class ScanlineRenderer extends ScanlineBatchRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Scanline Projectile Renderer";

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

	public ScanlineRenderer(RendererManager pRendererManager, int pEntityGroupUid) {
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

		final var lScanlineMan = mScanlineProjectileController.scanlineManager();
		final var lScanlineIntances = lScanlineMan.instances();
		final var lNumLiveScanlines = lScanlineIntances.size();

		if (lNumLiveScanlines == 0)
			return;

		begin();

		final var z = -0.01f;
		final var r90 = (float) Math.toRadians(90);
		final var scan_width = .5f;

		for (int i = 0; i < lNumLiveScanlines; i++) {
			final var lScanline = lScanlineIntances.get(i);
			if (lScanline.hasRendered)
				continue;

			final var c = (float) Math.cos(lScanline.angle);
			final var s = (float) Math.sin(lScanline.angle);

			final var pc = (float) Math.cos(lScanline.angle + r90);
			final var ps = (float) Math.sin(lScanline.angle + r90);

			final var dx = lScanline.sx;
			final var dy = lScanline.sy;

			final var sx = lScanline.sx;
			final var sy = lScanline.sy;
			final var sw = mWhiteTexture.getTextureWidth();
			final var sh = mWhiteTexture.getTextureHeight();

			float x0 = dx + pc * -scan_width;
			float y0 = dy + ps * -scan_width;
			float u0 = sx / sw;
			float v0 = (sy + sh) / sh;

			float x1 = dx + c * lScanline.dist + pc * -scan_width;
			float y1 = dy + s * lScanline.dist + ps * -scan_width;
			float u1 = sx / sw;
			float v1 = sy / sh;

			float x2 = dx + c * lScanline.dist + pc * scan_width;
			float y2 = dy + s * lScanline.dist + ps * scan_width;
			float u2 = (sx + sw) / sw;
			float v2 = sy / sh;

			float x3 = dx + pc * scan_width;
			float y3 = dy + ps * scan_width;
			float u3 = (sx + sw) / sw;
			float v3 = (sy + sh) / sh;
			
			final var r = 1.f + RandomNumbers.random(-0.4f, .4f);
			final var g = 1.f + RandomNumbers.random(-0.4f, .4f);
			final var b = 0.f;
			final var a = 1.f - RandomNumbers.random(0.0f, 1.f);

			addVertToBuffer(x0, y0, z, r, g, b, a, u0, v0);
			addVertToBuffer(x1, y1, z, r, g, b, a, u1, v1);
			addVertToBuffer(x2, y2, z, r, g, b, a, u2, v2);
			addVertToBuffer(x3, y3, z, r, g, b, a, u3, v3);

			mIndexCount += NUM_INDICES_PER_SPRITE;
		}

		super.draw(pCore);
	}
}
