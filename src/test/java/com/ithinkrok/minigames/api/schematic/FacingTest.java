package com.ithinkrok.minigames.api.schematic;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by paul on 05/05/16.
 */
@RunWith(DataProviderRunner.class)
public class FacingTest {

    @Test
    @DataProvider({"0,0,0", "0,1,2", "0,2,1", "0,3,3",
            "1,0,1", "1,1,3", "1,2,0", "1,3,2",
            "2,0,3", "2,1,0", "2,2,2", "2,3,1",
            "3,0,2", "3,1,1", "3,2,3", "3,3,0"})
    public void checkCorrectRotateStairsValues(int stairRot, int inputRot, int expected) {
        int actual = Facing.rotateStairs(stairRot, inputRot);

        assertThat(actual).isEqualTo(expected);
    }

}