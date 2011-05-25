package com.expedia.bookings.widget.gl;

import javax.microedition.khronos.opengles.GL10;

/**
 * This is the OpenGL ES version of a sprite.  It is more complicated than the
 * CanvasSprite class because it can be used in more than one way.  This class
 * can draw using a grid of verts, a grid of verts stored in VBO objects, or
 * using the DrawTexture extension.
 */
public class GLSprite extends Renderable {
	// The OpenGL ES texture handle to draw.
	private int mTextureName;
	// The id of the original resource that mTextureName is based on.
	private int mResourceId;
	// If drawing with verts or VBO verts, the grid object defining those verts.
	private Grid mGrid;

	public GLSprite(int resourceId) {
		super();
		mResourceId = resourceId;
	}

	public void setTextureName(int name) {
		mTextureName = name;
	}

	public int getTextureName() {
		return mTextureName;
	}

	public void setResourceId(int id) {
		mResourceId = id;
	}

	public int getResourceId() {
		return mResourceId;
	}

	public void setGrid(Grid grid) {
		mGrid = grid;
	}

	public Grid getGrid() {
		return mGrid;
	}

	public void draw(GL10 gl) {
		if (!visible) {
			return;
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureName);

		if (mGrid == null) {
			mGrid = new Grid(2, 2, false);
			mGrid.set(0, 0, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, null);
			mGrid.set(1, 0, width, 0.0f, 0.0f, 1.0f, 1.0f, null);
			mGrid.set(0, 1, 0.0f, height, 0.0f, 0.0f, 0.0f, null);
			mGrid.set(1, 1, width, height, 0.0f, 1.0f, 0.0f, null);
		}

		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glTranslatef(x, y, 0);

		gl.glTranslatef(rotationX, rotationY, 0);
		gl.glRotatef((float) rotation, 0, 0, 1);
		gl.glTranslatef(-rotationX, -rotationY, 0);

		mGrid.draw(gl, true, false);

		gl.glPopMatrix();
	}
}
