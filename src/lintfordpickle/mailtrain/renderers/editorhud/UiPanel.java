package lintfordpickle.mailtrain.renderers.editorhud;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.controllers.editor.EditorBrushController;
import lintfordpickle.mailtrain.controllers.editor.interfaces.IBrushModeCallback;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.data.textures.HudTextureNames;
import net.lintford.library.ConstantsApp;
import net.lintford.library.controllers.hud.HudLayoutController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.batching.SpriteBatch;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.graphics.textures.CoreTextureNames;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.renderers.windows.ConstantsUi;
import net.lintford.library.renderers.windows.UIWindowChangeListener;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.IScrollBarArea;
import net.lintford.library.renderers.windows.components.IUiWidgetInteractions;
import net.lintford.library.renderers.windows.components.ScrollBarContentRectangle;
import net.lintford.library.renderers.windows.components.UIWidget;

public abstract class UiPanel implements IScrollBarArea, UIWindowChangeListener, IInputProcessor, IUiWidgetInteractions, IBrushModeCallback {

	// --------------------------------------
	// Constants
	// --------------------------------------

	protected static final int BUTTON_SET_LAYER = 500;
	protected static final int BUTTON_SHOW_LAYER = 501;

	// --------------------------------------
	// Variables
	// --------------------------------------

	protected UiWindow mParentWindow;
	protected int mEntityGroupUid;
	protected boolean mRenderPanelTitle;
	protected String mPanelTitle;
	protected float mPanelBarHeight;
	protected float mMouseClickTimer;

	protected boolean mIsExpandable;
	protected final Rectangle mExpandRectangle = new Rectangle();
	protected boolean mIsPanelOpen; // expanded

	// Brushes

	protected HudLayoutController mUiStructureController;
	protected EditorBrushController mEditorBrushController;

	protected SpriteSheetDefinition mCoreSpritesheet;
	protected SpriteSheetDefinition mHudSpritesheet;

	protected Rectangle mPanelArea;

	protected float mPaddingTop;
	protected float mPaddingLeft;
	protected float mVerticalSpacing;
	protected float mHorizontalSpacing;
	protected float mPaddingBottom;
	protected float mPaddingRight;

	protected boolean mShowShowLayerButton;
	private final Rectangle mShowLayerButtonRect = new Rectangle();
	private boolean mIsLayerVisibleToggleOn;

	protected boolean mShowActiveLayerButton;
	private final Rectangle mActiveLayerButtonRect = new Rectangle();
	private boolean mIsLayerActiveToggleOn;
	protected EditorLayer mEditorLayer = EditorLayer.Nothing;

	protected final List<UIWidget> mWidgets = new ArrayList<>();

	// --------------------------------------
	// Properties
	// --------------------------------------

	public void isLayerActive(boolean newValue) {
		mIsLayerActiveToggleOn = newValue;
	}

	public boolean isLayerActive() {
		return mIsLayerActiveToggleOn;
	}

	public void isLayerVisible(boolean newValue) {
		mIsLayerVisibleToggleOn = newValue;
	}

	public boolean isLayerVisible() {
		return mIsLayerVisibleToggleOn;
	}

	public HudLayoutController uiStructureController() {
		return mUiStructureController;
	}

	public void addWidget(UIWidget newWidget) {
		mWidgets.add(newWidget);
	}

	public Rectangle panelArea() {
		return mPanelArea;
	}

	public float getTitleBarHeight() {
		return 32.f;
	}

	public float getPanelFullHeight() {
		if (mIsPanelOpen == false)
			return 32.f; // standard panel title height

		float totalHeight = mPaddingTop;

		if (mRenderPanelTitle || mIsExpandable) {
			totalHeight += getTitleBarHeight();
			totalHeight += mVerticalSpacing;
		}

		final int lNumWidgets = mWidgets.size();
		for (int i = 0; i < lNumWidgets; i++) {
			final var lWidget = mWidgets.get(i);
			totalHeight += lWidget.marginTop();
			totalHeight += lWidget.height();
			totalHeight += lWidget.marginBottom();
			if (i < lNumWidgets - 1) {
				totalHeight += mVerticalSpacing;
			}
		}

		return totalHeight + mPaddingBottom;
	}

	public boolean isOpen() {
		return mIsPanelOpen;
	}

