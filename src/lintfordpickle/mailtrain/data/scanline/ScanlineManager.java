package lintfordpickle.mailtrain.data.scanline;

import net.lintford.library.core.entities.instances.OpenPoolInstanceManager;

public class ScanlineManager extends OpenPoolInstanceManager<ScanlineProjectile> {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public ScanlineManager() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected ScanlineProjectile createPoolObjectInstance() {
		return new ScanlineProjectile(getNewInstanceUID());
	}

}
