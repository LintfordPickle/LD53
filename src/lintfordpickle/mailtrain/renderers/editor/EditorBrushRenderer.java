package lintfordpickle.mailtrain.renderers.editor;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.controllers.editor.EditorBrushController;
import lintfordpickle.mailtrain.controllers.tracks.TrackController;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class EditorBrushRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Editor Brush Renderer";

	// ---------------------------------------------
	// Variable
	// ---------------------------------------------

	private EditorBrushController mEditorBrushController;

	private float mMouseX;
	private float mMouseY;

	private boolean mRenderBrush;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean renderBrush() {
		return mRenderBrush;
	}

	public void renderBrush(boolean newValue) {
		mRenderBrush = newValue;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public EditorBrushRenderer(RendererManager rendererManager, int entityGroupUid) {
		super(rendererManager, RENDERER_NAME, entityGroupUid);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		final var lControllerManager = core.controllerManager();

		mEditorBrushController = (EditorBrushController) lControllerManager.getControllerByNameRequired(EditorBrushController.CONTROLLER_NAME, mEntityGroupUid);
		mRenderBrush = true;
	}

	@Override
	public boolean handleInput(LintfordCore core) {
		if (!mEditorBrushController.isActive())
			return false;

		mMouseX = core.gameCamera().getMouseWorldSpaceX();
		mMouseY = core.gameCamera().getMouseWorldSpaceY();

		if (mEditorBrushController.brush().isActionSet() == false) {
			if (core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_C)) {
				mEditorBrushController.setCursor(mMouseX, mMouseY);
				mEditorBrushController.setHeightProfilePoint(mMouseX, mMouseY);
			}
		}

		return super.handleInput(core);
	}

	@Override
	public void draw(LintfordCore core) {
		final var lCursorWorldX = mEditorBrushController.cursorWorldX();
		final var lCursorWorldY = mEditorBrushController.cursorWorldY();
		final var wgCursorX = RailTrackInstance.worldToGrid(lCursorWorldX, TrackController.GRID_SIZE_DEPRECATED);
		final var wgCursorY = RailTrackInstance.worldToGrid(lCursorWorldY, TrackController.GRID_SIZE_DEPRECATED);

		GL11.glPointSize(4);

		Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), 0, 0);
		Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lCursorWorldX, lCursorWorldY);

		if (mRenderBrush == false)
			return;

		final var lHudBounds = core.HUD().boundingRectangle();
		final var lEditorBrush = mEditorBrushController.brush();

		final var lFontUnit = mRendererManager.uiTextFont();

		lFontUnit.begin(core.HUD());
		lFontUnit.drawText("brush: " + lEditorBrush.brushLayer(), lHudBounds.left() + 5.f, lHudBounds.bottom() - lFontUnit.fontHeight() - 5.f, -0.01f, 1.f);
		final var lDoingWhat = mEditorBrushController.doingWhatMessage();
		if (lDoingWhat != null) {
			lFontUnit.drawText(lDoingWhat, lHudBounds.left() + 5.f, lHudBounds.bottom() - lFontUnit.fontHeight() * 2 - 5.f, -0.01f, 1.f);
		}
		lFontUnit.end();

		lFontUnit.begin(core.gameCamera());

		Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), lCursorWorldX, lCursorWorldY);
		Debug.debugManager().drawers().drawRectImmediate(core.gameCamera(), wgCursorX - 16f, wgCursorY - 16f, 32, 32);

		final var lFontXOffset = 6.f;
		final var lFontYOffset = (lFontUnit.fontHeight() * .5f) / core.gameCamera().getZoomFactor();
		final var lFontSize = 0.5f / core.gameCamera().getZoomFactor();

		float lCursorTextPositionY = lCursorWorldY - lFontYOffset;
		float lMouseTextPositionY = mMouseY - lFontYOffset;

		if (mEditorBrushController.showPosition()) {
			var mouseDebugText = String.format("(%.2f,%.2f)", mMouseX, mMouseY);
			lFontUnit.drawText(mouseDebugText, mMouseX + lFontXOffset, lMouseTextPositionY += lFontYOffset, -0.001f, lFontSize);

			var cursorDebugText = String.format("(%.2f,%.2f)", lCursorWorldX, lCursorWorldY);
			lFontUnit.drawText(cursorDebugText, lCursorWorldX + lFontXOffset, lCursorTextPositionY += lFontYOffset, -0.001f, lFontSize);
		}

		if (mEditorBrushController.showGridUid()) {
			final int lMouseCellKey = mEditorBrushController.hashGrid().getCellKeyFromWorldPosition((int) mMouseX, (int) mMouseY);
			var mouseDebugText = String.format("%d", lMouseCellKey);
			lFontUnit.drawText(mouseDebugText, mMouseX + lFontXOffset, lMouseTextPositionY += lFontYOffset, -0.001f, lFontSize);

			final int lCursorCellKey = mEditorBrushController.hashGrid().getCellKeyFromWorldPosition((int) lCursorWorldX, (int) lCursorWorldY);
			var cursorDebugText = String.format("%d", lCursorCellKey);
			lFontUnit.drawText(cursorDebugText, lCursorWorldX + lFontXOffset, lCursorTextPositionY += lFontYOffset, -0.001f, lFontSize);
		}

		lFontUnit.end();
	}
}
