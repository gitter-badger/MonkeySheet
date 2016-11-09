package com.pesegato.collision;

import com.jme3.scene.Spatial;
import org.dyn4j.collision.broadphase.BroadphaseDetector;
import org.dyn4j.collision.broadphase.BroadphasePair;
import org.dyn4j.collision.broadphase.DynamicAABBTree;
import org.dyn4j.collision.manifold.ClippingManifoldSolver;
import org.dyn4j.collision.manifold.ManifoldSolver;
import org.dyn4j.collision.narrowphase.Gjk;
import org.dyn4j.collision.narrowphase.NarrowphaseDetector;
import org.dyn4j.collision.narrowphase.Sat;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class D4JSpace {
    //private World world;
    private Set<Spatial> spatials = new HashSet<Spatial>();

    BroadphaseDetector<Body, BodyFixture> bp;
    NarrowphaseDetector np;
    ManifoldSolver ms;

    String name="unnamed space";

    public void setName(String name){
        this.name=name;
    }

    public D4JSpace(){
     //   world=new World();

// collision detection process:
// Broadphase -> Narrowphase -> Manifold generation
// create detection chain
        bp = new DynamicAABBTree<Body, BodyFixture>();
        np = new Gjk();
        //NarrowphasePostProcessor npp = LinkPostProcessor();  // Only required if you use the Link shape
        ms = new ClippingManifoldSolver();

    }

    public void add(Spatial spatial) {
        if (spatial.getControl(IDyn4JControl.class) == null) throw new IllegalArgumentException("Cannot handle a node which isnt a ${Dyn4JShapeControl.getClass().getSimpleName()}");
        synchronized(spatials) {
            spatials.add(spatial);
            IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class);
            ctl.addToWorld(bp);
        }
    }

    void remove(Spatial spatial) {
        if (spatial == null || spatial.getControl(IDyn4JControl.class) == null) return;
        spatials.remove(spatials);
        IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class);
        ctl.removeFromWorld();
    }
    public void updateDraw(float tpf) {
        synchronized(spatials) {
            for (Spatial spatial: spatials){
                IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class);
                if (ctl == null) { spatials.remove(spatial); return; } //evict nodes which have their Dyn4JShapeControl removed
                ctl.updateDraw(tpf);
            }
        }
    }

    float tTPF=0;

    public void updatePhysics(float tpf) {
        synchronized(spatials) {
            for (Spatial spatial: spatials){
                IDyn4JControl ctl = spatial.getControl(IDyn4JControl.class);
                if (ctl == null) { spatials.remove(spatial); return; } //evict nodes which have their Dyn4JShapeControl removed
                ctl.updatePhysics(bp, tpf);
            }
        }
        //world.update(tpf, Integer.MAX_VALUE);
        tTPF+=tpf;
        if (tTPF>1/60f) {
            tTPF=0;
            //System.out.println("Collisions for "+name);
            // when ready to detect
            List<BroadphasePair<Body, BodyFixture>> pairs = bp.detect();
            for (BroadphasePair<Body, BodyFixture> pair : pairs) {
                //Body body1 = pair.getCollidable1();
                //Body body2 = pair.getCollidable2();
                BodyFixture fixture1 = pair.getFixture1();
                BodyFixture fixture2 = pair.getFixture2();
                //Transform transform1 = body1.getTransform();
                //Transform transform2 = body2.getTransform();
                //Convex convex2 = fixture2.getShape();
                //Convex convex1 = fixture1.getShape();
                //System.out.println("Collision " + fixture1.getUserData() + " " + fixture2.getUserData());
                for (CollisionListener listener:listeners){
                    listener.listen((Long)fixture1.getUserData(), (Long)fixture2.getUserData());
                }
            /*
            Penetration p = new Penetration();
            if (np.detect(convex1, transform1, convex2, transform2, p)) {
                npp.process(convex1, transform1, convex2, transform2, p);
                Manifold m= new Manifold();
                if (ms.getManifold(p, convex1, transform1, convex2, transform2, m)) {
                    // you have a collision and a manifold to work with
                }
            }
            */
            }
        }
    }

    public boolean checkCollision(Body a, Body b){
        return  bp.detect(a,b);
        //if (bp.detect(a,b)){
        //    return np.detect(body)
        //}
    }

    ArrayList<CollisionListener> listeners=new ArrayList<>();

    public void addListener(CollisionListener cl){
        listeners.add(cl);
    }

}