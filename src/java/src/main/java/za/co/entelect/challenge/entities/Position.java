package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("y")
    public int lane;

    @SerializedName("x")
    public int block;

    public int getBlock() {
        return block;
    }

    public int getLane() {
        return lane;
    }
}
