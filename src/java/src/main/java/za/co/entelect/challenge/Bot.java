package za.co.entelect.challenge;

import jdk.internal.org.jline.terminal.impl.PosixSysTerminal;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int MINIMUM_SPEED = 0;
    private static final int SPEED_STATE_1 = 3;
    private static final int INITIAL_SPEED = 5;
    private static final int SPEED_STATE_2 = 6;
    private static final int SPEED_STATE_3 = 8;
    private static final int MAXIMUM_SPEED = 9;
    private static final int BOOST_SPEED   = 15;

    private static final int DMG_MAX_SPEED_0 = 15;
    private static final int DMG_MAX_SPEED_1 = 9;
    private static final int DMG_MAX_SPEED_2 = 8;
    private static final int DMG_MAX_SPEED_3 = 6;
    private static final int DMG_MAX_SPEED_4 = 3;
    private static final int DMG_MAX_SPEED_5 = 0;

    private static final int NOTHING         = 0;
    private static final int ACCELERATE      = 1;
    private static final int DECELERATE      = 2;
    private static final int TURN_LEFT       = 3;
    private static final int TURN_RIGHT      = 4;
    private static final int USE_BOOST       = 5;
    private static final int USE_OIL         = 6;
    private static final int USE_TWEET       = 7;
    private static final int USE_LIZARD      = 8;
    private static final int USE_EMP         = 9;

    private static final int POINT_PICK_BOOST   = 5;
    private static final int POINT_PICK_TWEET   = 4;
    private static final int POINT_PICK_EMP     = 3;
    private static final int POINT_PICK_LIZARD  = 2;
    private static final int POINT_PICK_OIL     = 1;
    private static final int POINT_MUD          = -2;
    private static final int POINT_OIL_SPILL    = -2;
    private static final int POINT_WALL         = -5;
    private static final int POINT_TRUCK        = -15;

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(1);
        directionList.add(-1);
    }

    //Method untuk mengecek apakah bot memiliki powerUp
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    public Command run() {
        List<CarPred> carPred = new ArrayList<>();
        if(hasPowerUp(PowerUps.TWEET, myCar.powerups)){                             //ADD CONDITION : USE TWEET
            carPred.add(UseTweet(myCar));
        }
        if(hasPowerUp(PowerUps.EMP, myCar.powerups)) {                              //ADD CONDITION : USE EMP
            int lane = myCar.position.lane;
            int oplane = opponent.position.lane;
            int block = myCar.position.block;
            int opblock = opponent.position.block;            
            
            if(oplane == (lane-1) || oplane == lane || oplane == (lane+1)){
                if( opblock > block){
                    carPred.add(UseEmp(myCar));
                }
            }          
                
        }
        carPred.add(nothing(myCar));                                                //ADD CONDITION : DO NOTHING
        carPred.add(accelerate(myCar));                                             //ADD CONDITION : ACCELERATE
        carPred.add(decelerate(myCar));                                             //ADD CONDITION : DECELERATE
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && (!myCar.boosting) ){       //ADD CONDITION : PICKUP BOOST
            carPred.add(UseBoost(myCar));
        }
        if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){                           //ADD CONDITION : PICKUP LIZARD
            carPred.add(UseLizard(myCar));
        }
        if (myCar.position.lane != 1) {                                             //ADD CONDITION : TURN LEFT
            carPred.add(turnLeft(myCar));
        }
        if (myCar.position.lane != 4) {                                             //ADD CONDITION : TURN RIGHT
            carPred.add(turnRight(myCar));
        }

        //Sort prioritas command - terhadap speed
        carPred.sort(Comparator.comparing(CarPred::getSpeed).reversed());
        carPred.removeIf(car -> car.getSpeed() < carPred.get(0).getSpeed());
        
        //Sort prioritas command - terhadap point
        carPred.sort(Comparator.comparing(CarPred::getPoint).reversed());
        
        if((myCar.damage >= 2) || ((myCar.damage == 1) &&(hasPowerUp(PowerUps.BOOST, myCar.powerups)))){
            return new FixCommand();                                                //FIX
        }else{
            if (carPred.get(0).cmd == NOTHING) {
                if ((hasPowerUp(PowerUps.OIL, myCar.powerups)) && 
                        (myCar.position.block > opponent.position.block)) {         //OIL
                    return new OilCommand();
                }
                return new DoNothingCommand();                                      //NOTHING
            } else if (carPred.get(0).cmd == ACCELERATE) {                          //ACCELERATE
                return new AccelerateCommand();
            } else if (carPred.get(0).cmd == DECELERATE) {                          //DECELERATE
                return new DecelerateCommand();
            } else if (carPred.get(0).cmd == TURN_LEFT) {                           //TURN_LEFT
                return new ChangeLaneCommand(0);
            } else if (carPred.get(0).cmd == TURN_RIGHT) {                          //TURN_RIGHT
                return new ChangeLaneCommand(1);
            } else if (carPred.get(0).cmd == USE_LIZARD) {                          //USE_LIZARD
                return new LizardCommand();
            } else if (carPred.get(0).cmd == USE_BOOST) {                           //USE_BOOST
                return new BoostCommand();
            } else if (carPred.get(0).cmd == USE_EMP) {                             //USE_EMP
                return new EmpCommand();
            } else if (carPred.get(0).cmd == USE_TWEET) {                           //USE_TWEET
                return new TweetCommand(opponent.position.lane, getOpFinalPosition());
            } else {
                return new DoNothingCommand();
            }
        }
    }

    //Method untuk mendapatkan maxSpeed yang bisa dicapai car
    private int getMaxSpeed() {
        if (myCar.damage == 5) {return  DMG_MAX_SPEED_5;}
        else if (myCar.damage == 4) {return  DMG_MAX_SPEED_4;}
        else if (myCar.damage == 3) {return  DMG_MAX_SPEED_3;}
        else if (myCar.damage == 2) {return DMG_MAX_SPEED_2;}
        else if (myCar.damage == 1) {return DMG_MAX_SPEED_1;}
        else if (myCar.damage == 0) {return DMG_MAX_SPEED_0;} // Hanya bisa dicapai saat boosting}
        else {return -1;}
    }

    //Method untuk memprediksi lokasi mobil lawan pada round selanjutnya
    private int getOpFinalPosition(){
        Position temp = new Position();
        temp.block = opponent.position.block;
        temp.lane = opponent.position.lane;
        
        int maxSpeed;                                                                   //max speed lawan
        if (opponent.damage == 5)      {maxSpeed = DMG_MAX_SPEED_5;}
        else if (opponent.damage == 4) {maxSpeed = DMG_MAX_SPEED_4;}
        else if (opponent.damage == 3) {maxSpeed = DMG_MAX_SPEED_3;}
        else if (opponent.damage == 2) {maxSpeed = DMG_MAX_SPEED_2;}
        else if (opponent.damage == 1) {maxSpeed = DMG_MAX_SPEED_1;}
        else if (opponent.damage == 0) {maxSpeed = DMG_MAX_SPEED_0;}
        else {maxSpeed = -1;}

        int accelerateSpeed = 0;                                                       //prediksi ketika lawan accelerate
        if (opponent.speed == MINIMUM_SPEED)      {accelerateSpeed = SPEED_STATE_1;}
        else if (opponent.speed == SPEED_STATE_1) {accelerateSpeed = INITIAL_SPEED;}
        else if (opponent.speed == INITIAL_SPEED) {accelerateSpeed = SPEED_STATE_2;}
        else if (opponent.speed == SPEED_STATE_2) {accelerateSpeed = SPEED_STATE_3;}
        else if (opponent.speed == SPEED_STATE_3) {accelerateSpeed = MAXIMUM_SPEED;}
        else if (opponent.speed == BOOST_SPEED)   {accelerateSpeed = BOOST_SPEED;}

        if (maxSpeed < accelerateSpeed) {accelerateSpeed = maxSpeed;}

        return temp.block + accelerateSpeed + 1;
    }

    //Method untuk memprediksi speed mobil apabila terkena mud
    private CarPred hitMud(CarPred carPred) {
        CarPred res = carPred;

        res.damage++;
        if (res.damage > 5) {res.damage = 5;}

        if (res.boosting) {
            res.speed = MAXIMUM_SPEED;
        } else {
            if (res.speed == SPEED_STATE_1)         {res.speed = SPEED_STATE_1;}
            else if (res.speed == INITIAL_SPEED)    {res.speed = SPEED_STATE_1;}
            else if (res.speed == SPEED_STATE_2)    {res.speed = SPEED_STATE_1;}
            else if (res.speed == SPEED_STATE_3)    {res.speed = SPEED_STATE_2;}
            else if (res.speed == MAXIMUM_SPEED)    {res.speed = SPEED_STATE_3;}
        }

        if (getMaxSpeed() < res.speed)              {res.speed = getMaxSpeed();}
        return res;
    }

    //Method untuk memprediksi speed mobil apabila terkena oil
    private CarPred hitOil(CarPred carPred) {
        CarPred res = carPred;

        res.damage++;
        if (res.damage > 5) {res.damage = 5;}

        if (res.boosting) {
            res.speed = MAXIMUM_SPEED;
        } else {
            if (res.speed == SPEED_STATE_1)         {res.speed = SPEED_STATE_1;}
            else if (res.speed == INITIAL_SPEED)    {res.speed = SPEED_STATE_1;}
            else if (res.speed == SPEED_STATE_2)    {res.speed = SPEED_STATE_1;}
            else if (res.speed == SPEED_STATE_3)    {res.speed = SPEED_STATE_2;}
            else if (res.speed == MAXIMUM_SPEED)    {res.speed = SPEED_STATE_3;}
        }

        if (getMaxSpeed() < res.speed) {res.speed = getMaxSpeed();}
        return res;
    }

    //Method untuk memprediksi speed mobil apabila terkena Wall
    private CarPred hitWall(CarPred carPred) {
        // Car res = car;
        CarPred res = carPred;

        res.damage += 2;
        if (res.damage > 5) {res.damage = 5;}

        res.speed = SPEED_STATE_1;
        if (getMaxSpeed() < res.speed) {res.speed = getMaxSpeed();}

        return res;
    }

    private CarPred hitTruck(CarPred carPred) {
        // Car res = car;
        CarPred res = carPred;

        res.damage += 2;
        if (res.damage > 5) {res.damage = 5;}

        res.speed = 0;
        if (getMaxSpeed() < res.speed) {res.speed = getMaxSpeed();}

        return res;
    }

    //Method membaca terrain (menghitung point)
    private CarPred checkTerrain(CarPred carPred, Lane carLane) {
        // Car res = car;
        CarPred res = carPred;

        
        if (carLane.OccupiedByCyberTruck)                   {res.point += POINT_TRUCK;     res = hitTruck(res);}
        else if (carLane.terrain == Terrain.MUD)            {res.point += POINT_MUD;        res = hitMud(res);}  
        else if (carLane.terrain == Terrain.OIL_SPILL)      {res.point += POINT_OIL_SPILL;  res = hitOil(res);} 
        else if (carLane.terrain == Terrain.WALL)           {res.point += POINT_WALL;       res = hitWall(res);} 
        else if (carLane.terrain == Terrain.BOOST)          {res.point += POINT_PICK_BOOST;} 
        else if (carLane.terrain == Terrain.TWEET)          {res.point += POINT_PICK_TWEET;} 
        else if (carLane.terrain == Terrain.EMP)            {res.point += POINT_PICK_EMP;} 
        else if (carLane.terrain == Terrain.LIZARD)         {res.point += POINT_PICK_LIZARD;} 
        else if (carLane.terrain == Terrain.OIL_POWER)      {res.point += POINT_PICK_OIL;} 

        return res;
    }

    //Method untuk memprediksi speed mobil apabila 'do nothing'
    private CarPred nothing(Car car) {
        CarPred res = new CarPred();
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = NOTHING;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        int dest = res.speed;
        int initialBlock = block - startBlock;

        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }
        return res;
    }

    //Method untuk memprediksi speed mobil apabila accelerate
    private CarPred accelerate(Car car) {
        CarPred res = new CarPred();
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = ACCELERATE;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        if (res.speed == MINIMUM_SPEED)      {res.speed = SPEED_STATE_1;}
        else if (res.speed == SPEED_STATE_1) {res.speed = INITIAL_SPEED;}
        else if (res.speed == INITIAL_SPEED) {res.speed = SPEED_STATE_2;}
        else if (res.speed == SPEED_STATE_2) {res.speed = SPEED_STATE_3;}
        else if (res.speed == SPEED_STATE_3) {res.speed = MAXIMUM_SPEED;}

        if (getMaxSpeed() < res.speed)      {res.speed = getMaxSpeed();}

        Lane[] laneList = map.get(lane - 1);
        int dest = res.speed;
        int initialBlock = block - startBlock;
        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }
        return res;
    }

    //Method untuk memprediksi speed mobil apabila decelerate
    private CarPred decelerate(Car car) {
        CarPred res = new CarPred();
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = DECELERATE;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        if (res.speed == SPEED_STATE_1)      {res.speed = MINIMUM_SPEED;}
        else if (res.speed == INITIAL_SPEED) {res.speed = SPEED_STATE_1;}
        else if (res.speed == SPEED_STATE_2) {res.speed = INITIAL_SPEED;}
        else if (res.speed == SPEED_STATE_3) {res.speed = SPEED_STATE_2;}
        else if (res.speed == MAXIMUM_SPEED) {res.speed = SPEED_STATE_3;}
        else if (res.speed == BOOST_SPEED)   {res.speed = maxSpeed;}

        Lane[] laneList = map.get(lane - 1);
        int dest = res.speed;
        int initialBlock = block - startBlock;
        for (int i = initialBlock; i <= initialBlock + dest; i++) {         
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }

        return res;
    }

    //Method untuk memprediksi speed mobil apabila turn left
    private CarPred turnLeft(Car car) {
        CarPred res = new CarPred();
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = TURN_LEFT;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 2);
        int dest = res.speed - 1;
        int initialBlock = block - startBlock;
        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }

        return res;
    }

    //Method untuk memprediksi speed mobil apabila turn right
    private CarPred turnRight(Car car) {
        CarPred res = new CarPred();        
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = TURN_RIGHT;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane);
        int dest = res.speed - 1;
        int initialBlock = block - startBlock;
        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }
        
        return res;
    }

    //Method untuk memprediksi speed mobil apabila menggunakan boost
    private CarPred UseBoost(Car car) {
        CarPred res = new CarPred();        
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = USE_BOOST;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = res.position.lane;
        int block = res.position.block;        
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        //speed = 8
        res.speed = getMaxSpeed();
        int dest = res.speed;
        int initialBlock = block - startBlock;
        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            if (laneList[i].terrain == Terrain.MUD || laneList[i].terrain == Terrain.OIL_SPILL || laneList[i].terrain == Terrain.WALL || laneList[i].OccupiedByCyberTruck ){
                res.speed = 0;
                break;
            }
            res.position.block++;
        }
        return res;
    }

    //Method untuk memprediksi speed mobil apabila menggunakan lizard
    private CarPred UseLizard(Car car) {
        CarPred res = new CarPred();        
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = USE_LIZARD;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = res.position.lane;
        int block = res.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane-1);
        int dest = res.speed - 1;
        
        if (opponent.position.lane == temp.lane){
            if (temp.block + res.speed > getOpFinalPosition()){
                return res; 
            }
        }

        int lastBlock = block - startBlock + dest;
        
        for (int i = block - startBlock; i < lastBlock; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }

        if (res.position.block + res.speed > getOpFinalPosition() && res.position.lane == opponent.position.lane){
            res.speed = car.speed;
        }
        else if (res.speed == car.speed){
            res.speed = 0;
        }else{
            res = checkTerrain(res, laneList[lastBlock]);
        }
        return res;
    }

    //Method untuk memprediksi speed mobil apabila tweet
    private CarPred UseTweet(Car car) {
        CarPred res = new CarPred();
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = USE_TWEET;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        int dest = res.speed;
        int initialBlock = block - startBlock;

        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {break;}
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }
        return res;
    }


    private CarPred UseEmp(Car car) {
        CarPred res = new CarPred();
        Position temp = new Position();
        temp.block = car.position.block;
        temp.lane = car.position.lane;

        res.cmd = USE_EMP;
        res.position = temp;
        res.speed = car.speed;
        res.state = car.state;
        res.damage = car.damage;
        res.boosting = car.boosting;
        res.boostCounter = car.boostCounter;
        res.point = 0;

        int lane = car.position.lane;
        int block = car.position.block;
        List<Lane[]> map = new ArrayList<>();
        for (int i = 0; i < gameState.lanes.size(); i++) {
            map.add(gameState.lanes.get(i));
        }
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        int dest = res.speed;
        int initialBlock = block - startBlock;

        for (int i = initialBlock; i <= initialBlock + dest; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {break;}
            res = checkTerrain(res, laneList[i]);
            res.position.block++;
        }
        return res;
    }
}