	public void isOpen(boolean isOpen) {
		mIsPanelOpen = isOpen;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UiPanel(UiWindow parentWindow, String panelTitle, int entityGroupdUid) {
		mParentWindow = parentWindow;
		mPanelTitle = panelTitle;
		mEntityGroupUid = entityGroupdUid;

		mPanelArea = new Rectangle();

		mIsPanelOpen = false;

		mIsExpandable = true;
		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void initialize(LintfordCore core) {
		mUiStructureController = (HudLayoutController) core.controllerManager().getControllerByName(HudLayoutController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mEditorBrushController = (EditorBrushController) core.controllerManager().getControllerByName(EditorBrushController.CONTROLLER_NAME, mEntityGroupUid);

		final int lNumWidgets = mWidgets.size();
		for (int i = 0; i < lNumWidgets; i++) {
			final var lWidget = mWidgets.get(i);

			lWidget.initialize();
		}
	}

	public void loadResources(ResourceManager resourceManager) {
		mCoreSpritesheet = resourceManager.spriteSheetManager().coreSpritesheet();
		mHudSpritesheet = resourceManager.spriteSheetManager().getSpriteSheet("SPRITESHEET_HUD", ConstantsGame.GAME_RESOURCE_GROUP_ID);

		final int lNumWidgets = mWidgets.size();
		for (int i = 0; i < lNumWidgets; i++) {
			final var lWidget = mWidgets.get(i);

			lWidget.loadResources(resourceManager);
		}
	}

	public void unloadResources() {
		final int lNumWidgets = mWidgets.size();
		for (int i = 0; i < lNumWidgets; i++) {
			final var lWidget = mWidgets.get(i);

			lWidget.unloadResources();
		}
	}

	public boolean handleInput(LintfordCore core) {
		final float lMouseX = core.HUD().getMouseWorldSpaceX();
		final float lMouseY = core.HUD().getMouseWorldSpaceY();

		if (mShowActiveLayerButton) {
			if (mActiveLayerButtonRect.intersectsAA(lMouseX, lMouseY))
				if (core.input().mouse().tryAcquireMouseLeftClickTimed(hashCode(), this)) {
					handleSetLayerToggle(core);
					return true;
				}
		}

		if (mShowShowLayerButton) {
			if (mShowLayerButtonRect.intersectsAA(lMouseX, lMouseY))
				if (core.input().mouse().tryAcquireMouseLeftClickTimed(hashCode(), this)) {
					mIsLayerVisibleToggleOn = !mIsLayerVisibleToggleOn;

					widgetOnClick(core.input(), BUTTON_SHOW_LAYER);
					return true;
				}
		}

		if (mIsExpandable) {
			if (mExpandRectangle.intersectsAA(lMouseX, lMouseY))
				if (core.input().mouse().tryAcquireMouseLeftClickTimed(hashCode(), this)) {
					mIsPanelOpen = !mIsPanelOpen;
					return true;
				}
		}

		final boolean result = false;
		if (mIsPanelOpen) {
			final int lNumWidgets = mWidgets.size();
			for (int i = 0; i < lNumWidgets; i++) {
				final var lWidget = mWidgets.get(i);

				lWidget.handleInput(core);
			}
		}

		return result;
	}

	public void update(LintfordCore core) {
		if (mMouseClickTimer > 0)
			mMouseClickTimer -= core.gameTime().elapsedTimeMilli();

		mPaddingLeft = 5.f * mUiStructureController.uiScaleFactor();
		mHorizontalSpacing = 5.f * mUiStructureController.uiScaleFactor();
		mPaddingRight = 5.f * mUiStructureController.uiScaleFactor();

		mPaddingTop = 5.f * mUiStructureController.uiScaleFactor();
		mVerticalSpacing = 5.f * mUiStructureController.uiScaleFactor();
		mPaddingBottom = 5.f * mUiStructureController.uiScaleFactor();

		final float lCanvasScale = mParentWindow != null ? mParentWindow.uiStructureController().uiScaleFactor() : 1.0f;
		final float lTitleButtonSize = 25.f;
		final float lInsetSize = (32.f - lTitleButtonSize) / 2.f;

		// @formatter:off
		int lButtonCounter = 1;
		if(mIsExpandable) {
			mExpandRectangle.set(
					mPanelArea.x() + mPanelArea.width() - lInsetSize - lTitleButtonSize * lCanvasScale, 
					mPanelArea.y() + lInsetSize, 
					lTitleButtonSize,
					lTitleButtonSize);
			lButtonCounter++;
		}
		
		mIsLayerActiveToggleOn = mEditorLayer != EditorLayer.Nothing && mEditorBrushController.isLayerActive(mEditorLayer);
		if(mShowActiveLayerButton) {
			mActiveLayerButtonRect.set(
					mPanelArea.x() + mPanelArea.width() - lInsetSize * lButtonCounter - (lTitleButtonSize * lCanvasScale) * lButtonCounter, 
					mPanelArea.y() + lInsetSize, 
					lTitleButtonSize,
					lTitleButtonSize);
			lButtonCounter++;
		}
		
		if(mShowShowLayerButton) {
		mShowLayerButtonRect.set(
				mPanelArea.x() + mPanelArea.width() - lInsetSize * lButtonCounter - (lTitleButtonSize * lCanvasScale) * lButtonCounter, 
				mPanelArea.y() + lInsetSize, 
				lTitleButtonSize,
				lTitleButtonSize);
		lButtonCounter++;
		}
		// @formatter:on

		if (mIsPanelOpen) {
			arrangeWidgets(core);

			final int lNumWidgets = mWidgets.size();
			for (int i = 0; i < lNumWidgets; i++) {
				final var lWidget = mWidgets.get(i);

				lWidget.update(core);
			}
		}

	}

	public void draw(LintfordCore core) {
		final var rendererManager = mParentWindow.rendererManager();

		final var lFontUnit = rendererManager.uiTextFont();
		final var lSpriteBatch = rendererManager.uiSpriteBatch();
		final var mCoreSpriteSheet = mParentWindow.coreSpritesheet();

		if (ConstantsApp.getBooleanValueDef("DEBUG_SHOW_UI_COLLIDABLES", false)) {
			lSpriteBatch.begin(core.HUD());
			lSpriteBatch.draw(mCoreSpritesheet, CoreTextureNames.TEXTURE_WHITE, mPanelArea, -0.01f, ColorConstants.Debug_Transparent_Magenta);
			lSpriteBatch.end();
		}

		drawBackground(core, lSpriteBatch, true, -0.01f);

		if (mRenderPanelTitle) {
			mPanelBarHeight = 20.f;
			lFontUnit.begin(core.HUD());
			lFontUnit.drawText(mPanelTitle, mPanelArea.x() + 5.f, mPanelArea.y() + 5.f, -0.01f, 1.f);
			lFontUnit.end();
		}

		lSpriteBatch.begin(core.HUD());
		if (mShowShowLayerButton) {
			if (mIsLayerVisibleToggleOn) {
				lSpriteBatch.draw(mHudSpritesheet, HudTextureNames.TEXTURE_SHOW_LAYER, mShowLayerButtonRect, -0.01f, ColorConstants.WHITE);
			} else {
				lSpriteBatch.draw(mHudSpritesheet, HudTextureNames.TEXTURE_HIDE_LAYER, mShowLayerButtonRect, -0.01f, ColorConstants.WHITE);
			}
		}

		if (mShowActiveLayerButton) {
			if (mIsLayerActiveToggleOn) {
				lSpriteBatch.draw(mHudSpritesheet, HudTextureNames.TEXTURE_SET_LAYER_ON, mActiveLayerButtonRect, -0.01f, ColorConstants.WHITE);
			} else {
				lSpriteBatch.draw(mHudSpritesheet, HudTextureNames.TEXTURE_SET_LAYER_OFF, mActiveLayerButtonRect, -0.01f, ColorConstants.WHITE);
			}
		}

		if (mIsExpandable) {
			if (mIsPanelOpen) {
				lSpriteBatch.draw(mCoreSpriteSheet, CoreTextureNames.TEXTURE_EXPAND, mExpandRectangle, -0.01f, ColorConstants.WHITE);
			} else {
				lSpriteBatch.draw(mCoreSpriteSheet, CoreTextureNames.TEXTURE_COLLAPSE, mExpandRectangle, -0.01f, ColorConstants.WHITE);
			}
		}
		lSpriteBatch.end();

		if (mIsPanelOpen) {
			lFontUnit.begin(core.HUD());
			lSpriteBatch.begin(core.HUD());

			final int lNumWidgets = mWidgets.size();
			for (int i = 0; i < lNumWidgets; i++) {
				final var lWidget = mWidgets.get(i);
				lWidget.draw(core, lSpriteBatch, mCoreSpriteSheet, lFontUnit, -0.01f);
			}

			lSpriteBatch.end();
			lFontUnit.end();
		}
	}

	private void drawBackground(LintfordCore core, SpriteBatch spriteBatch, boolean withTitlebar, float componentDepth) {
		final var lSpriteSheetCore = core.resources().spriteSheetManager().coreSpritesheet();

		final int lTileSize = 32;
		final int posX = (int) mPanelArea.x();
		final int posY = (int) mPanelArea.y();
		final int width = (int) mPanelArea.width();
		final int height = (int) mPanelArea.height();
		final var layoutColor = ColorConstants.WHITE;

		spriteBatch.begin(core.HUD());
		if (mIsPanelOpen == false) {
			spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X1_LEFT, posX, posY, lTileSize, lTileSize, componentDepth, layoutColor);
			spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X1_MID, posX + lTileSize, posY, width - lTileSize * 2, lTileSize, componentDepth, layoutColor);
			spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X1_RIGHT, posX + width - lTileSize, posY, lTileSize, lTileSize, componentDepth, layoutColor);
		} else {
			if (height < 64) {
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_TOP_LEFT, posX, posY, lTileSize, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_TOP_MID, posX + lTileSize, posY, width - lTileSize * 2, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_TOP_RIGHT, posX + width - lTileSize, posY, lTileSize, lTileSize, componentDepth, layoutColor);

				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_BOTTOM_LEFT, posX, posY + height - lTileSize, lTileSize, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_BOTTOM_MID, posX + lTileSize, posY + height - lTileSize, width - lTileSize * 2, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_BOTTOM_RIGHT, posX + width - lTileSize, posY + height - lTileSize, lTileSize, lTileSize, componentDepth, layoutColor);
			} else {
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_TOP_LEFT, posX, posY, lTileSize, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_TOP_MID, posX + lTileSize, posY, width - lTileSize * 2 + 1, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_TOP_RIGHT, posX + width - lTileSize, posY, lTileSize, lTileSize, componentDepth, layoutColor);

				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_MID_LEFT, posX, posY + lTileSize, lTileSize, height - lTileSize * 2 + 1, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_MID_CENTER, posX + lTileSize, posY + lTileSize, width - lTileSize * 2 + 1, height - 64 + 1, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_MID_RIGHT, posX + width - lTileSize, posY + lTileSize, lTileSize, height - lTileSize * 2 + 1, componentDepth, layoutColor);

				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_BOTTOM_LEFT, posX, posY + height - lTileSize, lTileSize, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_BOTTOM_MID, posX + lTileSize, posY + height - lTileSize, width - lTileSize * 2 + 1, lTileSize, componentDepth, layoutColor);
				spriteBatch.draw(lSpriteSheetCore, CoreTextureNames.TEXTURE_PANEL_3X3_01_BOTTOM_RIGHT, posX + width - lTileSize, posY + height - lTileSize, lTileSize, lTileSize, componentDepth, layoutColor);
			}
		}

		spriteBatch.end();
	}

	// --------------------------------------

	public abstract int layerOwnerHashCode();

	private void handleSetLayerToggle(LintfordCore core) {
		final var lIsLayerActive = mEditorLayer != EditorLayer.Nothing && mEditorBrushController.isLayerActive(mEditorLayer);
		if (lIsLayerActive) {
			mEditorBrushController.clearActiveLayer(layerOwnerHashCode());
		} else {
			mEditorBrushController.setActiveLayer(this, mEditorLayer, layerOwnerHashCode());
		}

		isLayerActive(mEditorBrushController.isLayerActive(mEditorLayer));
		mIsLayerActiveToggleOn = mEditorLayer != EditorLayer.Nothing && mEditorBrushController.isLayerActive(mEditorLayer);
	}

	protected abstract void arrangeWidgets(LintfordCore core);

	// --------------------------------------
	// Inherited Methods
	// --------------------------------------

	@Override
	public boolean isCoolDownElapsed() {
		return mMouseClickTimer <= 0.f;
	}

	@Override
	public void resetCoolDownTimer() {
		mMouseClickTimer = ConstantsUi.MOUSE_TIMED_CLICK_TIMER_MS;
	}

	@Override
	public void onWindowOpened() {

	}

	@Override
	public void onWindowClosed() {

	}

	@Override
	public Rectangle contentDisplayArea() {
		return null;
	}

	@Override
	public ScrollBarContentRectangle fullContentArea() {
		return null;
	}

	protected float increaseYPosition(float currentY, UIWidget currentWidget, UIWidget nextWidget) {
		if (currentWidget != null) {
			currentY += currentWidget.height();
			currentY += currentWidget.marginBottom();
		}

		if (nextWidget != null) {
			currentY += nextWidget.marginTop();
		}

		return currentY;
	}

	@Override
	public void onLayerDeselected() {

	}

	@Override
	public void onLayerSelected() {

	}

	@Override
	public void onActionDeselected() {

	}

	@Override
	public void onActionSelected() {

	}
}
