package test.com.tfl.billing;

import com.tfl.billing.TravelTracker;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;
//import org.jmock.Expectations;
//import org.jmock.Mockery;
//import com.tfl.billing;


public class TravelTrackerTest{

    //public void populateDatabases(){
    //  JourneyEvent testJE = new JourneyEvent();
    //}

    @Test
    public void testChargeAccount(){

    }

    @Test
    public void testPeak(){
        TravelTracker travel = new TravelTracker();
        // year month date hour minute
        Date date1 = new Date(2011,12,12,6,0);
        boolean result1 = travel.peak(date1);

        Date date2 = new Date(2011,12,12,0,0);
        boolean result2 = travel.peak(date2);

        Date date3 = new Date(2011,12,12,18,0);
        boolean result3 = travel.peak(date3);

        assertTrue(result1);
        assertFalse(result2);
        assertTrue(result3);
    }


}