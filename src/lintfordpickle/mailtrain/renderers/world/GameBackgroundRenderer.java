package lintfordpickle.mailtrain.renderers.world;

import org.lwjgl.opengl.GL11;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.geometry.FullScreenTexturedQuad;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class GameBackgroundRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "World Background Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Texture mWorldTexture;
	private FullScreenTexturedQuad mFullScreenTexturedQuad;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public int ZDepth() {
		return 1;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameBackgroundRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mFullScreenTexturedQuad = new FullScreenTexturedQuad();
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

		mWorldTexture = pResourceManager.textureManager().loadTexture("TEXTURE_GRASS", "res/textures/textureGrass00.png", GL11.GL_NEAREST, entityGroupID());

		mFullScreenTexturedQuad.loadResources(pResourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mWorldTexture = null;

		mFullScreenTexturedQuad.unloadResources();
	}

	@Override
	public void draw(LintfordCore pCore) {
		final var mTextureBatch = mRendererManager.uiSpriteBatch();

		final float lScale = 1.f;
		final float lDestX = (pCore.gameCamera().getPosition().x / lScale) - 2048.f;
		final float lDestY = (pCore.gameCamera().getPosition().y / lScale) - 2048.f;

//		mTextureBatch.begin(pCore.gameCamera());
//		mTextureBatch.draw(mWorldTexture, lDestX, lDestY, 4096.f * lScale, 4096.f * lScale, lDestX, lDestY, 4096.f, 4096.f, -.9f, ColorConstants.WHITE);
//		mTextureBatch.end();
	}

}
