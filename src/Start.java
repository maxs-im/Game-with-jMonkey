/*	
 *	Start class
 *
 *	First version of the game
 *
 *	Student's lab with jMonkeyEngine
 */

import myrandomizer.RandShape;
import java.util.Vector;
import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.*;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.asset.TextureKey;
import com.jme3.texture.Texture;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterBoxShape;
import com.jme3.font.BitmapText;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioData.DataType;
import com.jme3.collision.CollisionResults;

/** Game initialization in class Start
 *
 * @version 1.0 10 Oct 2017
 * @author Maxim G.
 * */
public class Start extends SimpleApplication {
	/*The game is to move the cone through obstacles*/

    /**Game entrance and assign default settings*/
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1366, 768);
        settings.setTitle("Game");
        settings.setFullscreen(true);

        Start app = new Start();
        app.setSettings(settings);
        app.setShowSettings(false);

        app.start();
    }

    // some constants
    private Node playerNode;
    private int monitorH,
            monitorW,
            borderRatio,
            speedPlayer,
            speedGame;

    private final int lengthRatio = 10;			// percents player on screen

    private boolean isRunning = true;			// if "pause" or not

    private int counter,						// counter game passing
            score;

    private CombinedListener keyListener = new CombinedListener(); 		// for the keyboard


    private Node obstaclesNode;				// separate pivot for the obstacles
    private RandShape ShapeRandomer;		// my own class for randomizing shapes
    private Vector<Geometry> obstacles;		// vector of existing obstacles

    private AudioNode audio_game,
            audio_loose;

    /** Prepare Materials */
    private Material floor_mat,
    //ceiling_mat,
    wall_mat,
            player_mat_Base,
            player_mat_Dot,
            effect_mat;

    private BitmapText scoreText;					// text on the screen for the player score
    private ColorRGBA effect_col = new ColorRGBA(1f, 1f, 0f, 0.5f); // yellow


    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);

        // Off cursor
        inputManager.setCursorVisible(true);

        assignDefaultConstants();
        setCamera();

        initMaterials();

        createPlayer();
        initScene();

        initKeys();
        initAudio();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(isRunning) {
            if(score == 0) {
                obstaclesNode.detachAllChildren();
                obstacles.removeAllElements();
            }
            ++counter;
            if(counter == 2000) {
                ++score;
                counter = 0;

                if(speedGame < borderRatio) {
                    speedGame = speedPlayer + score/speedPlayer;
                }

                scoreText.setText("Your score is " + score);
                tryAdd();
            }

            if(checkCollision()) {
                looseGame();
            }

            tryDel();

            for(Geometry it : obstacles) {
                it.move(0, 0, speedGame*tpf);
                it.rotate(0, speedGame, 0);
            }
        }
        else {
            scoreText.setText("Press \'P\' to continue");
        }
    }

    /** Check if player crashed into the obstacle */
    private boolean checkCollision() {
        CollisionResults results = new CollisionResults();
        obstaclesNode.collideWith(playerNode.getWorldBound(), results);

        return results.size() > 0;
    }

    /** Set music during the game*/
    private void initAudio() {
        audio_game = new AudioNode(assetManager, "Sound/Environment/Ocean Waves.ogg", DataType.Stream);
        audio_game.setPositional(true);
        audio_game.setLooping(true);
        audio_game.setVolume(1);

        rootNode.attachChild(audio_game);
        audio_game.play();

        audio_loose = new AudioNode(assetManager, "Sound/Effects/Gun.wav", DataType.Buffer);
        audio_loose.setLooping(true);  // activate continuous playing
        audio_loose.setPositional(true);
        audio_loose.setVolume(3);

        rootNode.attachChild(audio_loose);
    }

    /** Adding new obstacles if it real */
    private void tryAdd() {
        Geometry geom = ShapeRandomer.getGeom(obstacles.size());

        Random rnd = new Random(System.currentTimeMillis());
        int x = -borderRatio/2 + rnd.nextInt(borderRatio);
        int y = -borderRatio/2 + rnd.nextInt(borderRatio);
        geom.setLocalTranslation(x, y, -(borderRatio + 1)*lengthRatio);

        obstacles.addElement(geom);
        obstaclesNode.attachChild(geom);
    }

    /** Deleting passed obstacles */
    private void tryDel() {
        for(int i = 0; i < obstacles.size(); ++i) {
            if(obstacles.get(i).getLocalTranslation().z > borderRatio) {
                obstaclesNode.detachChild(obstacles.get(i));
                obstacles.remove(i);
            }
        }
    }

    /**  Constants depend on monitor properties. So there we initialize them*/
    private void assignDefaultConstants() {
        monitorH = settings.getHeight();
        monitorW = settings.getWidth();
        borderRatio = (int)Math.sqrt((float)monitorH*monitorW / (monitorH + monitorW)) ;
        speedPlayer = borderRatio;


        obstaclesNode = new Node("ObstaclesNode");
        rootNode.attachChild(obstaclesNode);

        playerNode = new Node("PlayerNode");
        rootNode.attachChild(playerNode);

        ShapeRandomer = new RandShape(this,
                new Vector2f(borderRatio, lengthRatio));
        obstacles = new Vector<>();
        counter = 0;
        score = 0;
    }

    /** Camera settings */
    private void setCamera() {
        // Because camera override WSAD keys
        flyCam.setEnabled(false);

        cam.setLocation(new Vector3f(0, 0, borderRatio/2));
        cam.lookAt(new Vector3f(0, 0, -borderRatio*lengthRatio/2), new Vector3f(0, 0, 0));
        cam.setFrustumPerspective(90, ((float)monitorW)/monitorH, 1, (float)Math.pow(lengthRatio, 3));
    }

    /** This functions create all Materials for the start of the game */
    private void initMaterials() {
        // Initialize material for player's cone
        player_mat_Dot = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");

        player_mat_Base = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        player_mat_Base.setColor("Color", ColorRGBA.Green);

        viewPort.setBackgroundColor( new ColorRGBA(0.06f, 0.06f, 0.06f, 0.1f) );
        // Initialize material for scene
        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey keyF = new TextureKey("Textures/Terrain/splat/mountains512.png");
        Texture texF = assetManager.loadTexture(keyF);
        floor_mat.setTexture("ColorMap", texF);
	    /*
	    ceiling_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    TextureKey keyC = new TextureKey("Textures/Terrain/splat/mountains512.png");
	    Texture texC = assetManager.loadTexture(keyC);
	    ceiling_mat.setTexture("ColorMap", texC);
	    */
        effect_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        effect_mat.setTexture("Texture", assetManager.loadTexture(
                "Effects/Explosion/flame.png"));

        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wall_mat.setColor("Color", effect_col);
    }

    /** Create cone figure for the player*/
    private void createPlayer() {
        float radius = (float)borderRatio/lengthRatio;
        int accuracy = (int)((float)((monitorH > monitorW) ? monitorH : monitorW) / radius);

        Dome shape = new Dome(Vector3f.ZERO, 2, accuracy, radius, false);
        Sphere base = new Sphere(accuracy, 2, radius);

        Geometry coneDot = new Geometry("dot of cone", shape);
        coneDot.setMaterial(player_mat_Dot);

        Geometry coneBase = new Geometry("base of cone", base);
        coneBase.setMaterial(player_mat_Base);

        playerNode.attachChild(coneBase);
        playerNode.attachChild(coneDot);

        //set player at the start point
        playerNode.move(0, 0, (float)-borderRatio/3);
    }

    /** Create scene where cone would flight */
    private void initScene() {
        float boxL = borderRatio*lengthRatio,
                boxW = borderRatio;


        Box floor = new Box(boxW, 0.1f, boxL);
        Geometry floorGeo = new Geometry("Floor", floor);
        floorGeo.setMaterial(floor_mat);
        floorGeo.setLocalTranslation(0, -boxW/2f - 0.05f, -boxL/2f);

        Box wall = new Box(boxW, boxW, 0.1f);

        Geometry wallGeo = new Geometry("Wall", wall);
        wallGeo.setMaterial(wall_mat);
        wallGeo.setLocalTranslation(0, 0, -boxL - 0.05f);
		
		/*
		Box ceiling = new Box(boxW, 0.1f, boxL);
		
		Geometry ceilingGeo = new Geometry("Ceiling", ceiling);
		ceilingGeo.setMaterial(ceiling_mat);
		ceilingGeo.setLocalTranslation(0, boxW/2f + 0.05f, -boxL/2f);
		rootNode.attachChild(ceilingGeo);
		*/
        rootNode.attachChild(floorGeo);
        rootNode.attachChild(wallGeo);

        setPosteriorEffect(boxL);
        setText();
    }

    private void setText() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        scoreText = new BitmapText(guiFont, false);
        scoreText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText.setText("Your score is " + score);
        scoreText.setLocalTranslation( monitorW - 2*scoreText.getLineWidth(),
                monitorH - 2*scoreText.getLineHeight(),
                -borderRatio);
        guiNode.attachChild(scoreText);
    }

    /** Initialize effect on the back wall */
    private void setPosteriorEffect(float boxL) {
        ParticleEmitter fire =
                new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 100);
        fire.setMaterial(effect_mat);

        fire.setImagesX(borderRatio/2);
        fire.setImagesY((int)(borderRatio/1.5));
        fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fire.setStartColor( effect_col ); // yellow
        fire.setShape(new EmitterBoxShape(new Vector3f(-borderRatio*2, -borderRatio*2, -boxL/2),
                new Vector3f(borderRatio*2, borderRatio*2, -boxL)));
        fire.setLocalTranslation(0, 0, -boxL/2f);
        fire.setStartSize(borderRatio/4);
        fire.setEndSize(borderRatio);
        fire.setGravity(0, 1, 0);
        fire.setLowLife(1f);
        fire.setHighLife(5f);
        fire.getParticleInfluencer().setVelocityVariation(0.1f);

        rootNode.attachChild(fire);
    }

    /** Reset all results after crashing (loose) */
    private void looseGame() {
        audio_game.stop();
        audio_loose.playInstance();
        isRunning = false;
        score = 0;
        counter = 0;
        speedPlayer = 10;
    }

    /**Keyboard control*/
    private void initKeys() {

        // pause the game
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));

        // Move control
        {
            inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W),
                    new KeyTrigger(KeyInput.KEY_UP));
            inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S),
                    new KeyTrigger(KeyInput.KEY_DOWN));
            inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A),
                    new KeyTrigger(KeyInput.KEY_LEFT));
            inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D),
                    new KeyTrigger(KeyInput.KEY_RIGHT));
        }

        // Rotate control
        {
            inputManager.addMapping("Rforward", new KeyTrigger(KeyInput.KEY_NUMPAD8));
            inputManager.addMapping("Rback", new KeyTrigger(KeyInput.KEY_NUMPAD2));
            inputManager.addMapping("Rleft", new KeyTrigger(KeyInput.KEY_NUMPAD4));
            inputManager.addMapping("Rright", new KeyTrigger(KeyInput.KEY_NUMPAD6));
        }


        inputManager.addListener(keyListener, "Pause",
                "Left", "Right", "Up", "Down",
                "Rforward", "Rback", "Rleft", "Rright");
    }
    /**Listener for keys*/
    private class CombinedListener implements AnalogListener, ActionListener {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if(name.equals("Pause")  && !keyPressed) {
                isRunning = !isRunning;
                if(!isRunning)
                    audio_game.pause();
                else
                    audio_game.play();
            }
        }

        public void onAnalog(String name, float value, float tpf) {
            if(isRunning) {
                if(name.equals("Right")) {
                    Vector3f pos = playerNode.getLocalTranslation();
                    if((pos.getX() + value*speedPlayer) < (borderRatio - borderRatio/lengthRatio))
                        playerNode.move(value*speedPlayer, 0, 0);
                }
                if(name.equals("Left")) {
                    Vector3f pos = playerNode.getLocalTranslation();
                    if((pos.getX() - value*speedPlayer) > (-borderRatio + borderRatio/lengthRatio))
                        playerNode.move(-value*speedPlayer, 0, 0);
                }
                if(name.equals("Up")) {
                    Vector3f pos = playerNode.getLocalTranslation();
                    if((pos.getY() + value*speedPlayer) < (borderRatio/2f - borderRatio/lengthRatio))
                        playerNode.move(0, value*speedPlayer, 0);
                }
                if(name.equals("Down")) {
                    Vector3f pos = playerNode.getLocalTranslation();
                    if((pos.getY() - value*speedPlayer) > (-borderRatio/2f + borderRatio/lengthRatio))
                        playerNode.move(0, -value*speedPlayer, 0);
                }
                if(name.equals("Rforward")) {
                    playerNode.rotate( value*speedPlayer/4, 0, 0);
                }
                if(name.equals("Rback")) {
                    playerNode.rotate( -value*speedPlayer/4, 0, 0);
                }
                if(name.equals("Rleft")) {
                    playerNode.rotate(0, 0, value*speedPlayer/4);
                }
                if(name.equals("Rright")) {
                    playerNode.rotate(0, 0, -value*speedPlayer/4);
                }
            }
        }
    }
}