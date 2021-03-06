package com.tfl.billing;

import com.oyster.*;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.*;

public class TravelTracker implements ScanListener {

    //static final BigDecimal OFF_PEAK_JOURNEY_PRICE = new BigDecimal(2.40);
    //static final BigDecimal PEAK_JOURNEY_PRICE = new BigDecimal(3.20);
    static final BigDecimal PEAK_LONG = new BigDecimal(3.80);
    static final BigDecimal OFF_PEAK_LONG = new BigDecimal(2.70);
    static final BigDecimal PEAK_SHORT = new BigDecimal(2.90);
    static final BigDecimal OFF_PEAK_SHORT = new BigDecimal(1.60);

    private final List<JourneyEvent> eventLog = new ArrayList<JourneyEvent>();
    private final Set<UUID> currentlyTravelling = new HashSet<UUID>();

    public void chargeAccounts() {
        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();

        List<Customer> customers = customerDatabase.getCustomers();
        for (Customer customer : customers) {
            totalJourneysFor(customer);
        }
    }

    private void totalJourneysFor(Customer customer) {
        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        for (JourneyEvent journeyEvent : eventLog) {
            if (journeyEvent.cardId().equals(customer.cardId())) {
                customerJourneyEvents.add(journeyEvent);
            }
        }

        List<Journey> journeys = new ArrayList<Journey>();

        JourneyEvent start = null;
        for (JourneyEvent event : customerJourneyEvents) {
            if (event instanceof JourneyStart) {
                start = event;
            }
            if (event instanceof JourneyEnd && start != null) {
                journeys.add(new Journey(start, event));
                start = null;
            }
        }

        BigDecimal customerTotal = new BigDecimal(0);
        // Checks all journeys for if it includes a peak journey. Necessary for cap implementation.
        boolean includesPeak = false;
        for (Journey journey1 : journeys) {
            if (journey1.getPeak()) {
                includesPeak = true;
            }
        }

        for (Journey journey : journeys) {
            // Initiate journey price as the lowest cost trip.
            BigDecimal journeyPrice = OFF_PEAK_SHORT;
            if (journey.getPeak() && journey.durationSeconds() < 1500) {
                journeyPrice = PEAK_SHORT;
            } else if (journey.getPeak() && journey.durationSeconds() >= 1500) {
                journeyPrice = PEAK_LONG;
            } else if (!journey.getPeak() && journey.durationSeconds() >= 1500) {
                journeyPrice = OFF_PEAK_LONG;
            }
            // Don't check for off peak short because it's default.
            if (includesPeak && customerTotal.compareTo(new BigDecimal(9.00)) == -1) {
                customerTotal = customerTotal.add(journeyPrice);
            } else if (!includesPeak && customerTotal.compareTo(new BigDecimal(7.00)) == -1) {
                customerTotal = customerTotal.add(journeyPrice);
            }
        }

        PaymentsSystem.getInstance().charge(customer, journeys, roundToNearestPenny(customerTotal));
    }

    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence) {
        return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public void connect(OysterCardReader... cardReaders) {
        for (OysterCardReader cardReader : cardReaders) {
            cardReader.register(this);
        }
    }

    @Override
    public void cardScanned(UUID cardId, UUID readerId) {
        if (currentlyTravelling.contains(cardId)) {
            eventLog.add(new JourneyEnd(cardId, readerId));
            currentlyTravelling.remove(cardId);
        } else {
            if (CustomerDatabase.getInstance().isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                eventLog.add(new JourneyStart(cardId, readerId));
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }
    }

}