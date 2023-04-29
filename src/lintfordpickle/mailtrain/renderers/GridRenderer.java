package lintfordpickle.mailtrain.renderers;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.batching.TextureBatchPCT;
import net.lintford.library.core.graphics.linebatch.LineBatch;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class GridRenderer extends BaseRenderer implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Grid Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private LineBatch mLineBatch;

	protected int mTrackLogicalCounter;
	protected float mUiTextScale = 1.f;
	protected float mGameTextScale = .4f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return false;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GridRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mLineBatch = new LineBatch();

		mTrackLogicalCounter = -1;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
	}

	@Override
	public void loadResources(ResourceManager pResourceManager) {
		super.loadResources(pResourceManager);

		mLineBatch.loadResources(pResourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mLineBatch.unloadResources();
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R, this)) {
			mTrackLogicalCounter = -1;
		}
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);
	}

	@Override
	public void draw(LintfordCore pCore) {
		drawGrid(pCore, mRendererManager.uiSpriteBatch(), 32);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawGrid(LintfordCore pCore, TextureBatchPCT pTextureBatch, int pGridSize) {
		final float lLeftEdge = pCore.gameCamera().boundingRectangle().left();
		final float lLeftOffset = lLeftEdge % pGridSize;

		final float lRightEdge = pCore.gameCamera().boundingRectangle().right();

		final float lTopEdge = pCore.gameCamera().boundingRectangle().top();
		final float lTopOffset = lTopEdge % pGridSize;

		final float lBottomEdge = pCore.gameCamera().boundingRectangle().bottom();

		final float lAlphaAmt = 0.2f;

		mLineBatch.lineAntialiasing(false);
		mLineBatch.lineWidth(1f);
		mLineBatch.lineType(GL11.GL_LINES);

		final float lR = 0.08f;
		final float lG = 0.1f;
		final float lB = 0.5f;
		final float lZ = -.1f;

		mLineBatch.begin(pCore.gameCamera());
		for (float x = lLeftEdge - lLeftOffset; x < lRightEdge; x += pGridSize) {
			mLineBatch.draw(x, lTopEdge, x, lBottomEdge, lZ, lR, lG, lB, lAlphaAmt);
			for (float y = lTopEdge - lTopOffset; y < lBottomEdge; y += pGridSize) {
				mLineBatch.draw(lLeftEdge, y, lRightEdge, y, lZ, lR, lG, lB, lAlphaAmt);

			}
		}
		mLineBatch.end();
	}

	@Override
	public boolean isCoolDownElapsed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetCoolDownTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean allowKeyboardInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowGamepadInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		// TODO Auto-generated method stub
		return false;
	}

}
