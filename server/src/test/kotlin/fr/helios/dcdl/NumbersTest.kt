package fr.helios.dcdl

import fr.helios.dcdl.rules.NumbersRules.Score.calculateScore
import org.junit.Assert
import kotlin.test.Test

class NumbersTest {

    @Test
    fun testCalculateScore() {
        Assert.assertEquals(calculateScore(100, 0, 100), 10)
        Assert.assertEquals(calculateScore(100, 0, 102), 7)
        Assert.assertEquals(calculateScore(100, 0, 103), 6)
        Assert.assertEquals(calculateScore(100, 0, 120), 1)
        Assert.assertEquals(calculateScore(100, 0, 121), 0)

        Assert.assertEquals(calculateScore(510, 0, 510), 10)
        Assert.assertEquals(calculateScore(510, 0, 525), 7)
        Assert.assertEquals(calculateScore(510, 0, 526), 6)
        Assert.assertEquals(calculateScore(510, 0, 617), 1)
        Assert.assertEquals(calculateScore(510, 0, 618), 0)

        Assert.assertEquals(calculateScore(100, 10, 110), 10)
        Assert.assertEquals(calculateScore(100, 10, 90), 10)
        Assert.assertEquals(calculateScore(100, 10, 111), 7)
        Assert.assertEquals(calculateScore(100, 10, 89), 7)
        Assert.assertEquals(calculateScore(100, 10, 130), 1)
        Assert.assertEquals(calculateScore(100, 10, 70), 1)
        Assert.assertEquals(calculateScore(100, 10, 131), 0)
        Assert.assertEquals(calculateScore(100, 10, 69), 0)

        //error
        Assert.assertEquals(calculateScore(100, 10, 100), 0)
    }
}
