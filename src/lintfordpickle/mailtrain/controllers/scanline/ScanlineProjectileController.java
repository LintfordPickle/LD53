package lintfordpickle.mailtrain.controllers.scanline;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scanline.ScanlineManager;
import lintfordpickle.mailtrain.data.scanline.ScanlineProjectile;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class ScanlineProjectileController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Scanline Projectile Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ScanlineManager mScanlineManager;
	private List<ScanlineProjectile> mUpdateList = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ScanlineManager scanlineManager() {
		return mScanlineManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public ScanlineProjectileController(ControllerManager controllerManager, ScanlineManager scanlineMangeer, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mScanlineManager = scanlineMangeer;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		final var lDelta = (float) core.gameTime().elapsedTimeMilli();

		final var lInstances = mScanlineManager.instances();
		final var lNumInstances = lInstances.size();

		mUpdateList.clear();
		mUpdateList.addAll(lInstances);

		for (int i = 0; i < lNumInstances; i++) {
			final var s = mUpdateList.get(i);
			s.life += lDelta;

			if (s.life >= s.maxLife) {
				mScanlineManager.returnPooledItem(s);
				continue;
			}

			if (s.hasHit == false) {
				// TODO: perform hit check
				s.hasHit = true;
			}
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void shootScanline(int ownerId, float sx, float sy, float angle, float dist) {
		final var s = mScanlineManager.getFreePooledItem();

		s.init(ownerId, sx, sy, angle, dist, ScanlineProjectile.SCANLINE_LIFE);
	}

}