
package com.mygdx.game.loxodonta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.util.ScreenListener;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class AboutMode implements Screen, InputProcessor, ControllerListener {
    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "ui/ui_screens/about.png";
    private static final String BACK_BTN_FILE = "ui/ui_buttons/back_button.png";
    private static final String BACK_BTN_HOVER_FILE = "ui/ui_buttons/back_button_hover.png";
    /** The font for labeling the levels */
    private static final String LEVEL_FONT_FILE = "fonts/lobster.ttf";

    /** Current progress (0 to 1) of the asset manager */
    private float progress;

    /** Background texture for start-up */
    private Texture background;
    /** Back button pressed down*/
    private int backdown;

    /** InputController*/
    public InputController input;

    /** The font for labeling levels */
    protected BitmapFont levelFont;
    /** The back button */
    private Button backButton;
    /** The back button hover texture */
    private Texture backButtonHover;
    /** The back button texture */
    private Texture backButtonText;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Amount to scale the level button */
    private static float BUTTON_SCALE  = 1.0f;
    /** Amount to scale the back button */
    private static float BACK_BUTTON_SCALE = 0.5f;
    /** Amount to offset between each level button */
    private static int LEVEL_OFFSET = 220;
    /** Where to start drawing the level buttons */
    private static int LEVEL_START_LOC = 200;
    /** The font size for labelling levels */
    private static int LEVEL_FONT_SIZE = 60;
    /** Amount to offset the back button in the X direction */
    private int BACK_BUTTON_X_OFFSET = 60;
    /** Amount to offset the back button in the Y direction */
    private int BACK_BUTTON_Y_OFFSET = 650;

    /** AssetManager to be loading in the background */
    private AssetManager manager;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the level button */
    private int   playpressState;
    /** The current state of the back button */
    private int   backpressState;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;
    /** Whether or not this player mode is still active */
    private boolean active;

    /**
     * Returns the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return playpressState == 3 || backpressState == 3;
    }

    /**
     * Creates a LoadingMode with the default budget, size and position.
     *
     * @param manager The AssetManager to load in the background
     */
    public AboutMode(GameCanvas canvas, AssetManager manager) {
        this(canvas, manager,DEFAULT_BUDGET);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param manager The AssetManager to load in the background
     * @param millis The loading budget in milliseconds
     */
    public AboutMode(GameCanvas canvas, AssetManager manager, int millis) {
        this.manager = manager;
        this.canvas  = canvas;
        budget = millis;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        input = new InputController(KeyboardLayout.fromPresetLayout(0));//new InputController();

        // Load the next two images immediately.
        background = new Texture(BACKGROUND_FILE);
        progress = 0;

        backButtonHover = new Texture(BACK_BTN_HOVER_FILE);
        backButtonText = new Texture(BACK_BTN_FILE);
        backButton = new Button(backButtonText, backButtonHover);

        //load font
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params2 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params2.fontFileName = LEVEL_FONT_FILE;
        size2Params2.fontParameters.size = LEVEL_FONT_SIZE;
        manager.load(LEVEL_FONT_FILE, BitmapFont.class, size2Params2);

        // No progress so far.
        playpressState = 0;
        backpressState = 0;
        active = false;

        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {

        background.dispose();
        background = null;

    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (levelFont == null) {
            manager.update(budget);
            // Allocate the font
            if (manager.isLoaded(LEVEL_FONT_FILE)) {
                levelFont = manager.get(LEVEL_FONT_FILE, BitmapFont.class);
            } else {
                levelFont = null;
            }
        }


    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background, 0, 0);

        int row = 0;
        float yOffset = centerY;
        float xOffset = LEVEL_START_LOC - LEVEL_OFFSET;


        float ox = backButton.get_texture().getWidth() / 2;
        float oy = backButton.get_texture().getHeight() / 2;
        canvas.draw(backButton.get_texture(), Color.WHITE, ox, oy, BACK_BUTTON_X_OFFSET, BACK_BUTTON_Y_OFFSET, 0,
                BACK_BUTTON_SCALE * scale, BACK_BUTTON_SCALE * scale);

        canvas.end();
    }

    public void readKeyboard(){
        input.readInput();

    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            this.progress = manager.getProgress();
            update(delta);
            readKeyboard();
            if (progress >= 1.0f){
                draw();
            }

            // We are are ready, notify our listener
            if (isReady() && listener != null) {
                if (playpressState == 3){
                    listener.exitScreen(this, GDXRoot.EXIT_CODE_DEFAULT);
                } else{
                    listener.exitScreen(this, GDXRoot.EXIT_CODE_QUIT);
                }

            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerY = height/2;
        centerX = width/2;
        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playpressState == 3 || backpressState == 3) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // Back button
        float backh = BACK_BUTTON_SCALE*scale*backButton.get_texture().getHeight()/2;
        float backw = BACK_BUTTON_SCALE*scale*backButton.get_texture().getWidth()/2;

        float yOffsetBack = BACK_BUTTON_Y_OFFSET;
        float xOffsetBack = BACK_BUTTON_X_OFFSET;

        float yOffset = centerY;
        float xOffset = LEVEL_START_LOC - LEVEL_OFFSET;
        if (screenY > yOffsetBack - backh && screenY < yOffsetBack + backh && screenX >
                xOffsetBack - backw && screenX < xOffsetBack + backw) {
            backButton.set_state(Button.state.DOWN);
            if (backpressState == 1) {
                backpressState = 2;
            }
        }

        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (backpressState == 2){
            backButton.set_state(Button.state.UP);
            backpressState = 3;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (buttonCode == startButton && playpressState == 0) {
            playpressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (playpressState == 2 && buttonCode == startButton) {
            playpressState = 3;
            return false;
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     *
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            if (playpressState == 2){
                playpressState = 3;
            }
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        if (progress == 1.0) {
            screenY = heightY-screenY;

            // Back button
            float backh = BACK_BUTTON_SCALE*scale*backButton.get_texture().getHeight()/2;
            float backw = BACK_BUTTON_SCALE*scale*backButton.get_texture().getWidth()/2;
            float yOffsetBack = BACK_BUTTON_Y_OFFSET;
            float xOffsetBack = BACK_BUTTON_X_OFFSET;

            if (screenY > yOffsetBack - backh && screenY < yOffsetBack + backh && screenX >
                    xOffsetBack - backw && screenX < xOffsetBack + backw) {
                backButton.set_state(Button.state.HOVER);
                backpressState = 1;
                playpressState = 0;
            } else{
                backButton.set_state(Button.state.WAIT);
            }
        }

        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     *
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode 	The POV controller moved
     * @param value 	The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     *
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     *
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     *
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

}