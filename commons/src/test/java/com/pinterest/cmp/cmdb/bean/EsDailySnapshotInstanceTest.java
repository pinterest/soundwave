package com.pinterest.cmp.cmdb.bean;

import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class EsDailySnapshotInstanceTest {

  @Test
  public void roundToSeconds() throws Exception {
    DateTime d = new DateTime(2016, 7, 7, 10, 11, 23, 123);
    Date d2 = EsDailySnapshotInstance.roundToSeconds(d.toDate());
    Assert.assertEquals(d2.getTime(), d.getMillis() - 123);
  }

  @Test
  public void getStringOfPeriod() throws Exception {
    DateTime d = new DateTime(2016, 7, 7, 10, 11, 23, 123);
    Assert.assertEquals("15s",
        EsDailySnapshotInstance.getStringOfPeriod(d.minusSeconds(15).toDate(), d.toDate()));
    Assert.assertEquals("1m 15s", EsDailySnapshotInstance
        .getStringOfPeriod(d.minusMinutes(1).minusSeconds(15).toDate(), d.toDate()));
    Assert.assertEquals("2h 1m 15s", EsDailySnapshotInstance
        .getStringOfPeriod(d.minusHours(2).minusMinutes(1).minusSeconds(15).toDate(), d.toDate()));
    Assert.assertEquals("13D 2h 1m 15s", EsDailySnapshotInstance
        .getStringOfPeriod(d.minusDays(13).minusHours(2).minusMinutes(1).minusSeconds(15).toDate(),
            d.toDate()));
    Assert.assertEquals("11M 3D 2h 1m 15s", EsDailySnapshotInstance
        .getStringOfPeriod(
            d.minusMonths(11).minusDays(3).minusHours(2).minusMinutes(1).minusSeconds(15).toDate(),
            d.toDate()));
    Assert.assertEquals("1Y 2M 13D 2h 1m 15s", EsDailySnapshotInstance
        .getStringOfPeriod(
            d.minusYears(1).minusMonths(2).minusDays(13).minusHours(2).minusMinutes(1)
                .minusSeconds(15).toDate(), d.toDate()));

  }

}
