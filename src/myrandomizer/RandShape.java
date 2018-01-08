/*
*	RandShape class
*
*	Randomize Shapes for the main class Start. It depends on existing application.
*/
package myrandomizer;

import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.texture.Texture;
import com.jme3.math.*;
import com.jme3.scene.shape.*;

public class RandShape {
    private final int numberObstacle = 5;		// number of different colors | shaped of obstacles
    private Material mat[];

    private SimpleApplication app;
    private int maxVal, maxCoef;				// max size of the figure

    public RandShape(SimpleApplication _app, Vector2f size) {
        app = _app;
        maxVal = (int)size.x;
        maxCoef = (int)size.y;
        initMat();
    }

    public Geometry getGeom(int con) {
        Mesh shape = createForm();

        Random random = new Random();
        int num = random.nextInt(numberObstacle);

        Material loc_mat = mat[num];
        Geometry geom = new Geometry("NewGeom" + con, shape);
        geom.setMaterial(loc_mat);

        return geom;
    }

    private Mesh createForm() {
        Random random = new Random();
        int i = random.nextInt(numberObstacle);
        Mesh ans;
        float radius = 1.5f*random.nextFloat()*maxVal/maxCoef;
        switch(i) {
            case 0:
                ans = new Sphere(32, 32, radius, false, false);		//Sphere
                break;
            case 1:
                ans = new Dome(Vector3f.ZERO, 2, 4, radius,false); 	// Pyramid
                break;
            case 2:
                ans = new Dome(Vector3f.ZERO, 32, 32, radius,false); // Small hemisphere
                break;
            case 3:
                ans = new PQTorus(5,3, radius, 1f, 32, 32); // Spiral torus
                break;
            case 4:
                ans = new Box(random.nextInt(maxVal - maxCoef),
                        random.nextInt(maxVal - maxCoef),
                        random.nextInt(maxVal - maxCoef));
                break;
            default:
                ans = new Dome(Vector3f.ZERO, 2, 32, 1f,false); // Cone
        }
        return ans;
    }

    /** Initialize all materials for the obstacles */
    private void initMat() {
        mat = new Material[numberObstacle];
        for(int i = 0; i < numberObstacle; ++i) {
            mat[i] = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            TextureKey tex_key = new TextureKey("Textures/Terrain/Pond/Pond_normal.png"); // default
            switch(i) {
                case 0:
                    tex_key = new TextureKey("Textures/ColoredTex/Monkey.png");
                    break;
                case 1:
                    tex_key = new TextureKey("Textures/Terrain/Pond/Pond_normal.png");
                    break;
                case 2:
                    tex_key = new TextureKey("Textures/Terrain/splat/grass.jpg");
                    break;
                case 3:
                    tex_key = new TextureKey("Textures/Terrain/splat/dirt.jpg");
                    break;
                case 4:
                    tex_key = new TextureKey("Textures/Terrain/splat/road.jpg");
                    break;
            }
            Texture tex = app.getAssetManager().loadTexture(tex_key);
            mat[i].setTexture("ColorMap", tex);
        }
    }
}
