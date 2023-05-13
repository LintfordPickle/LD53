package lintfordpickle.mailtrain.data.projectiles;

import net.lintford.library.core.entities.instances.OpenPoolInstanceManager;

public class ProjectileManager extends OpenPoolInstanceManager<Projectile> {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public ProjectileManager() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected Projectile createPoolObjectInstance() {
		return new Projectile(getNewInstanceUID());
	}

}
