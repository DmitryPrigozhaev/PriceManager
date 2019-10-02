package com.prigozhaev.util;

import com.prigozhaev.model.Price;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Prigozhaev
 * 01.10.2019
 */

public class PriceManagerTest {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private Collection<Price> existingPrices;
    private Collection<Price> incomingPrices;
    private Collection<Price> result;

    @Before
    public void setUp() {
        existingPrices = new ArrayList<>();
        incomingPrices = new ArrayList<>();
        result = new ArrayList<>();
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeNullPricesTest() {
        existingPrices = null;
        incomingPrices = null;

        PriceManager.merge(existingPrices, incomingPrices);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeEmptyPricesTest() {
        existingPrices = Collections.emptyList();
        incomingPrices = Collections.emptyList();

        PriceManager.merge(existingPrices, incomingPrices);
    }

    @Test
    public void mergeNullWithIncomingPricesTest() throws ParseException {
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 21:00:00"), dateFormat.parse("10.10.2019 21:00:00"), 100L));
        existingPrices = null;

        assertEquals(incomingPrices, PriceManager.merge(existingPrices, incomingPrices));
    }

    @Test
    public void mergeEmptyExistingPricesWithIncomingPricesTest() throws ParseException {
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 21:00:00"), dateFormat.parse("10.10.2019 21:00:00"), 100L));
        assertEquals(incomingPrices, PriceManager.merge(existingPrices, incomingPrices));
    }

    @Test
    public void mergeExistingPricesWithNullTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 21:00:00"), dateFormat.parse("10.10.2019 21:00:00"), 100L));
        incomingPrices = null;

        assertEquals(existingPrices, PriceManager.merge(existingPrices, incomingPrices));
    }

    @Test
    public void mergeExistingPricesWithEmptyIncomingPricesTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 21:00:00"), dateFormat.parse("10.10.2019 21:00:00"), 100L));
        assertEquals(existingPrices, PriceManager.merge(existingPrices, incomingPrices));
    }

    @Test
    public void mergePricesTest() throws ParseException {
        existingPrices.add(new Price("122856", 1, 1, dateFormat.parse("01.01.2013 00:00:00"), dateFormat.parse("31.01.2013 23:59:59"), 11_000L));
        existingPrices.add(new Price("122856", 2, 1, dateFormat.parse("10.01.2013 00:00:00"), dateFormat.parse("20.01.2013 23:59:59"), 9_9000L));
        existingPrices.add(new Price("6654", 1, 2, dateFormat.parse("01.01.2013 00:00:00"), dateFormat.parse("31.01.2013 00:00:00"), 5_000L));

        incomingPrices.add(new Price("122856", 1, 1, dateFormat.parse("20.01.2013 00:00:00"), dateFormat.parse("20.02.2013 23:59:59"), 11_000L));
        incomingPrices.add(new Price("122856", 2, 1, dateFormat.parse("15.01.2013 00:00:00"), dateFormat.parse("25.01.2013 23:59:59"), 92_000L));
        incomingPrices.add(new Price("6654", 1, 2, dateFormat.parse("12.01.2013 00:00:00"), dateFormat.parse("13.01.2013 00:00:00"), 4_000L));

        result.add(new Price("122856", 1, 1, dateFormat.parse("01.01.2013 00:00:00"), dateFormat.parse("20.02.2013 23:59:59"), 11_000L));
        result.add(new Price("122856", 2, 1, dateFormat.parse("10.01.2013 00:00:00"), dateFormat.parse("15.01.2013 00:00:00"), 99_000L));
        result.add(new Price("122856", 2, 1, dateFormat.parse("15.01.2013 00:00:00"), dateFormat.parse("25.01.2013 23:59:59"), 92_000L));
        result.add(new Price("6654", 1, 2, dateFormat.parse("01.01.2013 00:00:00"), dateFormat.parse("12.01.2013 00:00:00"), 5_000L));
        result.add(new Price("6654", 1, 2, dateFormat.parse("12.01.2013 00:00:00"), dateFormat.parse("13.01.2013 00:00:00"), 4_000L));
        result.add(new Price("6654", 1, 2, dateFormat.parse("13.01.2013 00:00:00"), dateFormat.parse("31.01.2013 00:00:00"), 5_000L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergeLeftIntersectionPricesWithEqualsValuesTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 100L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("05.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 100L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 100L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergeLeftIntersectionPricesWithNotEqualsValuesTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 100L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("05.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 200L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("05.10.2019 00:00:00"), 100L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("05.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 200L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergeRightIntersectionPricesWithEqualsValuesTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("05.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 100L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 100L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 100L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergeRightIntersectionPricesWithNotEqualsValuesTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("05.10.2019 00:00:00"), dateFormat.parse("15.10.2019 23:59:59"), 100L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 200L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("10.10.2019 23:59:59"), dateFormat.parse("15.10.2019 23:59:59"), 100L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 200L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergeNotIntersectionPricesTest() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 100L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("20.10.2019 00:00:00"), dateFormat.parse("31.10.2019 23:59:59"), 200L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 23:59:59"), 100L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("20.10.2019 00:00:00"), dateFormat.parse("31.10.2019 23:59:59"), 200L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergePricesFirstExample() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("31.10.2019 23:59:59"), 50L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("10.10.2019 00:00:00"), dateFormat.parse("20.10.2019 23:59:59"), 60L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 00:00:00"), 50L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("10.10.2019 00:00:00"), dateFormat.parse("20.10.2019 23:59:59"), 60L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("20.10.2019 23:59:59"), dateFormat.parse("31.10.2019 23:59:59"), 50L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergePricesSecondExample() throws ParseException {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("20.10.2019 00:00:00"), 100L));
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("20.10.2019 00:00:00"), dateFormat.parse("31.10.2019 23:59:59"), 120L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("15.10.2019 00:00:00"), dateFormat.parse("25.10.2019 23:59:59"), 110L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("15.10.2019 00:00:00"), 100L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("15.10.2019 00:00:00"), dateFormat.parse("25.10.2019 23:59:59"), 110L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("25.10.2019 23:59:59"), dateFormat.parse("31.10.2019 23:59:59"), 120L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

    @Test
    public void mergePricesThirdExample() throws Exception {
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("10.10.2019 00:00:00"), 80L));
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("10.10.2019 00:00:00"), dateFormat.parse("20.10.2019 00:00:00"), 87L));
        existingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("20.10.2019 00:00:00"), dateFormat.parse("31.10.2019 23:59:59"), 90L));

        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("05.10.2019 00:00:00"), dateFormat.parse("15.10.2019 00:00:00"), 80L));
        incomingPrices.add(new Price("price_1", 1, 1, dateFormat.parse("15.10.2019 00:00:00"), dateFormat.parse("25.10.2019 23:59:59"), 85L));

        result.add(new Price("price_1", 1, 1, dateFormat.parse("01.10.2019 00:00:00"), dateFormat.parse("15.10.2019 00:00:00"), 80L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("15.10.2019 00:00:00"), dateFormat.parse("25.10.2019 23:59:59"), 85L));
        result.add(new Price("price_1", 1, 1, dateFormat.parse("25.10.2019 23:59:59"), dateFormat.parse("31.10.2019 23:59:59"), 90L));

        List<Price> mergedPrices = new ArrayList<>(PriceManager.merge(existingPrices, incomingPrices));

        assertTrue(result.containsAll(mergedPrices) && result.size() == mergedPrices.size());
    }

}