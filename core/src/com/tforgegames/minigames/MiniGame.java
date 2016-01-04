package com.tforgegames.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiniGame implements Screen {

	public SpriteBatch batch;
	public AssetManager assets;
	
	public PerspectiveCamera camera3d;
	public OrthographicCamera camera2d;

	
	public MiniGame(MiniGameGlobalProperties properties) {
		batch = properties.batch;
		assets = properties.assets;
		
		camera2d = new OrthographicCamera(properties.screenWidth, properties.screenHeight);
		camera2d.setToOrtho(false, properties.screenWidth, properties.screenHeight);
		camera2d.position.set(properties.screenWidth/2, properties.screenHeight/2, 0);
		camera2d.update();
	}

	@Override
	public void show() {
		//entry point
	}

	@Override
	public void render(float delta) {
		//Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera2d.update();
		batch.setProjectionMatrix(camera2d.combined);
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void resize(int width, int height) {
		camera2d.viewportWidth = width;
		camera2d.viewportHeight = height;
		camera2d.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}

}
