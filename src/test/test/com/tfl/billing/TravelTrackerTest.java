package test.test.com.tfl.billing;

import com.oyster.*;
import com.tfl.billing.*;
import com.tfl.underground.*;
import com.tfl.external.*;
import org.junit.Test;
import static org.junit.Assert.*;
import com.tfl.billing.UnknownOysterCardException;
import java.text.DecimalFormat;
import java.util.*;
import java.math.BigDecimal;
//import org.jmock.Expectations;
//import org.jmock.Mockery;
//import com.tfl.billing;


public class TravelTrackerTest{

    //public void populateDatabases(){
    //  JourneyEvent testJE = new JourneyEvent();
    //}

    @Test
    //tests totalJourneysFor method for if account is charged the right amount depending on if traveling during peak or off peak hours
    public void testChargeAccount(){
        TravelTracker travel = new TravelTracker();

        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();
        Customer customer = customerDatabase.getCustomers().get(0);
        OysterCard card = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");

        OysterCardReader reader = new OysterCardReader();
        OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
        OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);

        travel.connect(paddingtonReader, kingsCrossReader);
        paddingtonReader.touch(card);
        kingsCrossReader.touch(card);

        long currentTime = System.currentTimeMillis();
        Date currentDate = new Date(currentTime);

        if(travel.peak(currentDate)){
            assertEquals(3.20, travel.totalJourneysFor(customer).doubleValue(), 0);
        }else{
            assertEquals(2.40, travel.totalJourneysFor(customer).doubleValue(), 0);
        }

    }

    // Testing the cardScanned method for cases where they're ending the journey
    @Test
    public void testCardScannedJourneyEnd() {
        TravelTracker travel = new TravelTracker();
        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();
        Customer customer = customerDatabase.getCustomers().get(0);
        OysterCard card = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        OysterCardReader reader = new OysterCardReader();
        travel.getCurrentlyTravelling().add(card.id());

        travel.getCurrentlyTravelling().add(card.id());
        travel.cardScanned(card.id(), reader.id());

        assertEquals(1, travel.getEventLog().size());
        assertEquals(card.id(), travel.getEventLog().get(0).cardId());
        assertEquals(0, travel.getCurrentlyTravelling().size());
    }

    // Testing the cardScanned method for cases where they're starting the journey.
    @Test
    public void testCardScannedJourneyStart() {
        TravelTracker travel = new TravelTracker();
        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();
        Customer customer = customerDatabase.getCustomers().get(0);
        OysterCard card = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        OysterCardReader reader = new OysterCardReader();

        travel.cardScanned(card.id(), reader.id());

        assertEquals(1, travel.getEventLog().size());
        assertEquals(card.id(), travel.getEventLog().get(0).cardId());
        assertEquals(1, travel.getCurrentlyTravelling().size());
        assertTrue(travel.getCurrentlyTravelling().contains(card.id()));
    }

    // Testing that the cardScanned method throws an error
    @Test
    public void testCardScannedThrowError() {
        TravelTracker travel = new TravelTracker();
        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();
        Customer customer = customerDatabase.getCustomers().get(2);
        OysterCard card = new OysterCard("07b0bcb1-87df-447f-bf5c-d9961ab9d010");
        OysterCardReader reader = new OysterCardReader();

        try {
            travel.cardScanned(card.id(), reader.id());
            fail("expected exception did not occur.");
        } catch(UnknownOysterCardException e) {

        }
    }
}