package lintfordpickle.mailtrain.data.projectiles;

import net.lintford.library.core.entities.instances.OpenPooledBaseData;

public class Projectile extends OpenPooledBaseData {

	// --------------------------------------
	// Variables
	// --------------------------------------

	public float sx, sy;
	public float angle;
	public float dist;
	public float life;
	public float maxLife;

	public boolean hasHit;
	public boolean hasRendered; // scanlines are instantaneous, but we need a few ms to render..

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Projectile(int instancePoolUid) {
		super(instancePoolUid);

		reset();
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void init(int ownerId, float sx, float sy, float angle, float dist, float maxLife) {

		this.sx = sx;
		this.sy = sy;
		this.angle = angle;
		this.dist = dist;
		this.life = 0;
		this.maxLife = maxLife;

		this.hasHit = false;
		this.hasRendered = false;

	}

	public void reset() {
		this.sx = 0;
		this.sy = 0;
		this.angle = 0;
		this.dist = 0;
		this.life = 0;
		this.maxLife = 0;

		this.hasHit = false;
		this.hasRendered = false;
	}

}
