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
            List<Price> availablePriceGroup = result.stream()
                    .filter(availablePrice -> isContain(availablePrice, incomingPrice))
                    .collect(Collectors.toList());
            result.removeAll(availablePriceGroup);
            result.addAll(getPriceListForGroup(availablePriceGroup, incomingPrice));
        }

        return result;
    }

    private static Collection<Price> getPriceListForGroup(List<Price> availablePricesGroup, Price incomingPrices) {

        List<Price> groupOfPriceList = new ArrayList<>();

        if (availablePricesGroup.isEmpty()) {
            groupOfPriceList.add(incomingPrices);
            return groupOfPriceList;
        }

        availablePricesGroup.stream()
                .filter(price -> incomingPrices.getPriceActionPeriod().isInsideIn(price.getPriceActionPeriod()))
                .findFirst()
                .ifPresent(price -> {
                    if (incomingPrices.getValue().equals(price.getValue())) {
                        incomingPrices.setBegin(price.getBegin());
                        incomingPrices.setEnd(price.getEnd());
                    } else {
                        groupOfPriceList.add(new Price(price, price.getBegin(), incomingPrices.getBegin()));
                        groupOfPriceList.add(new Price(price, incomingPrices.getEnd(), price.getEnd()));
                    }
                });

        availablePricesGroup.stream()
                .filter(price -> incomingPrices.getPriceActionPeriod().haveIntersectionOnTheRightWith(price.getPriceActionPeriod()))
                .findFirst()
                .ifPresent(price -> {
                    if (incomingPrices.getValue().equals(price.getValue()))
                        incomingPrices.setEnd(price.getEnd());
                    else
                        groupOfPriceList.add(new Price(price, incomingPrices.getEnd(), price.getEnd()));
                });

        availablePricesGroup.stream()
                .filter(price -> incomingPrices.getPriceActionPeriod().haveIntersectionOnTheLeftWith(price.getPriceActionPeriod()))
                .findFirst()
                .ifPresent(price -> {
                    if (incomingPrices.getValue().equals(price.getValue()))
                        incomingPrices.setBegin(price.getBegin());
                    else
                        groupOfPriceList.add(new Price(price, price.getBegin(), incomingPrices.getBegin()));
                });

        availablePricesGroup.stream()
                .filter(price -> incomingPrices.getPriceActionPeriod().noIntersection(price.getPriceActionPeriod()))
                .forEachOrdered(groupOfPriceList::add);

        groupOfPriceList.add(incomingPrices);

        return groupOfPriceList;
    }

    private static boolean isContain(Price availablePrice, Price incomingPrice) {
        return availablePrice.getProductCode().equals(incomingPrice.getProductCode()) &&
                availablePrice.getNumber() == incomingPrice.getNumber() &&
                availablePrice.getDepart() == incomingPrice.getDepart();
    }

}