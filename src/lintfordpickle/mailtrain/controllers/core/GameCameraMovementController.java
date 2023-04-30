package lintfordpickle.mailtrain.controllers.core;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.trains.Train;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.maths.Vector2f;

public class GameCameraMovementController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Camera Movement Controller";

	private static final float CAMERA_MAN_MOVE_SPEED = 40.f;
	private static final float CAMERA_MAN_MOVE_SPEED_MAX = 10f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Rectangle mPlayArea;
	private ICamera mGameCamera;
	private Vector2f mVelocity;
	private Train mFollowTrain;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isFollowingTrain() {
		return mFollowTrain != null;
	}

	public void setFollowTrain(Train pFollowTrain) {
		mFollowTrain = pFollowTrain;
	}

	public Rectangle playArea() {
		return mPlayArea;
	}

	public void setPlayArea(float pX, float pY, float pWidth, float pHeight) {
		mPlayArea.set(pX, pY, pWidth, pHeight);
	}

	public ICamera gameCamera() {
		return mGameCamera;
	}

	@Override
	public boolean isInitialized() {
		return mGameCamera != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameCameraMovementController(ControllerManager pControllerManager, ICamera pCamera, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mVelocity = new Vector2f();
		mPlayArea = new Rectangle();
		if (pCamera instanceof Camera) {
			final var lCamera = (Camera) pCamera;
			lCamera.setIsChaseCamera(true, 0.06f);
		}
		//
		mGameCamera = pCamera;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (mGameCamera == null)
			return false;

		final float lElapsed = (float) pCore.appTime().elapsedTimeMilli() * 0.001f;
		final float lOneOverCameraZoom = mGameCamera.getZoomFactorOverOne();
		final float speed = CAMERA_MAN_MOVE_SPEED * lOneOverCameraZoom;

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			return false; // editor controls

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT)) {
			mVelocity.x -= speed * lElapsed;
			mFollowTrain = null; // stop auto follow

		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			mVelocity.x += speed * lElapsed;
			mFollowTrain = null; // stop auto follow

		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DOWN)) {
			mVelocity.y += speed * lElapsed;
			mFollowTrain = null; // stop auto follow

		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_UP)) {
			mVelocity.y -= speed * lElapsed;
			mFollowTrain = null; // stop auto follow

		}
		return false;
	}

	@SuppressWarnings("unused")
	@Override
	public void update(LintfordCore pCore) {
		if (mGameCamera == null)
			return;
		if (mFollowTrain != null) {
			if (mFollowTrain.getLeadCar() != null) {
				final var lFrontAxle = mFollowTrain.getLeadCar().frontAxle;
				mGameCamera.setPosition(lFrontAxle.worldPositionX, lFrontAxle.worldPositionY);

			}
		} else {
			// Cap
			if (mVelocity.x < -CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.x = -CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.x > CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.x = CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.y < -CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.y = -CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.y > CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.y = CAMERA_MAN_MOVE_SPEED_MAX;

			float elapsed = (float) pCore.appTime().elapsedTimeMilli();

			// Apply
			float lCurX = mGameCamera.getPosition().x;
			float lCurY = mGameCamera.getPosition().y;

			// FIXME: Why is this double??
			if (!ConstantsGame.CAMERA_DEBUG_MODE && mPlayArea != null && !mPlayArea.isEmpty()) {
				if (lCurX - mGameCamera.getWidth() * .5f < mPlayArea.left()) {
					lCurX = mPlayArea.left() + mGameCamera.getWidth() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT)) // kill velocity
						mVelocity.x = 0;
				}
				if (lCurX + mGameCamera.getWidth() * .5f > mPlayArea.right()) {
					lCurX = mPlayArea.right() - mGameCamera.getWidth() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT)) // kill velocity
						mVelocity.x = 0;
				}
				if (lCurY - mGameCamera.getHeight() * .5f < mPlayArea.top()) {
					lCurY = mPlayArea.top() + mGameCamera.getHeight() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_UP)) // kill velocity
						mVelocity.y = 0;
				}
				if (lCurY + mGameCamera.getHeight() * .5f > mPlayArea.bottom()) {
					lCurY = mPlayArea.bottom() - mGameCamera.getHeight() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DOWN)) // kill velocity
						mVelocity.y = 0;
				}
			}
			mGameCamera.setPosition(lCurX + mVelocity.x * elapsed, lCurY + mVelocity.y * elapsed);

		}
		// DRAG
		mVelocity.x *= 0.857f;
		mVelocity.y *= 0.857f;

		// There are minimums for the camera
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void zoomIn(float pZoomFactor) {
		mGameCamera.setZoomFactor(pZoomFactor);
	}
}
