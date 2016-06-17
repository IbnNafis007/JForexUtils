package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Currency;
import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.ICurrency;
import com.google.common.collect.Sets;
import com.jforex.programming.currency.CurrencyBuilder;
import com.jforex.programming.misc.MathUtil;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class MathUtilTest extends CurrencyUtilForTest {

    private final Set<ICurrency> currenciesForkPowerSetTests =
            CurrencyBuilder.fromNames(currencyNameEUR,
                                      currencyNameUSD,
                                      currencyNameAUD,
                                      currencyNameJPY);

    @SuppressWarnings("unchecked")
    private Set<Set<ICurrency>> getEmptyPowerSet() {
        return Sets.newHashSet(CurrencyBuilder.fromNames(""));
    }

    @Test
    public void testkPowerSetRetunsEmptySetForEmptySourceSet() {
        assertThat(MathUtil.kPowerSet(getEmptyPowerSet(), 2),
                   equalTo(Collections.<Currency> emptySet()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testkPowerSetRetunsEmptySetForSourceSetWithSizeOne() {
        final Set<Set<ICurrency>> currencies = Sets.newHashSet(CurrencyBuilder.fromNames(currencyNameEUR));

        assertThat(MathUtil.kPowerSet(currencies, 2),
                   equalTo(Collections.<Currency> emptySet()));
    }

    @Test
    public void testkPowerSetRetunsEmptyPowerSetSetWhenkIsZero() {
        assertThat(MathUtil.kPowerSet(currenciesForkPowerSetTests, 0),
                   equalTo(getEmptyPowerSet()));
    }

    @Test
    public void testkPowerSetRetunsEmptySetWhenkIsGreaterThanCurrencySet() {
        assertThat(MathUtil.kPowerSet(currenciesForkPowerSetTests, 5),
                   equalTo(Collections.<Currency> emptySet()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testkPowerSetRetunsCorrectPowerSet() {
        final Set<Set<ICurrency>> expectedkPowerset =
                Sets.newHashSet(CurrencyBuilder.fromNames(currencyNameEUR, currencyNameJPY, currencyNameUSD),
                                CurrencyBuilder.fromNames(currencyNameJPY, currencyNameUSD, currencyNameAUD),
                                CurrencyBuilder.fromNames(currencyNameEUR, currencyNameUSD, currencyNameAUD),
                                CurrencyBuilder.fromNames(currencyNameEUR, currencyNameJPY, currencyNameAUD));

        assertThat(MathUtil.kPowerSet(currenciesForkPowerSetTests, 3),
                   equalTo(expectedkPowerset));
    }

    public void testRateOfReturnIsCorrect() {
        assertThat(MathUtil.rateOfReturn(1.3245, 1.3248), closeTo(-0.022644, 0.000001));
        assertThat(MathUtil.rateOfReturn(99.345, 99.341), closeTo(0.004026, 0.000001));
        assertThat(MathUtil.rateOfReturn(120, 100), closeTo(20, 0.000001));
        assertThat(MathUtil.rateOfReturn(70, 90), closeTo(-22.222222, 0.000001));
    }

    @Test
    public void testRoundDouble() {
        assertThat(MathUtil.roundDouble(1.3245656, 0), equalTo(1.0));
        assertThat(MathUtil.roundDouble(1.3245656, 1), equalTo(1.3));
        assertThat(MathUtil.roundDouble(1.3245656, 2), equalTo(1.32));
        assertThat(MathUtil.roundDouble(-1.3245656, 0), equalTo(-1.0));
        assertThat(MathUtil.roundDouble(-1.3245656, 1), equalTo(-1.3));
        assertThat(MathUtil.roundDouble(-1.3245656, 2), equalTo(-1.32));
        assertThat(MathUtil.roundDouble(-1.35, 1), equalTo(-1.4));
    }

    @Test
    public void testRoundAmount() {
        assertThat(MathUtil.roundAmount(123456.7867545), equalTo(123456.786755));
        assertThat(MathUtil.roundAmount(0.0015679), equalTo(0.001568));
        assertThat(MathUtil.roundAmount(0.1254679), equalTo(0.125468));
    }

    @Test
    public void testRoundPips() {
        assertThat(MathUtil.roundPips(12.61), equalTo(12.6));
        assertThat(MathUtil.roundPips(5.55), equalTo(5.6));
        assertThat(MathUtil.roundPips(-24.62), equalTo(-24.6));
        assertThat(MathUtil.roundPips(-24.65), equalTo(-24.7));
    }

    @Test
    public void testRoundPriceForNonJPYPrices() {
        assertThat(MathUtil.roundPrice(1.10687, instrumentEURUSD), equalTo(1.10687));
        assertThat(MathUtil.roundPrice(1.126864, instrumentEURUSD), equalTo(1.12686));
        assertThat(MathUtil.roundPrice(1.126865, instrumentEURUSD), equalTo(1.12687));
    }

    @Test
    public void testRoundPriceForJPYPrices() {
        assertThat(MathUtil.roundPrice(132.34, instrumentUSDJPY), equalTo(132.34));
        assertThat(MathUtil.roundPrice(132.344, instrumentUSDJPY), equalTo(132.344));
        assertThat(MathUtil.roundPrice(132.3445, instrumentUSDJPY), equalTo(132.345));
    }

    @Test
    public void testRoundIsValueDivisible() {
        assertFalse(MathUtil.isValueDivisibleByX(10.4, 10));
        assertTrue(MathUtil.isValueDivisibleByX(10.4, 0.1));
        assertTrue(MathUtil.isValueDivisibleByX(0, 0.1));
        assertFalse(MathUtil.isValueDivisibleByX(0.5, 0.2));
        assertTrue(MathUtil.isValueDivisibleByX(0.5, 0.5));
        assertTrue(MathUtil.isValueDivisibleByX(1000, 10));
    }
}
