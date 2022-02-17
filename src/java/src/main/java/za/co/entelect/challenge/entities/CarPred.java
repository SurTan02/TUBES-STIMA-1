package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.State;

public class CarPred {
    @SerializedName("cmd")
    public int cmd;
    /*
    0: NOTHING
    1: ACCELERATE
    2: DECELERATE
    3: TURN_LEFT
    4: TURN_RIGHT
    5: USE_BOOST
    6: USE_OIL
    7: USE_TWEET 4 76
    8: USE_LIZARD
     */
    public Position position;

    public int speed;

    public State state;

    public int damage;

//    public PowerUps[] powerups;

    public Boolean boosting;

    public int boostCounter;

    public int point;

    public int getSpeed() {
        return speed;
    }

    public int getPoint() {
        return point;
    }
}
