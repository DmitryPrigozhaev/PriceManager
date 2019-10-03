package com.prigozhaev.util;

import com.prigozhaev.model.Price;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Price Manager utility class.
 * Allows to perform operations with prices.
 *
 * @author Dmitry Prigozhaev
 * 30.09.2019
 * @see Price
 */

public class PriceManager {

    /**
     * The PriceManager class cannot be instantiated.
     */
    private PriceManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The method merges two price collections: existing prices with newly imported from an external system.
     *
     * <p>Price pooling rules:</p>
     *
     * <p>If the product does not yet have prices, or the existing prices do not intersect with
     * the new in the price active periods, then the new prices are simply added to the product;</p>
     *
     * <p>If the existing price intersects in the price active period with the new price, then:</p>
     * <p> • if the price values are the same, the price action period of the existing price
     * increases according to the period of the new price;</p>
     * <p> • if the price values differ, a new price is added, and the price action period of the old price
     * is reduced according to the period of the new price.</p>
     *
     * @param existingPrices the collection of available prices
     * @param incomingPrices the collection of incoming prices
     * @return combined price collection
     * @throws IllegalArgumentException if the incoming collections are null or empty
     */
    public static Collection<Price> merge(Collection<Price> existingPrices, Collection<Price> incomingPrices) {

        if ((existingPrices == null || existingPrices.isEmpty()) && (incomingPrices == null || incomingPrices.isEmpty()))
            throw new IllegalArgumentException("Missing valid data for merge");

        if (existingPrices == null || existingPrices.isEmpty())
            return incomingPrices;

        if (incomingPrices == null || incomingPrices.isEmpty())
            return existingPrices;

        List<Price> result = new ArrayList<>(existingPrices);

        for (Price incomingPrice : incomingPrices) {
            List<Price> existingPricesGroup = result.stream()
                    .filter(existingPrice -> isContain(existingPrice, incomingPrice))
                    .collect(Collectors.toList());
            result.removeAll(existingPricesGroup);
            result.addAll(getPriceListForGroup(existingPricesGroup, incomingPrice));
        }

        return result;
    }

    private static Collection<Price> getPriceListForGroup(List<Price> existingPricesGroup, Price incomingPrice) {

        List<Price> groupOfPricesList = new ArrayList<>();

        if (existingPricesGroup.isEmpty()) {
            groupOfPricesList.add(incomingPrice);
            return groupOfPricesList;
        }

        existingPricesGroup.stream()
                .filter(existingPrice -> incomingPrice.getPriceActionPeriod().isInsideIn(existingPrice.getPriceActionPeriod()))
                .findFirst()
                .ifPresent(existingPrice -> {
                    if (incomingPrice.getValue().equals(existingPrice.getValue())) {
                        incomingPrice.setBegin(existingPrice.getBegin());
                        incomingPrice.setEnd(existingPrice.getEnd());
                    } else {
                        groupOfPricesList.add(new Price(existingPrice, existingPrice.getBegin(), incomingPrice.getBegin()));
                        groupOfPricesList.add(new Price(existingPrice, incomingPrice.getEnd(), existingPrice.getEnd()));
                    }
                });

        existingPricesGroup.stream()
                .filter(existingPrice -> incomingPrice.getPriceActionPeriod().haveIntersectionOnTheRightWith(existingPrice.getPriceActionPeriod()))
                .findFirst()
                .ifPresent(existingPrice -> {
                    if (incomingPrice.getValue().equals(existingPrice.getValue()))
                        incomingPrice.setEnd(existingPrice.getEnd());
                    else
                        groupOfPricesList.add(new Price(existingPrice, incomingPrice.getEnd(), existingPrice.getEnd()));
                });

        existingPricesGroup.stream()
                .filter(existingPrice -> incomingPrice.getPriceActionPeriod().haveIntersectionOnTheLeftWith(existingPrice.getPriceActionPeriod()))
                .findFirst()
                .ifPresent(existingPrice -> {
                    if (incomingPrice.getValue().equals(existingPrice.getValue()))
                        incomingPrice.setBegin(existingPrice.getBegin());
                    else
                        groupOfPricesList.add(new Price(existingPrice, existingPrice.getBegin(), incomingPrice.getBegin()));
                });

        existingPricesGroup.stream()
                .filter(existingPrice -> incomingPrice.getPriceActionPeriod().doesNotIntersectionWith(existingPrice.getPriceActionPeriod()))
                .forEachOrdered(groupOfPricesList::add);

        groupOfPricesList.add(incomingPrice);

        return groupOfPricesList;
    }

    private static boolean isContain(Price existingPrice, Price incomingPrice) {
        return existingPrice.getProductCode().equals(incomingPrice.getProductCode()) &&
                existingPrice.getNumber() == incomingPrice.getNumber() &&
                existingPrice.getDepart() == incomingPrice.getDepart();
    }

}