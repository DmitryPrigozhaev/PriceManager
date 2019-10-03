package com.prigozhaev.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * The {@code Price} class represents the price.
 * <p>
 * Each item sold has a price. There may be several prices for a product; each price has
 * its own number, department affiliation, price action period and currency value.
 * <p>
 * A price history is stored in the database for each product.
 * <p>
 * Only one price from the prices with the same number and department can act at one moment in time.
 * Usually the product is sold at the first price, the second, third and fourth can be used to apply
 * discounts (i.e. the discount condition worked, the product will be sold at price number 2).
 * <p>
 * A cash desk can be served by a department, then it will use the prices indicated for
 * that department when selling it.
 *
 * @author Dmitry Prigozhaev
 * 30.09.2019
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Price implements Serializable {

    @EqualsAndHashCode.Exclude
    private Long id;

    private String productCode;

    private int number;

    private int depart;

    private Date begin;

    private Date end;

    private Long value;

    @EqualsAndHashCode.Exclude
    private transient PriceActionPeriod priceActionPeriod;

    /**
     * Initializes a newly created {@code Price} object so that it represents
     * an empty price.
     * <p>
     * Note that use of this constructor is not recommended
     * because the {@code PriceActionPeriod} object will not initialize
     * because there is no information about the start and end time of the price.
     */
    public Price() {
    }

    /**
     * Standard constructor for initializing an {@code Price} object.
     *
     * @param productCode the unique product code
     * @param number      the price number
     * @param depart      the department number
     * @param begin       the price start date
     * @param end         the price end date
     * @param value       the currency value (in kopecks)
     */
    public Price(String productCode, int number, int depart, Date begin, Date end, Long value) {
        this.productCode = productCode;
        this.number = number;
        this.depart = depart;
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.priceActionPeriod = new PriceActionPeriod(begin, end);
    }

    /**
     * The constructor is used to create a new price when combining prices,
     * when some prices may change the price action period.
     *
     * @param price the existing price
     * @param begin the new start date for the price action period
     * @param end   the new end date for the price action period
     * @see com.prigozhaev.util.PriceManager#merge(Collection, Collection)
     */
    public Price(Price price, Date begin, Date end) {
        this.productCode = price.getProductCode();
        this.number = price.getNumber();
        this.depart = price.getDepart();
        this.begin = begin;
        this.end = end;
        this.value = price.value;
        this.priceActionPeriod = new PriceActionPeriod(begin, end);
    }

    /**
     * The {@code PriceActionPeriod} class represents the price action period.
     * By design, it should always be equal to the time range between {@code this.begin} and {@code this.end}.
     */
    public class PriceActionPeriod {

        private Date begin;
        private Date end;

        /**
         * Constructs an price action period.
         *
         * @param begin the start date for the price action period
         * @param end   the end date for the price action period
         */
        PriceActionPeriod(Date begin, Date end) {
            this.begin = begin;
            this.end = end;
        }

        /**
         * The method checks if the price action period (this) is inside the another period (param).
         * <p>
         * For example (return {@code true}):
         * <pre>
         * (1) {this} checked period;
         * (2) {param} price action period;
         *
         * |----------┗━━━━━(1)━━━━━┛-----------
         * |-------┗━━━━━━━━━(2)━━━━━━━━━┛-------
         * </pre>
         *
         * @param priceActionPeriod the period, regarding which check will be made
         * @return {@code true} if the checked period (this) is fully in the range of
         * the other period (param), and {@code false} otherwise
         */
        public boolean isInsideIn(PriceActionPeriod priceActionPeriod) {
            return (this.begin.after(priceActionPeriod.begin) || this.begin.equals(priceActionPeriod.begin)) &&
                    (this.end.before(priceActionPeriod.end) || this.end.equals(priceActionPeriod.end));
        }

        /**
         * The method checks if the price action period (this) intersects with
         * another period (param) on the right.
         * <p>
         * For example (return {@code true}):
         * <pre>
         * (1) {this} checked period;
         * (2) {param} price action period;
         *
         * |----------┗━━━━━━━━━(1)━━━━━━━━┛-----
         * |-------┗━━━━━(2)━━━━━┛--------------
         * </pre>
         *
         * @param priceActionPeriod the period, regarding which check will be made
         * @return {@code true} if the checked period (this) has an intersection with
         * another period (param) on the right, and {@code false} otherwise
         */
        public boolean haveIntersectionOnTheLeftWith(PriceActionPeriod priceActionPeriod) {
            return this.end.after(priceActionPeriod.end) &&
                    this.begin.before(priceActionPeriod.end) &&
                    this.begin.after(priceActionPeriod.begin);
        }

        /**
         * The method checks if the price action period (this) intersects with
         * another period (param) on the left.
         * <p>
         * For example (return {@code true}):
         * <pre>
         * (1) {this} checked period;
         * (2) {param} price action period;
         *
         * |-------┗━━━━━(1)━━━━━┛--------------
         * |----------┗━━━━━━━━━(2)━━━━━━━━┛-----
         * </pre>
         *
         * @param priceActionPeriod the period, regarding which check will be made
         * @return {@code true} if the checked period (this) has an intersection with
         * another period (param) on the left, and {@code false} otherwise
         */
        public boolean haveIntersectionOnTheRightWith(PriceActionPeriod priceActionPeriod) {
            return this.begin.before(priceActionPeriod.begin) &&
                    this.end.after(priceActionPeriod.begin) &&
                    this.end.before(priceActionPeriod.end);
        }

        /**
         * The method checks if the price action period (this) does not intersect with another period (param).
         * <p>
         * For example (return {@code true}):
         * <pre>
         * (1) {this} checked period;
         * (2) {param} price action period;
         *
         * |--┗━━━━━(1)━━━━━┛-------------------
         * |------------------┗━━━━━(2)━━━━━┛---
         * </pre>
         *
         * @param priceActionPeriod the period, regarding which check will be made
         * @return {@code true} if the checked period (this) is include the
         * another period (param), and {@code false} otherwise
         */
        public boolean doesNotIntersectionWith(PriceActionPeriod priceActionPeriod) {
            return (this.begin.after(priceActionPeriod.end) || this.begin.equals(priceActionPeriod.end)) ||
                    (this.end.before(priceActionPeriod.begin) || this.end.equals(priceActionPeriod.begin));
        }

    }

}