package lintfordpickle.mailtrain.controllers.core;

import lintfordpickle.mailtrain.ConstantsGame;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.debug.Debug;

public class GameCameraZoomController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "NodeBoundedZoomController";

	/**
	 * Specifies the amount of DRAG to be applied to the zoom factor velocity over time.
	 */
	public static final float ZOOM_VELOCITY_DRAG = 0.967f;

	/**
	 * A coefficient for the speed of the zoom (modifies the mouse scroll wheel speed)
	 */
	public static final float ZOOM_ACCELERATE_AMOUNT = 10.0f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	/** The associated {@link Camera} object this controller should control. */
	private ICamera mCamera;

	/** tracks the state of the velocity and accelerate over time. */
	private float mZoomAcceleration;

	/** tracks the state of the velocity and accelerate over time. */
	private float mZoomVelocity;

	/** Flag to enable/disable zoom control in this controller. */
	private boolean mAllowZoom = true;

	private int mMinimumNumberPixelsWide;
	private int mMaximumNumberPixelsWide;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	/**
	 * Sets the {@link Camera} object this controller works with. If null, the controller will skip its update calls.
	 */
	public void setCamera(Camera pCamera) {
		this.mCamera = pCamera;
	}

	public void setZoomConstraints(int pMinNumberPixelsWide, int pMaxNumberPixelsWide) {
		mMinimumNumberPixelsWide = pMinNumberPixelsWide;
		mMaximumNumberPixelsWide = pMaxNumberPixelsWide;
	}

	public float zoomFactor() {
		return mCamera.getZoomFactor();
	}

	public void zoomFactor(float pNewValue) {
		mCamera.setZoomFactor(pNewValue);
	}

	@Override
	public boolean isInitialized() {
		return mCamera != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameCameraZoomController(ControllerManager pControllerManager, ICamera pCamera, int pControllerBaseGroup) {
		super(pControllerManager, CONTROLLER_NAME, pControllerBaseGroup);

		mCamera = pCamera;
		// this.mCameraMinZoom = mCameraMaxZoom = 1.0f;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
	}

	@Override
	public void unloadController() {
		super.unloadController();

		mCamera = null;
	}

	/**
	 * Listens to mouse scroll wheel input and updates the zoom of the associated {@link Camera} if available.
	 */
	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (mCamera == null)
			return false;

		// static zoom factor
		if (mAllowZoom && pCore.input().mouse().tryAcquireMouseOverThisComponent(hashCode())) {
			mZoomAcceleration += pCore.input().mouse().mouseWheelYOffset() * mCamera.getZoomFactor();

		}
		return super.handleInput(pCore);
	}

	/**
	 * Controls the zoom factor of the associated {@link Camera} object, if present and applicable.
	 */
	@Override
	public void update(LintfordCore pCore) {
		if (this.mCamera == null)
			return;

		final float lDeltaTime = (float) pCore.appTime().elapsedTimeSeconds();
		float lZoomFactor = mCamera.getZoomFactor();

		// apply zoom //
		mZoomVelocity += mZoomAcceleration;
		lZoomFactor += mZoomVelocity * lDeltaTime;
		mZoomVelocity *= 0.85f;
		mZoomAcceleration = 0.0f;

		final float lCameraWidth = mCamera.windowWidth();
		if (mMinimumNumberPixelsWide == 0) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "CameraZoomController not initialized properly (bounds not set)");
			mMinimumNumberPixelsWide = 800;
			return;

		}
		float lZoomWidthMax = lCameraWidth / mMinimumNumberPixelsWide;
		float lZoomWidthMin = lCameraWidth / mMaximumNumberPixelsWide;
		if (!ConstantsGame.CAMERA_DEBUG_MODE) {
			// Bound check - zoom out
			if (lZoomFactor < lZoomWidthMin) {
				lZoomFactor = lZoomWidthMin;
				mZoomVelocity = 0;
			}
			// Bound check - zoom in
			if (lZoomFactor > lZoomWidthMax) {
				lZoomFactor = lZoomWidthMax;
				mZoomVelocity = 0;
			}
		}
		// Apply the new zoom factor to the camera object
		mCamera.setZoomFactor(lZoomFactor);
	}

}
